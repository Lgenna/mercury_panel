package android.b.networkingapplication2;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import database.FirewallBaseHelper;
import database.QueryLogBaseHelper;

import static android.b.networkingapplication2.OverviewActivity.PREFS_NAME;
import static android.view.View.GONE;

public class FirewallActivity extends AppCompatActivity {

    FirewallBaseHelper myFireDb;
    RecyclerView mFirewallRecyclerView;

    Thread appFetcher;
    ArrayList<AppInfo> storedList;
    ArrayList<ApplicationInfo> installedApps;

    private static final String TAG = "FirewallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firewall);
        FirewallFragment.newInstance();

        SharedPreferences FirewallPrefs = getSharedPreferences(OverviewActivity.PREFS_NAME, MODE_PRIVATE);

        boolean closedDangerZone = FirewallPrefs.getBoolean("dangerZoneClosed", false);

        myFireDb = new FirewallBaseHelper(getBaseContext());

        mFirewallRecyclerView = findViewById(R.id.application_list);

        LinearLayoutManager firewalllinearLayoutManager = new LinearLayoutManager(getBaseContext());
        mFirewallRecyclerView.setLayoutManager(firewalllinearLayoutManager);

        appFetcher = new Thread() {
            @Override
            public void run() {
                getApplications();
            }
        };

        appFetcher.start();

        ImageButton removeFirewallDangerZone = findViewById(R.id.close_warning);
        LinearLayout firewallDangerZone = findViewById(R.id.danger_zone_firewall);

        if (closedDangerZone) {
            firewallDangerZone.setVisibility(GONE);
        } else {
           removeFirewallDangerZone.setOnClickListener(v -> {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putBoolean("dangerZoneClosed", true);
                editor.apply();

                firewallDangerZone.setVisibility(GONE);
            });
        }

    }

    private void getApplications() {

        Log.i(TAG, "Started getApplications...");

        installedApps = new ArrayList<>();

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        // Code created with the help of Stack Overflow question
        // https://stackoverflow.com/questions/6165023/get-list-of-installed-android-applications
        // Question by user577732:
        // https://stackoverflow.com/users/577732/user577732
        // Answer by Kevin Coppock:
        // https://stackoverflow.com/users/321697/kevin-coppock

        for (ApplicationInfo app : apps) {
            // updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                installedApps.add(app);

            // non-updated system app
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                // do nothing.
            // user-installed app

            } else {
                installedApps.add(app);
            }
        }

//        runOnUiThread(() -> updateUI());
        runOnUiThread(this :: updateUI); // somehow better?

        Log.i(TAG, "Finished getApplications.");

    }

    public void updateUI() {

        Log.i(TAG, "Updating the user interface...");

        ArrayList<Firewall> mApplications = new ArrayList<>();

        if (installedApps != null || installedApps.size() != 0) {
            for (ApplicationInfo element : installedApps) {

                PackageManager pm = getPackageManager();
                Firewall FirewallItem = new Firewall();

                FirewallItem.setProcessName(element.processName);
                FirewallItem.setPicture(pm.getApplicationIcon(element));
                FirewallItem.setApplication(pm.getApplicationLabel(element) + "");
                FirewallItem.setStatus(false);

                mApplications.add(FirewallItem);
            }
        }

        FirewallCustomAdapter firewallcustomAdapter = new FirewallCustomAdapter(getBaseContext(), mApplications);
        mFirewallRecyclerView.setAdapter(firewallcustomAdapter);

        Log.i(TAG, "Finished updating the user interface...");

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
            case R.id.action_dns_servers:
                intent = new Intent(this, DNSActivity.class);
                break;
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


//    private final class GetApplicationsTask extends AsyncTask<Void, Void, String> {
//
//        ArrayList<ApplicationInfo> installedApps;
//
//        protected String doInBackground(Void... params) {
//            installedApps = new ArrayList<>();
//
//            PackageManager pm = getPackageManager();
//            List<ApplicationInfo> apps = pm.getInstalledApplications(0);
//
//            for (ApplicationInfo app : apps) {
//                if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
//                    installedApps.add(app);
//
//                    System.out.println("Added " + app.processName + " the Firewall Database");
//                } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
//                } else {
//                    installedApps.add(app);
//                }
//            }
//            return "Executed";
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            runOnUiThread(() -> updateUI());
//        }
//    }
}