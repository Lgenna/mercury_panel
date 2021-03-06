package android.b.networkingapplication2;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import database.BlockListBaseHelper;
import database.MasterBlockListBaseHelper;
import database.QueryLogBaseHelper;

public class OverviewActivity extends AppCompatActivity {

    String[] locations = new String[]  {"/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
            "/sys/devices/platform/tegra-i2c.3/i2c-4/4-004c/temperature",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/devices/platform/tegra_tmon/temp1_input",
            "/sys/kernel/debug/tegra_thermal/temp_tj",
            "/sys/devices/platform/s5p-tmu/temperature",
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/class/hwmon/hwmon0/device/temp1_input",
            "/sys/devices/virtual/thermal/thermal_zone1/temp",
            "/sys/devices/platform/s5p-tmu/curr_temp"};

    private Object mPauseLock;
    private boolean mPaused, mFinished;
    private TextView deviceTemp, deviceCPU, memoryUsage, IPV4Address,
            IPV6Address, startTime, totalApps, blockedApps, dnsStatus,
            dnsNumber, vpnStatus, vpnServer, totalQueries, blockedQueries;
    private Intent intent;
    private String addressIPV6, sTotalApps, addressIPV4, formattedMemoryUsed, formattedTemperature,
            formattedCPUUsage, formattedUpTime, sBlockedApps, sDNSStatus, sDNSNumber,
            sVPNStatus = "Offline!", sVPNServer;

    public static long startupTime;
    public static ArrayList<ApplicationInfo> installedApps = new ArrayList<>();
    public static final String PREFS_GENERAL = "NetworkingApp";
    public static final String PREFS_FIREWALL = "Firewall";
    public static final String PREFS_DNS = "DomainNameSystem";
    public static final String PREFS_VPN = "VirtualPrivateNetwork";

    private static QueryLogBaseHelper myQueDb;
    private static BlockListBaseHelper myBloDb;
    private static MasterBlockListBaseHelper myMasDb;
    private static final String TAG = "OverviewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;

        setContentView(R.layout.activity_overview);

        totalQueries = findViewById(R.id.total_queries);
        blockedQueries = findViewById(R.id.total_queries_blocked);
        deviceTemp = findViewById(R.id.cpu_temperature_status);
        memoryUsage = findViewById(R.id.memory_usage_percent);
        deviceCPU = findViewById(R.id.cpu_usage_percent);
        IPV4Address = findViewById(R.id.ipv4_address);
        IPV6Address = findViewById(R.id.ipv6_address);
        startTime = findViewById(R.id.up_time_data);
        totalApps = findViewById(R.id.allowed_applications);
        blockedApps = findViewById(R.id.blocked_applications);
//        dnsStatus = findViewById(R.id.dns_status);
//        dnsNumber = findViewById(R.id.dns_ip_address);
        vpnStatus = findViewById(R.id.vpn_status);
        vpnServer = findViewById(R.id.vpn_server);

        LinearLayout domainBlockerBox = findViewById(R.id.domain_blocker_info_box);
//        LinearLayout DNSBox = findViewById(R.id.dns_info_box);
        LinearLayout firewallBox = findViewById(R.id.firewall_info_box);
        LinearLayout VPNBox = findViewById(R.id.vpn_info_box);

        // get time when OnCreate is called
        startupTime = new Date().getTime();

        myQueDb = new QueryLogBaseHelper(getBaseContext());
        myBloDb = new BlockListBaseHelper(getBaseContext());
        myMasDb = new MasterBlockListBaseHelper(getBaseContext());

        domainBlockerBox.setOnClickListener(v -> {
            intent = new Intent(this, DomainBlockerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        });

//        DNSBox.setOnClickListener(v -> {
//            intent = new Intent(this, DNSActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            this.startActivity(intent);
//        });

        firewallBox.setOnClickListener(v -> {
            intent = new Intent(this, FirewallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        });

        VPNBox.setOnClickListener(v -> {
            intent = new Intent(this, VPNActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        });

        // Builds a new thread
        Thread uiUpdater = new Thread() {
            @Override
            public void run() {

                try {
                    // while this activity is open...
                    while (!mFinished) {

                        getDomainBlockerInfo();

                        getIPAddresses();
                        getBatteryTemp();
                        getCPUUsage();
                        getMemoryUsage();

                        getApplications();

                        getVPNStatus();
//                        getDnsInfo();

                        getUpTime();

                        runOnUiThread(() -> updateUI());

                        // update the user interface every 5 seconds
                        Thread.sleep(5000);

                        // create a synchronized boolean mPauseLock
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
        uiUpdater.start();

    }

    public static QueryLogBaseHelper getMyQueDb() {
        return myQueDb;
    }

    public static BlockListBaseHelper getMyBloDb() {
        return myBloDb;
    }

    public static MasterBlockListBaseHelper getMyMasDb() {
        return myMasDb;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private long lBlockedQueries = 0;
    private long lTotalQueries = 0;

    private void getDomainBlockerInfo() {

        if (myQueDb != null) {
            lTotalQueries = myQueDb.countData();
            Cursor blockedQueriesRes = myQueDb.selectData("STATUS", "Blocked (trashed)");

            if (blockedQueriesRes != null) {
                lBlockedQueries = blockedQueriesRes.getCount();
            }
        }

        String sTotalQueries = lTotalQueries + " Total Queries";
        String sBlockedQueries = lBlockedQueries + " Blocked Queries";

        runOnUiThread(() -> {
            totalQueries.setText(sTotalQueries);
            blockedQueries.setText(sBlockedQueries);
        });
    }

//    private void getDnsInfo() {
//
//        SharedPreferences DNSPrefs = getSharedPreferences(PREFS_DNS, MODE_PRIVATE);
//
//        String[] DNSNames = {"Custom 1 (IPV4)", "Custom 2 (IPV4)", "Custom 3 (IPV6)", "Custom 4 (IPV6)"};
//
//        int counter = 0;
//        for (String element : DNSNames) {
//            boolean currentValue = DNSPrefs.getBoolean("b" + element, false);
//            if (currentValue) {
//                counter++;
//            }
//        }
//
//        if (counter > 0) {
//            sDNSNumber = counter + " DNS(s) Active";
//            sDNSStatus = "Online!";
//            runOnUiThread(() -> dnsNumber.setText(sDNSNumber));
//            runOnUiThread(() -> dnsStatus.setText(sDNSStatus));
//        }
//    }

    private void getVPNStatus() {
        try {
            if (VPNActivity.monitoringStatus.isChecked()) {
                sVPNStatus = "Online!";
            }
        } catch (NullPointerException ignore) {
            // VPNActivity was never initialized yet
        }

        runOnUiThread(() -> vpnStatus.setText(sVPNStatus));

        SharedPreferences GeneralPrefs = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE);
        boolean VPNServer = GeneralPrefs.getBoolean("isVPNServerDefault", false);

        if (VPNServer) {
            sVPNServer = "Default VPN Server";
            runOnUiThread(() -> vpnServer.setText(sVPNServer));
        }
    }


    private void getUpTime() {
        // System Uptime - get current time

        long currentTime = new Date().getTime();

        long timeDifference = currentTime - startupTime;

        int currentTimeDifference = (int)timeDifference / 1000;

        int hours = currentTimeDifference / 3600;
        int minutes = (currentTimeDifference % 3600) / 60;
        int seconds = currentTimeDifference % 60;

        formattedUpTime = hours + " hours, " + minutes + " minutes, " + seconds + " seconds";

        Log.i(TAG, "Up-time : " + formattedUpTime);
    }


    public void getApplications() {

        Log.i(TAG, "Started getApplications...");

        // Code created with the help of Stack Overflow question:
        // https://stackoverflow.com/questions/6165023/get-list-of-installed-android-applications
        // Question by user577732:
        // https://stackoverflow.com/users/577732/user577732
        // Answer by Kevin Coppock:
        // https://stackoverflow.com/users/321697/kevin-coppock

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        installedApps = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) { // updated system app
                installedApps.add(app);

            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) { // non-updated system app, do nothing

            } else { // user-installed app
                installedApps.add(app);
            }
        }

        boolean getApplications = true;

        sTotalApps = installedApps.size() + " Total Apps";
        Log.i(TAG, sTotalApps);

        // update the blocked apps number here so apps can be blocked dynamically
        SharedPreferences FirewallPrefs = getSharedPreferences(PREFS_FIREWALL, MODE_PRIVATE);

        sBlockedApps = FirewallPrefs.getAll().size() + " Blocked Apps";
    }

    private void getIPAddresses() {

        // Code created with help of Stack Overflow question
        // https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
        // Question by Nilesh Tupe:
        // https://stackoverflow.com/users/640034/nilesh-tupe
        // Answer by Whome:
        // https://stackoverflow.com/users/185565/whome

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for(int i = 0; i < addrs.size(); i++) {
                    if (!addrs.get(i).isLoopbackAddress()) {
                        String sAddr = addrs.get(i).getHostAddress();

                        if (sAddr.indexOf(':') < 0) {
                            // the address was ipv4
                            Log.i(TAG, "IPV4 Address :" +sAddr);
                            addressIPV4 = sAddr;
                        } else {
                            // the address was ipv6
                            int delimiter = sAddr.indexOf('%');

                            if (delimiter > 0 && i != 0) {

                                addressIPV6 = sAddr.substring(0, delimiter).toUpperCase();

                                Log.i(TAG, "IPV6 Address :" + addressIPV6);
                            }
                        }
                    }
                }
            }
        } catch (SocketException ignored) { }
    }

    private void getBatteryTemp() {

        // Code created with help of Stack Overflow question:
        // https://stackoverflow.com/questions/20771070/how-do-i-get-the-cpu-temperature
        // Question by Asaf:
        // https://stackoverflow.com/users/1225878/asaf
        // Answer by Jyoti-jk:
        // https://stackoverflow.com/users/8867002/jyoti-jk

        for (String location : locations) { // Temperature of battery
            try {
                RandomAccessFile reader = new RandomAccessFile(location, "r");
                String temperatureValue = reader.readLine();
                if (temperatureValue != null) {
                    String currentTemp = Double.toString(Double.parseDouble(temperatureValue) / 1000);
                    formattedTemperature = currentTemp + getResources().getString(R.string.degree_symbol_celsius);
                    Log.i(TAG, "Battery Temperature : " + formattedTemperature + " : " + location);

                    break; // breaks the for-each loop once it finds one valid temperature
                }
            } catch (IOException ignore) { }
        }
    }

    private void getCPUUsage() {

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        // Code created with help of Stack Overflow question:
        // https://stackoverflow.com/questions/3118234/get-memory-usage-in-android
        // Question by Badal:
        // https://stackoverflow.com/users/376287/badal
        // Answer by Szcoder:
        // https://stackoverflow.com/users/688747/szcoder

        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception ignore) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            double numerator = cpu2 - cpu1;
            double denominator = (cpu2 + idle2) - (cpu1 + idle1);

            double rawPercentage = (numerator / denominator) * 100.0;

            formattedCPUUsage = decimalFormat.format(rawPercentage) + getResources().getString(R.string.percent_symbol);

            Log.i(TAG, "CPU Usage: " + formattedCPUUsage);

        } catch (IOException e) {
            Log.w(TAG, "[IOException] File not found at /proc/stat");
        }
    }

    private void getMemoryUsage() {

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        // Code created with help of Stack Overflow question:
        // https://stackoverflow.com/questions/3170691/how-to-get-current-memory-usage-in-android
        // Question by Badal:
        // https://stackoverflow.com/users/376287/badal
        // Answer by Badal:
        // https://stackoverflow.com/users/376287/badal
        //  Possibly a site error for the same asker/answerer?

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = mi.availMem / 0x100000L;
        double totalMegs = mi.totalMem / 0x100000L;
        double usedMegs = totalMegs - availableMegs;

        double percentUsed = (usedMegs / totalMegs) * 100.0;

        formattedMemoryUsed = decimalFormat.format(percentUsed) + getResources().getString(R.string.percent_symbol);

        Log.i(TAG, "Memory Usage: " + formattedMemoryUsed);
    }

    private void updateUI() {

        IPV4Address.setText(addressIPV4);
        IPV6Address.setText(addressIPV6);

        totalApps.setText(sTotalApps);
        memoryUsage.setText(formattedMemoryUsed);
        deviceTemp.setText(formattedTemperature);
        deviceCPU.setText(formattedCPUUsage);
        blockedApps.setText(sBlockedApps);

        startTime.setText(formattedUpTime);
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
                intent  = new Intent(this, FirewallActivity.class);
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

        updateUI();

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