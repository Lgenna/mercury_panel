package android.b.networkingapplication2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static android.b.networkingapplication2.OverviewActivity.PREFS_GENERAL;

public class FirewallActivity extends AppCompatActivity {

    private RecyclerView mFirewallRecyclerView;
    private ArrayList<ApplicationInfo> installedApps;
    private Thread uiUpdater;
    public static ArrayList<Firewall> mApplications;

    private static final String TAG = "FirewallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firewall);
        FirewallFragment.newInstance();

        SharedPreferences GeneralPrefs = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE);

        boolean closedDangerZone = GeneralPrefs.getBoolean("dangerZoneClosed", false);

        mFirewallRecyclerView = findViewById(R.id.application_list);

        LinearLayoutManager firewalllinearLayoutManager = new LinearLayoutManager(getBaseContext());
        mFirewallRecyclerView.setLayoutManager(firewalllinearLayoutManager);

        ImageButton removeFirewallDangerZone = findViewById(R.id.close_warning);
        LinearLayout firewallDangerZone = findViewById(R.id.danger_zone_firewall);

        if (closedDangerZone) {
            firewallDangerZone.setVisibility(View.GONE);
        } else {
           removeFirewallDangerZone.setOnClickListener(v -> {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE).edit();
                editor.putBoolean("dangerZoneClosed", true);
                editor.apply();

                firewallDangerZone.setVisibility(View.GONE);
            });
        }

        buildApplicationList();

    }

    public void buildApplicationList() {
        installedApps = OverviewActivity.installedApps;

        mApplications = new ArrayList<>();

        if (installedApps != null && installedApps.size() != 0) {
            for (ApplicationInfo element : installedApps) {
                PackageManager pm = getPackageManager();

                Firewall FirewallItem = new Firewall();
                FirewallItem.setProcessName(element.processName);
                FirewallItem.setUid(String.valueOf(element.uid));
                FirewallItem.setPicture(pm.getApplicationIcon(element));
                FirewallItem.setApplication(String.valueOf(pm.getApplicationLabel(element)));
                FirewallItem.setStatus(false);

                mApplications.add(FirewallItem);
            }
        }

        FirewallCustomAdapter firewallcustomAdapter = new FirewallCustomAdapter(getBaseContext(), mApplications);
        mFirewallRecyclerView.setAdapter(firewallcustomAdapter);

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