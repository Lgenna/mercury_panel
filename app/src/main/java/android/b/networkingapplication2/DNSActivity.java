package android.b.networkingapplication2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class DNSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_empty);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragment_container, DNSFragment.newInstance());
        fragmentTransaction.commit();

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
