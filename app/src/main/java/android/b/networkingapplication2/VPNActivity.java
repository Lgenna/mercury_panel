package android.b.networkingapplication2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.b.networkingapplication2.OverviewActivity.PREFS_DNS;
import static android.b.networkingapplication2.OverviewActivity.PREFS_FIREWALL;
import static android.b.networkingapplication2.OverviewActivity.PREFS_GENERAL;
import static android.b.networkingapplication2.OverviewActivity.PREFS_VPN;
import static android.b.networkingapplication2.OverviewActivity.startupTime;

public class VPNActivity extends AppCompatActivity {

    private String serverAddress = "192.168.0.12";

    public static Switch monitoringStatus;
    private LinearLayout VPNFileBox, VPNSettingsBox;
    private ImageButton VPNURLAdd, VPNDefaultAdd, ChangeVPNServer;
    private TextView currentVpnServer, upTime;

    String currentServer;

    private Object mPauseLock;
    private boolean mPaused, mFinished;

    private static final String TAG = "VPNActivity";

    private String formattedUpTime;

    public interface Prefs {
        String NAME = "connection";
        String SERVER_ADDRESS = "server.address";
        String SERVER_PORT = "server.port";
        String SHARED_SECRET = "shared.secret";
        String PACKAGES = "packages";
        String DNSSERVERS = "dnsServers";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn);

        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;

        VPNFileBox = findViewById(R.id.vpn_file_box);
        VPNSettingsBox = findViewById(R.id.vpn_settings_box);
        monitoringStatus = findViewById(R.id.toggle_vpn);
        VPNURLAdd = findViewById(R.id.vpn_url_add);
        VPNDefaultAdd = findViewById(R.id.vpn_default_add);
        ChangeVPNServer = findViewById(R.id.change_vpn_server_button);
//        VPNURL = findViewById(R.id.vpn_url);
        currentVpnServer = findViewById(R.id.current_vpn_server);
        upTime = findViewById(R.id.up_time_data);

        updateViews();

        SharedPreferences prefs = getSharedPreferences(PREFS_VPN, MODE_PRIVATE);
//        SharedPreferences.Editor prefsEditor = getSharedPreferences(PREFS_VPN, MODE_PRIVATE).edit();

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE).edit();
        ChangeVPNServer.setOnClickListener(v -> {
            editor.putBoolean("isVPNServerDefault", false);
            editor.putBoolean("selectedVPNServer", false);
            editor.apply();
            prefs.edit().clear().apply();
            updateViews();
        });

        VPNURLAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Currently not implemented, using default", Toast.LENGTH_LONG).show();
            editor.putBoolean("isVPNServerDefault", true);
            editor.putBoolean("selectedVPNServer", true);
            editor.apply();
            setServerDefault(prefs);
            updateViews();
        });

        VPNDefaultAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Setting VPN to default", Toast.LENGTH_SHORT).show();
            editor.putBoolean("isVPNServerDefault", true);
            editor.putBoolean("selectedVPNServer", true);
            editor.apply();
            setServerDefault(prefs);
            updateViews();

        });

        // Builds a new thread
        Thread timer = new Thread() {
            @Override
            public void run() {

                try {
                    // while the app is open...
                    while (!mFinished) {

                        // methods that need to update info
                        updateInfo();

                        // update the user interface every 10 seconds
                        Thread.sleep(5000);
                        //create a synchronized boolean mPauseLock
                        synchronized (mPauseLock) {
                            // check to see if the activity was paused
                            while (mPaused) {
                                // if paused, stop updating the ui
                                try {
                                    mPauseLock.wait();
                                } catch (InterruptedException ignore) {
                                    // Panic x2
                                }
                            }
                        }
                    }
                    Log.i(TAG, "Finished Looping, this isn't supposed to happen.");
                } catch (InterruptedException ignore) {
                    // panic.
                }
            }
        };

        // start the thread
        timer.start();

        SharedPreferences VPNPrefs = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE);

        monitoringStatus.setOnClickListener(v -> {
            if (monitoringStatus.isChecked()) {
                if (VPNPrefs.getBoolean("isVPNServerDefault", false)) {

                    setServerDefault(prefs);

                    monitoringStatus.setEnabled(false);

                    Intent intent = VpnService.prepare(getBaseContext());
                    if (intent != null) {
                        VPNActivity.this.startActivityForResult(intent, 0);
                    } else {
                        VPNActivity.this.onActivityResult(0, RESULT_OK, null);
                    }
                } else {
                    Log.i(TAG, "VPN turned off due to connecting server");

                    monitoringStatus.setEnabled(true);
                    startService(getServiceIntent()
                            .setAction(ToyVpnService.ACTION_DISCONNECT));
//                    VPNActivity.this.startService(
//                            VPNActivity.this.getServiceIntent()
//                                    .setAction(ToyVpnService.ACTION_DISCONNECT));
                }

                updateInfo();

            } else {
                Log.i(TAG, "VPN turned off by user");

                monitoringStatus.setEnabled(true);
                VPNActivity.this.startService(
                        VPNActivity.this.getServiceIntent()
                                .setAction(ToyVpnService.ACTION_DISCONNECT));
            }
        });

    }

    private void setServerDefault(SharedPreferences prefs) {

//        final Set<String> dnsServersSet = new HashSet<>(getDnsServers());
        final Set<String> packageSet = new HashSet<>(getBlockedApps());

//        Log.i(TAG, "dnsServersSet : " + dnsServersSet.toString());

        prefs.edit()
            .putString(VPNActivity.Prefs.SERVER_ADDRESS, serverAddress)
            .putInt(VPNActivity.Prefs.SERVER_PORT, 8000)
            .putString(VPNActivity.Prefs.SHARED_SECRET, "test")
            .putStringSet(VPNActivity.Prefs.PACKAGES, packageSet)
//            .putStringSet(VPNActivity.Prefs.DNSSERVERS, dnsServersSet)
            .apply();

        updateInfo();
    }


    private void updateInfo() {

        SharedPreferences VPNPrefs = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE);

        if (VPNPrefs.getBoolean("isVPNServerDefault", false)) {
            currentServer = "Default VPN Server : ";
        } else {
            currentServer = "Something bad happened : ";
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_VPN, MODE_PRIVATE);
        String serverAddress = prefs.getString(VPNActivity.Prefs.SERVER_ADDRESS, "No data found");
        if (!serverAddress.equals("No data found")){
            currentServer += serverAddress;
        } else {
            currentServer = "No data found";
        }

        runOnUiThread(() -> currentVpnServer.setText(currentServer));

        long currentTime = new Date().getTime();

        long timeDifference = currentTime - startupTime;

        int currentTimeDifference = (int)timeDifference / 1000;

        int hours = currentTimeDifference / 3600;
        int minutes = (currentTimeDifference % 3600) / 60;
        int seconds = currentTimeDifference % 60;

        formattedUpTime = hours + " hours, " + minutes + " minutes, " + seconds + " seconds";

        runOnUiThread(() -> upTime.setText(formattedUpTime));
        Log.i(TAG, "Up-time : " + formattedUpTime);
    }


    private ArrayList<String> getBlockedApps() {

        SharedPreferences FirewallPrefs = getSharedPreferences(PREFS_FIREWALL, MODE_PRIVATE);

        ArrayList<String> BlockedApps = new ArrayList<>();

        Map<String, ?> keys = FirewallPrefs.getAll();

//        BlockedApps.add("com.example.a.missing.app");

        for(Map.Entry<String, ?> element : keys.entrySet()){
            BlockedApps.add(element.getKey());
        }

        return BlockedApps;
    }


    private ArrayList<String> getDnsServers() {
        SharedPreferences DNSPrefs = getSharedPreferences(PREFS_DNS, MODE_PRIVATE);

        ArrayList<String> DNSServers = new ArrayList<>();

        ArrayList<String> DNSBoolean = new ArrayList<>();


        Map<String, ?> keys = DNSPrefs.getAll();

        // TODO check for String and boolean pairs of DNS values ex. if DNS1 has a value, but not enabled


        // Find all the dns servers, then check if they are set to true, if case, add to a list
        for(Map.Entry<String, ?> element : keys.entrySet()) {
            String currentBoolean = element.getKey();
            if (currentBoolean.startsWith("b") && (element.getValue() + "").equals("true")) {
                DNSBoolean.add(currentBoolean.substring(1));
            }
        }

        // Go through the boolean value list and see if any strings with the same name match
        for(Map.Entry<String, ?> element : keys.entrySet()) {
            String currentString = element.getKey();
            if (currentString.startsWith("s") && !(element.getValue() + "").equals("")) {
                currentString = currentString.substring(1);
                for(String booleanKey : DNSBoolean) {

                    if (currentString.equals(booleanKey)) {
                        DNSServers.add(element.getValue() + "");
                    }
                }
            }
        }

        return DNSServers;
    }


    private void updateViews() {

        SharedPreferences VPNPrefs = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE);
        boolean selectedVPNServer = VPNPrefs.getBoolean("selectedVPNServer", false);

        if (!selectedVPNServer) {
            VPNSettingsBox.setForeground(getDrawable(R.color.transparent));
            VPNFileBox.setForeground(getDrawable(R.color.transparentClear));
//            VPNURL.setEnabled(true);
            monitoringStatus.setClickable(false);
            ChangeVPNServer.setClickable(false);
            VPNURLAdd.setClickable(true);
            VPNDefaultAdd.setClickable(true);
        } else {
            VPNFileBox.setForeground(getDrawable(R.color.transparent));
            VPNSettingsBox.setForeground(getDrawable(R.color.transparentClear));
//            VPNURL.setEnabled(false);
            monitoringStatus.setClickable(true);
            ChangeVPNServer.setClickable(true);
            VPNURLAdd.setClickable(false);
            VPNDefaultAdd.setClickable(false);
        }
    }



    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result == RESULT_OK) {
            startService(getServiceIntent().setAction(ToyVpnService.ACTION_CONNECT));
        }
    }

    public Intent getServiceIntent() {
        return new Intent(this, ToyVpnService.class);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_overview:
                intent = new Intent(this, OverviewActivity.class);
                break;
            case R.id.action_domain_blocker:
                intent = new Intent(this, DomainBlockerActivity.class);
                break;
            case R.id.action_vpn_servers:
                intent = new Intent(this, VPNActivity.class);
                break;
//            case R.id.action_dns_servers:
//                intent = new Intent(this, DNSActivity.class);
//                break;
            case R.id.action_firewall:
                intent = new Intent(this, FirewallActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(intent);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateInfo();
        updateViews();

        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}
