package android.b.networkingapplication2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class VPNActivity extends AppCompatActivity {

    public interface Prefs {
        String NAME = "connection";
        String SERVER_ADDRESS = "server.address";
        String SERVER_PORT = "server.port";
        String SHARED_SECRET = "shared.secret";
    }


    Switch monitoringStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn);
//        setContentView(R.layout.activity_empty);
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//        fragmentTransaction.add(R.id.fragment_container, VPNFragment.newInstance());
//        fragmentTransaction.commit();

        final SharedPreferences prefs = getSharedPreferences(VPNActivity.Prefs.NAME, MODE_PRIVATE);


        findViewById(R.id.toggle_vpn).setOnClickListener(v -> {
            monitoringStatus = findViewById(R.id.toggle_vpn);

            if (monitoringStatus.isChecked()) {
                prefs.edit()
                        .putString(VPNActivity.Prefs.SERVER_ADDRESS, "192.168.91.90")
                        .putInt(VPNActivity.Prefs.SERVER_PORT, 8000)
                        .putString(VPNActivity.Prefs.SHARED_SECRET, "test")
                        .apply();
                Intent intent = VpnService.prepare(getBaseContext());
                if (intent != null) {
//                    VPNFragment.this.startActivityForResult(intent, 0);
                    VPNActivity.this.startActivityForResult(intent, 0);
                } else {
//                    VPNFragment.this.onActivityResult(0, RESULT_OK, null);
                    VPNActivity.this.onActivityResult(0, RESULT_OK, null);
                }
            } else {
                VPNActivity.this.startService(VPNActivity.this.getServiceIntent().setAction(ToyVpnService.ACTION_DISCONNECT));
            }
        });

    }


    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            startService(getServiceIntent().setAction(ToyVpnService.ACTION_CONNECT));
        }
    }

    private Intent getServiceIntent() {
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
                intent  = new Intent(this, FirewallActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        this.startActivity(intent);

        return true;
    }
}
