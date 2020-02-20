package android.b.networkingapplication2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//import static android.b.networkingapplication2.OverviewActivity.BlockedApps;
import static android.b.networkingapplication2.OverviewActivity.PREFS_DNS;
import static android.b.networkingapplication2.OverviewActivity.PREFS_FIREWALL;
import static android.b.networkingapplication2.OverviewActivity.PREFS_GENERAL;
import static android.b.networkingapplication2.OverviewActivity.PREFS_VPN;

public class VPNActivity extends AppCompatActivity {

    Switch monitoringStatus;
    LinearLayout VPNFileBox, VPNSettingsBox;
    ImageButton VPNURLAdd, VPNDefaultAdd, ChangeVPNServer;
    TextView VPNURL;
    private static final String TAG = "VPNActivity";

    ArrayList<String> blockedApps;

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

        final SharedPreferences prefs = getSharedPreferences(PREFS_VPN, MODE_PRIVATE);

        VPNFileBox = findViewById(R.id.vpn_file_box);
        VPNSettingsBox = findViewById(R.id.vpn_settings_box);
        monitoringStatus = findViewById(R.id.toggle_vpn);
        VPNURLAdd = findViewById(R.id.vpn_url_add);
        VPNDefaultAdd = findViewById(R.id.vpn_default_add);
        ChangeVPNServer = findViewById(R.id.change_vpn_server_button);
        VPNURL = findViewById(R.id.vpn_url);

        updateViews();

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE).edit();

        ChangeVPNServer.setOnClickListener(v -> {
//            isVPNServerDefault = false;
//            selectedVPNServer = false;
            editor.putBoolean("isVPNServerDefault", false);
            editor.putBoolean("selectedVPNServer", false);
            editor.apply();
            updateViews();
        });

        VPNURLAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Currently not implemented, using default", Toast.LENGTH_LONG).show();
            editor.putBoolean("isVPNServerDefault", true);
            editor.putBoolean("selectedVPNServer", true);
            editor.apply();
            updateViews();
        });

        VPNDefaultAdd.setOnClickListener(v -> {
            Toast.makeText(this, "Setting VPN to default", Toast.LENGTH_SHORT).show();
            editor.putBoolean("isVPNServerDefault", true);
            editor.putBoolean("selectedVPNServer", true);
            editor.apply();
            updateViews();

        });

//        String[] appPackages = {
//                "com.android.chrome",
//                "com.example.a.missing.app"};

//        ArrayList<String> apps = new ArrayList<>();




        final Set<String> packageSet = new HashSet<>(getBlockedApps()); // This will crash if getBlockedApps == null

        final Set<String> dnsServersSet = new HashSet<>(getDnsServers());

        SharedPreferences VPNPrefs = getSharedPreferences(PREFS_GENERAL, MODE_PRIVATE);

        monitoringStatus.setOnClickListener(v -> {
            if (monitoringStatus.isChecked()) {
                if (VPNPrefs.getBoolean("isVPNServerDefault", false)) {
                    prefs.edit()
                            .putString(VPNActivity.Prefs.SERVER_ADDRESS, "10.120.86.219")
                            .putInt(VPNActivity.Prefs.SERVER_PORT, 8000)
                            .putString(VPNActivity.Prefs.SHARED_SECRET, "test")
                            .putStringSet(VPNActivity.Prefs.PACKAGES, packageSet)
                            .putStringSet(VPNActivity.Prefs.DNSSERVERS, dnsServersSet)
                            .apply();
                    Intent intent = VpnService.prepare(getBaseContext());
                    if (intent != null) {
                        VPNActivity.this.startActivityForResult(intent, 0);
                    } else {
                        VPNActivity.this.onActivityResult(0, RESULT_OK, null);
                    }
                } else {
                    Log.i(TAG, "VPN turned off");
                    VPNActivity.this.startService(VPNActivity.this.getServiceIntent().setAction(ToyVpnService.ACTION_DISCONNECT));
                }
            } else {
                // TODO URL based vpn server, but for now, we scream.
            }
        });

    }


    private ArrayList<String> getBlockedApps() {

        SharedPreferences FirewallPrefs = getSharedPreferences(PREFS_FIREWALL, MODE_PRIVATE);

        ArrayList<String> BlockedApps = new ArrayList<>();

        Map<String, ?> keys = FirewallPrefs.getAll();

        /**
         * Add a dud item, otherwise if the user doesn't add anything, it blocks ALL network
         *  traffic, even though the list given is empty. This is because with the Crude Approach
         *  it tells specific apps to be killed by the VPN whereas all the others are told to
         *  ignore it. If no apps are set, all apps are sent down the lonely VPN road.
         */
        BlockedApps.add("com.example.a.missing.app");

        for(Map.Entry<String, ?> element : keys.entrySet()){
            BlockedApps.add(element.getKey());
        }

        return BlockedApps;
    }

    private ArrayList<String> getDnsServers() {
        SharedPreferences DNSPrefs = getSharedPreferences(PREFS_DNS, MODE_PRIVATE);

        ArrayList<String> DNSServers = new ArrayList<>();

        Map<String, ?> keys = DNSPrefs.getAll();

        // TODO check for String and boolean pairs of DNS values ex. if DNS1 has a value, but not enabled

        for(Map.Entry<String, ?> element : keys.entrySet()){
            if (element.getKey().startsWith("s")) {
                DNSServers.add(element.getKey());
                Log.i(TAG, "Adding " + element.getKey() + " to DNSServers");
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
            VPNURL.setEnabled(true);
            monitoringStatus.setClickable(false);
            ChangeVPNServer.setClickable(false);
            VPNURLAdd.setClickable(true);
            VPNDefaultAdd.setClickable(true);
        } else {
            VPNFileBox.setForeground(getDrawable(R.color.transparent));
            VPNSettingsBox.setForeground(getDrawable(R.color.transparentClear));
            VPNURL.setEnabled(false);
            monitoringStatus.setClickable(true);
            ChangeVPNServer.setClickable(true);
            VPNURLAdd.setClickable(false);
            VPNDefaultAdd.setClickable(false);
        }
    }



    @Override
    protected void onActivityResult(int request, int result, Intent data) {
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
}
