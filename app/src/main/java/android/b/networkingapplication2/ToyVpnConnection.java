/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.b.networkingapplication2;
import static database.MasterBlockListDbSchema.MasterBlockListTable.Cols.DOMAIN;
import static database.MasterBlockListDbSchema.MasterBlockListTable.TABLE_NAME;
import static java.nio.charset.StandardCharsets.US_ASCII;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.VpnService;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;


import database.BlockListBaseHelper;
import database.MasterBlockListBaseHelper;
import database.QueryLogBaseHelper;

public class ToyVpnConnection implements Runnable {

    ArrayList<String> list = new ArrayList<>();
    private QueryLogBaseHelper myQueDb;

    /**
     * Callback interface to let the {@link ToyVpnService} know about new connections
     * and update the foreground notification with connection status.
     */
    public interface OnEstablishListener {
        void onEstablish(ParcelFileDescriptor tunInterface);
    }
    /** Maximum packet size is constrained by the MTU, which is given as a signed short. */
    private static final int MAX_PACKET_SIZE = Short.MAX_VALUE;
    /** Time to wait in between losing the connection and retrying. */
    private static final long RECONNECT_WAIT_MS = TimeUnit.SECONDS.toMillis(3);
    /** Time between keepalives if there is no traffic at the moment.
     *
     * TODO: don't do this; it's much better to let the connection die and then reconnect when
     *       necessary instead of keeping the network hardware up for hours on end in between.
     **/
    private static final long KEEPALIVE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15);

    /** Time to wait without receiving any response before assuming the server is gone. */
    private static final long RECEIVE_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(20);
    /**
     * Time between polling the VPN interface for new traffic, since it's non-blocking.
     *
     * TODO: really don't do this; a blocking read on another thread is much cleaner.
     */
    private static final long IDLE_INTERVAL_MS = TimeUnit.MILLISECONDS.toMillis(100);
    /**
     * Number of periods of length {@IDLE_INTERVAL_MS} to wait before declaring the handshake a
     * complete and abject failure.
     *
     * TODO: use a higher-level protocol; hand-rolling is a fun but pointless exercise.
     */

    private final String TAG = "ToyVpnConnection";
    private static final int MAX_HANDSHAKE_ATTEMPTS = 50;
//    private static final int MAX_HANDSHAKE_ATTEMPTS = 5;
    private final VpnService mService;
    private final int mConnectionId;
    private final String mServerName;
    private final int mServerPort;
    private final byte[] mSharedSecret;
    private PendingIntent mConfigureIntent;
    private OnEstablishListener mOnEstablishListener;
    AsyncTask<?, ?, ?> runningTask;

    // Proxy settings
//    private String mProxyHostName;
//    private int mProxyHostPort;
    // Allowed/Disallowed packages for VPN usage
//    private final boolean mAllow;
    private final Set<String> mPackages;
//    private final Set<String> mDnsServers;

    public ToyVpnConnection(final VpnService service, final int connectionId,
                            final String serverName, final int serverPort,
                            final byte[] sharedSecret, final Set<String> packages) {

        mService = service;
        mConnectionId = connectionId;
        mServerName = serverName;
        mServerPort= serverPort;
        mSharedSecret = sharedSecret;
//        mDnsServers = dnsServers;
//        if (!TextUtils.isEmpty(proxyHostName)) {
//            mProxyHostName = proxyHostName;
//        }
//        if (proxyHostPort > 0) {
//             The port value is always an integer due to the configured inputType.
//            mProxyHostPort = proxyHostPort;
//        }
//        mAllow = allow;
        mPackages = packages;
    }
    /**
     * Optionally, set an intent to configure the VPN. This is {@code null} by default.
     */
    public void setConfigureIntent(PendingIntent intent) {
        mConfigureIntent = intent;
    }
    public void setOnEstablishListener(OnEstablishListener listener) {
        mOnEstablishListener = listener;
    }
    @Override
    public void run() {
        try {
            Log.i(getTag(), "Starting");
            // If anything needs to be obtained using the network, get it now.
            // This greatly reduces the complexity of seamless handover, which
            // tries to recreate the tunnel without shutting down everything.
            // In this demo, all we need to know is the server address.
            final SocketAddress serverAddress = new InetSocketAddress(mServerName, mServerPort);
            // We try to create the tunnel several times.
            // TODO: The better way is to work with ConnectivityManager, trying only when the
            //  network is available.
            // Here we just use a counter to keep things simple.
            for (int attempt = 0; attempt < 10; ++attempt) {
                // Reset the counter if we were connected.
                if (run(serverAddress)) {
                    attempt = 0;
                }
                // Sleep for a while. This also checks if we got interrupted.
                Thread.sleep(3000);
            }
            Log.i(getTag(), "Giving up");

            new Handler(Looper.getMainLooper()).post(() -> {
                VPNActivity.monitoringStatus.setChecked(false);
                VPNActivity.monitoringStatus.setEnabled(true);
                Toast.makeText(VPNActivity.context, "The server timed out", Toast.LENGTH_SHORT).show();
            });

        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            Log.e(getTag(), "Connection failed, exiting", e);
            new Handler(Looper.getMainLooper()).post(() -> {
                VPNActivity.monitoringStatus.setChecked(false);
                VPNActivity.monitoringStatus.setEnabled(true);
                Toast.makeText(VPNActivity.context, "There was an error connecting", Toast.LENGTH_SHORT).show();
            });

        }
    }



    private boolean run(SocketAddress server)
            throws IOException, InterruptedException, IllegalArgumentException {
        ParcelFileDescriptor iface = null;
        boolean connected = false;

        long startTime = System.currentTimeMillis();
        // Create a DatagramChannel as the VPN tunnel.
        try (DatagramChannel tunnel = DatagramChannel.open()) {
            // Protect the tunnel before connecting to avoid loopback.
            if (!mService.protect(tunnel.socket())) {
                throw new IllegalStateException("Cannot protect the tunnel");
            }
            // Connect to the server.
            tunnel.connect(server);
            // For simplicity, we use the same thread for both reading and
            // writing. Here we put the tunnel into non-blocking mode.
            tunnel.configureBlocking(false);
            // Authenticate and configure the virtual network interface.
            iface = handshake(tunnel);
            // Now we are connected. Set the flag.
            connected = true;
            // Packets to be sent are queued in this input stream.
            FileInputStream in = new FileInputStream(iface.getFileDescriptor());
            // Packets received need to be written to this output stream.
            FileOutputStream out = new FileOutputStream(iface.getFileDescriptor());
            // Allocate the buffer for a single packet.
            ByteBuffer packet = ByteBuffer.allocate(MAX_PACKET_SIZE);
            // Timeouts:
            //   - when data has not been sent in a while, send empty keepalive messages.
            //   - when data has not been received in a while, assume the connection is broken.
            long lastSendTime = System.currentTimeMillis();
            long lastReceiveTime = System.currentTimeMillis();
            // We keep forwarding packets till something goes wrong.

            myQueDb = OverviewActivity.getMyQueDb();


            while (true) {
                // Assume that we did not make any progress in this iteration.
                boolean idle = true;
                // Read the outgoing packet from the input stream.

//                final long timeNow = System.currentTimeMillis();
                int length = in.read(packet.array());

                if (length > 0) {

                    // Write the outgoing packet to the tunnel.
                    packet.limit(length);

                    StringBuilder hexedPacket = new StringBuilder();

                    byte[] array = packet.array();
                    for (int i = 0; i < 250; i++) { // 1 byte = 2 hex, maximum hex we would need being around 400, but to keep it safe, use 250 bytes which equals 500 hex chars
                        hexedPacket.append(String.format("%02X", array[i]));
                    }

                    String sHexedPacket = hexedPacket.toString();

                    boolean allowPacket = true;

                    if (sHexedPacket.startsWith("160301", 104)) {
                        String domain = handyHector(sHexedPacket, 358);

                        if (domain != null) {
                            allowPacket = domainMatcher(domain);
                        }

                        String sAllowPacket;

                        if (allowPacket) {
                            sAllowPacket = "OK (forwarded)";
                        } else {
                            sAllowPacket = "Blocked (trashed)";
                        }

                        if (myQueDb != null && domain != null) {
                            myQueDb.insertData(
                                    "" + System.currentTimeMillis(),
                                    "" + domain,
                                    sAllowPacket);
                            OverviewActivity.setMyQueDb(myQueDb);
                        }
                    }

                    if (allowPacket) { // you can either get through or get thrown out
                        tunnel.write(packet);
                    }

                    packet.clear();
                    // There might be more outgoing packets.
                    idle = false;
                    lastReceiveTime = System.currentTimeMillis();
                }


                // Read the incoming packet from the tunnel.
                length = tunnel.read(packet);
                if (length > 0) {
                    // Ignore control messages, which start with zero.
                    if (packet.get(0) != 0) {
                        // Write the incoming packet to the output stream.
                        out.write(packet.array(), 0, length);
                    }
                    packet.clear();
                    // There might be more incoming packets.
                    idle = false;
                    lastSendTime = System.currentTimeMillis();
                }

                // If we are idle or waiting for the network, sleep for a
                // fraction of time to avoid busy looping.

                if (idle) {
                    Thread.sleep(IDLE_INTERVAL_MS);
//                        final long timeNow = System.currentTimeMillis();
//                        if (lastSendTime + KEEPALIVE_INTERVAL_MS <= timeNow) {
                        // We are receiving for a long time but not sending.
                        // Send empty control messages.

                        // No messages? I'll just chuck a scroll of scribbles at the server, that oughta' fix it :]

                        packet.put((byte) 0).limit(1);
                        for (int i = 0; i < 3; ++i) {
                            packet.position(0);
                            tunnel.write(packet);
                        }
                        packet.clear();
//                            lastSendTime = timeNow;
//                        } else if (lastReceiveTime + RECEIVE_TIMEOUT_MS <= timeNow) {
                        // We are sending for a long time but not receiving.
//                            Log.e(TAG,"Boy, that ther' server takin' a looong time to respond!");
//                        }
                }
            }
//            } catch (InterruptedException e) {
//                    Log.e(TAG, "Thread Interrupted, stopping VPN connection");
//            }
        } catch (SocketException e) {
            Log.e(TAG, "Cannot use socket", e);
        } finally {
            if (iface != null) {
                try {
                    iface.close();
                } catch (IOException e) {
                    Log.e(getTag(), "Unable to close interface", e);
                }
            }
        }
        return connected;
    }

    /**
     * Looks for client handshakes, because these contain server domains. Starts off by looking at
     *  where the length byte of a packet would be for the domain length. Then it takes that number
     *  of bytes after the starting point and stores it as a possibleDomain. It runs this through
     *  another method that checks to see if the domain generated is valid by passing it through
     *  a url and a uri validator to see if it is malformed. If it is, it tries again using this
     *  method but with a different starting point. If this try is unsuccessful it returns null,
     *  otherwise it returns the generated domain.
     * @param input A packet in hex form as ascii text;
     * @param startingPoint the known point where the domain usually is, there are two variations.
     * @return returns a recursive call with a different starting point, null if no valid domain
     *  was found, or the valid domain that was found.
     */

    private String handyHector(String input, int startingPoint) {
        int length = 0;
        StringBuilder str = new StringBuilder();

        for(int i = startingPoint - 4; i < startingPoint; i += 2) {
            length = Integer.parseInt(input.substring(i, i + 2), 16);
        }

        int endingPoint = startingPoint + (length * 2);

        if (endingPoint < input.length()) {
            for (int i = startingPoint; i < endingPoint; i += 2) {
                str.append((char) Integer.parseInt(input.substring(i, i + 2), 16));
            }
        }

        String possibleDomain = str.toString();

        if (domainChecker(possibleDomain)) {
            // domain is valid, add it to the query log
            Log.i(TAG, "Handshake made : " + possibleDomain);
//            Log.i(TAG, "Packet : " + input);
            return possibleDomain;
        } else if (startingPoint != 362) {
//             try a different starting point
            return handyHector(input, 362);
        } else {
            // was not valid, ignore
            return null;
        }
    }

    private boolean domainChecker (String possibleDomain) {
        try {
            URL url = new URL("https://" + possibleDomain);
            url.toURI();
            return true;

        } catch (MalformedURLException | URISyntaxException ignore) {
            return false;
        }

    }


    private ParcelFileDescriptor handshake(DatagramChannel tunnel)
            throws IOException, InterruptedException {
        // To build a secured tunnel, we should perform mutual authentication
        // and exchange session keys for encryption. To keep things simple in
        // this demo, we just send the shared secret in plaintext and wait
        // for the server to send the parameters.
        // Allocate the buffer for handshaking. We have a hardcoded maximum
        // handshake size of 1024 bytes, which should be enough for demo
        // purposes.
        ByteBuffer packet = ByteBuffer.allocate(1024);
        // Control messages always start with zero.
        packet.put((byte) 0).put(mSharedSecret).flip();
        // Send the secret several times in case of packet loss.
        for (int i = 0; i < 3; ++i) {
            packet.position(0);
            tunnel.write(packet);
        }
        packet.clear();
        // Wait for the parameters within a limited time.
        for (int i = 0; i < MAX_HANDSHAKE_ATTEMPTS; ++i) {
            Thread.sleep(IDLE_INTERVAL_MS);
            // Normally we should not receive random packets. Check that the first
            // byte is 0 as expected.
            int length = tunnel.read(packet);
            if (length > 0 && packet.get(0) == 0) {
                return configure(new String(packet.array(), 1, length - 1, US_ASCII).trim());
            }
        }
        throw new IOException("Timed out");

    }

//    String[] appPackages = {
//            "com.android.chrome",
//            "com.google.android.youtube",
//            "com.example.a.missing.app"};


    private ParcelFileDescriptor configure(String parameters) throws IllegalArgumentException {
        // Configure a builder while parsing the parameters.
        VpnService.Builder builder = mService.new Builder();

        for (String parameter : parameters.split(" ")) {
            String[] fields = parameter.split(",");
            try {
                switch (fields[0].charAt(0)) {
                    case 'm':
                        builder.setMtu(Short.parseShort(fields[1]));
                        break;
                    case 'a':
                        builder.addAddress(fields[1], Integer.parseInt(fields[2]));
                        break;
                    case 'r':
                        builder.addRoute(fields[1], Integer.parseInt(fields[2]));
                        break;
                    case 'd':
                        builder.addDnsServer(fields[1]);
                        break;
                    case 's':
                        builder.addSearchDomain(fields[1]);
                        break;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Bad parameter: " + parameter);
            }
        }
        // Create a new interface using the builder and save the parameters.
        final ParcelFileDescriptor vpnInterface;

        for (String packageName : mPackages) {
            try {

//                if (mAllow) {
//                    builder.addAllowedApplication(packageName);
//                } else {
                builder.addAllowedApplication(packageName); // any application enabled on the firewall
                //  is added to a VPN which is hosted on
                //  a server that doesn't have internet
                //  access. AKA, The Crude Approach
//                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, "Package not available: " + packageName, e);
            }
        }

        /**
         * instead of sending every application except for a few home free, why not send all the
         *  applications through the vpn, but send the ones who you want blocked with a dead end
         *  dns server, whereas all the other ones use... well, somehow get a default dns server
         *  otherwise use the one that was provided.
         */

        // TODO builder.addDnsServer(dnsAddress1);

//        mDnsServers



        builder.setSession(mServerName).setConfigureIntent(mConfigureIntent);
//        if (!TextUtils.isEmpty(mProxyHostName)) {
//            builder.setHttpProxy(ProxyInfo.buildDirectProxy(mProxyHostName, mProxyHostPort));
//        }
        synchronized (mService) {
            vpnInterface = builder.establish();
            if (mOnEstablishListener != null) {
                mOnEstablishListener.onEstablish(vpnInterface);
            }
        }
        Log.i(getTag(), "New interface: " + vpnInterface + " (" + parameters + ")");
        return vpnInterface;
    }
    private final String getTag() {
        return ToyVpnConnection.class.getSimpleName() + "[" + mConnectionId + "]";
    }

    /**
     * Takes a given domain being passed through the VPN and tries to match it with one in the
     *  master block list, list. If it successfully matches the given domain with one from the
     *  block list, it blocks it
     * @param foundDomain a given domain to look for in the block list
     * @return returns true or false if the given domain is to be blocked or not
     */

    private boolean domainMatcher(String foundDomain) {

        MasterBlockListBaseHelper myMasDb = OverviewActivity.getMyMasDb();

        Cursor cursor = myMasDb.selectData(foundDomain);

        if (cursor == null || cursor.getCount() > 0) {
            Log.i(TAG, "Blocked : " + foundDomain);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Creates an IPV4 address given a network packet
     *
     * @param packet a network packet sent through the VPN
     * @return a string of the ip address of the server that is being requested
     */

    private String ipGenerator(ByteBuffer packet) {
        ArrayList<String> letters = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f"));
        StringBuilder ipAddress = new StringBuilder();

        for (int i = 16; i < 20; i++) {
            if (packet.get(i) < 10 && packet.get(i) >= 0) {
                ipAddress.append(Integer.valueOf("0" + Integer.toHexString(packet.get(i) & 0xFF)));
            } else if (letters.contains(Integer.toHexString(packet.get(i) & 0xFF))) {
                int n = (int) Long.parseLong(Integer.toHexString(packet.get(i) & 0xFF), 16);
                ipAddress.append(Integer.decode("0" + n));
            } else {
                int n = (int) Long.parseLong(Integer.toHexString(packet.get(i) & 0xFF), 16);
                ipAddress.append(Integer.valueOf(n));
            }
            if (i != 19) {
                ipAddress.append(".");
            }
        }
        return ipAddress.toString();
    }
}

