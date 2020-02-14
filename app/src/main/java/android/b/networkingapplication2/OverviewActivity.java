package android.b.networkingapplication2;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OverviewActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "NetworkingApp";

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
    private TextView deviceTemp, deviceCPU, memoryUsage, IPV4Address, IPV6Address, startTime;
    private Thread uiUpdater;
    private LinearLayout domainBlockerBox, DNSBox, FirewallBox, VPNBox;
    private static final String TAG = "OverviewActivity";
    private Intent intent;
    private String formattedStartupTime, formattedStartupDate;


    long startupTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;

        setContentView(R.layout.activity_overview);
        OverviewFragment.newInstance();

        deviceTemp = findViewById(R.id.cpu_temperature_status);
        memoryUsage = findViewById(R.id.memory_usage_percent);
        deviceCPU = findViewById(R.id.cpu_usage_percent);
        IPV4Address = findViewById(R.id.ipv4_address);
        IPV6Address = findViewById(R.id.ipv6_address);
        startTime = findViewById(R.id.up_time_data);


        domainBlockerBox = findViewById(R.id.domain_blocker_info_box);
        DNSBox = findViewById(R.id.dns_info_box);
        FirewallBox = findViewById(R.id.firewall_info_box);
        VPNBox = findViewById(R.id.vpn_info_box);

        // get time when OnCreate is called
        startupTime = new Date().getTime();


        domainBlockerBox.setOnClickListener(v -> {
            intent = new Intent(this, DomainBlockerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        });

        DNSBox.setOnClickListener(v -> {
            intent = new Intent(this, DNSActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        });

        FirewallBox.setOnClickListener(v -> {
            intent = new Intent(this, FirewallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        });

        VPNBox.setOnClickListener(v -> {
            intent = new Intent(this, VPNActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        });

        updateUI();

        uiUpdater = new Thread() {
            @Override
            public void run() {

                try {
                    while (!mFinished) {
                        Thread.sleep(10000);
                        runOnUiThread(() -> updateUI());
                        synchronized (mPauseLock) {
                            while (mPaused) {
                                try {
                                    mPauseLock.wait();
                                } catch (InterruptedException e) {
                                  // Panic x2
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    // panic.
                }
            }
        };

        uiUpdater.start();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void updateUI() {

        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        try { // IP Address
            // TODO this is a temporary solution to find the ip addresses of the device
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();

                        if (sAddr.indexOf(':') < 0) {
                            // the address was ipv4
                            Log.i(TAG, "IPV4 Address :" +sAddr);
                            IPV4Address.setText(sAddr);
                        } else {
                            // the address was ipv6
                            int delimiter = sAddr.indexOf('%');

                            if (delimiter > 0) {

                                String addressIPV6 = sAddr.substring(0, delimiter).toUpperCase();

                                Log.i(TAG, "IPV6 Address :" + addressIPV6);

                                IPV6Address.setText(addressIPV6);
                            }
                        }
                    }
                }
            }
        } catch (SocketException ignored) { }

        for (String location : locations) { // Temperature of battery
            // TODO this is a temporary solution to find the battery temperature
            try {
                RandomAccessFile reader = new RandomAccessFile(location, "r");
                String temperatureValue = reader.readLine();
                if (temperatureValue != null) {
                    String currentTemp = Double.toString(Double.parseDouble(temperatureValue) / 1000);
                    String formattedTemperature = currentTemp + getResources().getString(R.string.degree_symbol_celsius);
                    Log.i(TAG, "Valid location : " + formattedTemperature + " : " + location);
                    deviceTemp.setText(formattedTemperature);
                    break; // breaks the for-each loop once it finds one valid temperature
                }
            } catch (IOException e) {
                Log.w(TAG, "[IOException] File not found at " + location);
            }
        }

        try {
            // CPU % Usage

            // TODO this is a temporary solution to find the CPU usage
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

            String formattedCPUUsage = decimalFormat.format(rawPercentage) + getResources().getString(R.string.percent_symbol);

            Log.i(TAG, "CPU Usage: " + formattedCPUUsage);
            deviceCPU.setText(formattedCPUUsage);
        } catch (IOException e) {
            Log.w(TAG, "[IOException] File not found at /proc/stat");
        }

        // Memory % Usage

        // TODO this is a temporary solution to find the memory usage

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = mi.availMem / 0x100000L;
        double totalMegs = mi.totalMem / 0x100000L;
        double usedMegs = totalMegs - availableMegs;

        double percentUsed = (usedMegs / totalMegs) * 100.0;

        String formattedMemoryUsed = decimalFormat.format(percentUsed) + getResources().getString(R.string.percent_symbol);

        Log.i(TAG, "Memory Usage: " + formattedMemoryUsed);
        memoryUsage.setText(formattedMemoryUsed);



        // System Uptime - get current time

        long currentTime = new Date().getTime();

        long timeDifference = currentTime - startupTime;

        int currentTimeDifference = (int)timeDifference / 1000;

        String formattedUpTime = decimalFormat.format(currentTimeDifference) + " seconds";

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
            case R.id.action_dns_servers:
                intent = new Intent(this, DNSActivity.class);
                break;
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
