package android.b.networkingapplication2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class DNSActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "NetworkingApp";

    private String sDNS_name_1, sDNS_name_2, sDNS_name_3, sDNS_name_4;
    private Boolean sDNS_stat_1, sDNS_stat_2, sDNS_stat_3, sDNS_stat_4;
    private Switch dns_title_1, dns_title_2, dns_title_3, dns_title_4;
    private TextView dns_text_1, dns_text_2, dns_text_3, dns_text_4;

    private String prefName, prefNameV, prefStatus;
    private Boolean prefStatusV;
    private int caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dns);
        DNSFragment.newInstance();

        SharedPreferences DNSPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sDNS_name_1 = DNSPrefs.getString("dns_name_1", "");
        sDNS_stat_1 = DNSPrefs.getBoolean("dns_stat_1", false);
        sDNS_name_2 = DNSPrefs.getString("dns_name_2", "");
        sDNS_stat_2 = DNSPrefs.getBoolean("dns_stat_2", false);
        sDNS_name_3 = DNSPrefs.getString("dns_name_3", "");
        sDNS_stat_3 = DNSPrefs.getBoolean("dns_stat_3", false);
        sDNS_name_4 = DNSPrefs.getString("dns_name_4", "");
        sDNS_stat_4 = DNSPrefs.getBoolean("dns_stat_4", false);

        dns_title_1 = findViewById(R.id.tdns_title_1);
        dns_title_2 = findViewById(R.id.tdns_title_2);
        dns_title_3 = findViewById(R.id.tdns_title_3);
        dns_title_4 = findViewById(R.id.tdns_title_4);

        dns_text_1 = findViewById(R.id.dns_textbox_1);
        dns_text_2 = findViewById(R.id.dns_textbox_2);
        dns_text_3 = findViewById(R.id.dns_textbox_3);
        dns_text_4 = findViewById(R.id.dns_textbox_4);


        if (sDNS_name_1 != null && !sDNS_name_1.equals("")) {
            dns_text_1.setText(sDNS_name_1);
            dns_title_1.setChecked(sDNS_stat_1);
        }

        if (sDNS_name_2 != null && !sDNS_name_2.equals("")) {
            dns_text_2.setText(sDNS_name_2);
            dns_title_2.setChecked(sDNS_stat_2);
        }

        if (sDNS_name_3 != null && !sDNS_name_3.equals("")) {
            dns_text_3.setText(sDNS_name_3);
            dns_title_3.setChecked(sDNS_stat_3);
        }

        if (sDNS_name_4 != null && !sDNS_name_4.equals("")) {
            dns_text_4.setText(sDNS_name_4);
            dns_title_4.setChecked(sDNS_stat_4);
        }

        dns_title_1.setOnClickListener(view -> {

            prefName = "dns_name_1";
            prefNameV = dns_text_1.getText().toString();
            prefStatus = "dns_stat_1";
            prefStatusV = dns_title_1.isChecked();
            caller = 1;

            dns_title_1.setChecked(dnsToaster());
        });

        dns_title_2.setOnClickListener(view -> {

            prefName = "dns_name_2";
            prefNameV = dns_text_2.getText().toString();
            prefStatus = "dns_stat_2";
            prefStatusV = dns_title_2.isChecked();
            caller = 2;

            dns_title_2.setChecked(dnsToaster());
        });

        dns_title_3.setOnClickListener(view -> {

            prefName = "dns_name_3";
            prefNameV = dns_text_3.getText().toString();
            prefStatus = "dns_stat_3";
            prefStatusV = dns_title_3.isChecked();
            caller = 3;

            dns_title_3.setChecked(dnsToaster());
        });

        dns_title_4.setOnClickListener(view -> {

            prefName = "dns_name_4";
            prefNameV = dns_text_4.getText().toString();
            prefStatus = "dns_stat_4";
            prefStatusV = dns_title_4.isChecked();
            caller = 4;

            dns_title_4.setChecked(dnsToaster());
        });

        dns_text_1.setOnClickListener(view -> {
            if (dns_title_1.isChecked() && !dns_text_1.getText().toString().equals("")) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

                dns_title_1.setChecked(false);

                editor.putBoolean("dns_name_1", false);
                editor.apply();

                Toast.makeText(this, "Disabled \"" + sDNS_name_1 + "\"", Toast.LENGTH_SHORT).show();
            }
        });

        dns_text_2.setOnClickListener(view -> {
            if (dns_title_2.isChecked() && !dns_text_2.getText().toString().equals("")) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

                dns_title_2.setChecked(false);

                editor.putBoolean("dns_name_1", false);
                editor.apply();

                Toast.makeText(this, "Disabled \"" + sDNS_name_2 + "\"", Toast.LENGTH_SHORT).show();
            }
        });

        dns_text_3.setOnClickListener(view -> {
            if (dns_title_3.isChecked() && !dns_text_3.getText().toString().equals("")) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

                dns_title_3.setChecked(false);

                editor.putBoolean("dns_name_3", false);
                editor.apply();

                Toast.makeText(this, "Disabled \"" + sDNS_name_3 + "\"", Toast.LENGTH_SHORT).show();
            }
        });

        dns_text_4.setOnClickListener(view -> {
            if (dns_title_4.isChecked() && !dns_text_4.getText().toString().equals("")) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

                dns_title_4.setChecked(false);

                editor.putBoolean("dns_name_4", false);
                editor.apply();

                Toast.makeText(this, "Disabled \"" + sDNS_name_4 + "\"", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private Boolean dnsToaster() {

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();


        if (prefStatusV) { // User turned on the DNS

            if (!prefNameV.equals("")) {

                editor.putString(prefName, prefNameV);
                editor.putBoolean(prefStatus, prefStatusV);
                editor.apply();

                Toast.makeText(this, "Set \"" + prefNameV + "\" as DNS " + caller + "!", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(this, "DNS server can not be empty.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else { // User turned off the DNS
            if (prefNameV.equals("")) {

                editor.clear();
                editor.putBoolean(prefStatus, prefStatusV);
                editor.apply();

                Toast.makeText(this, "Deleted DNS " + caller, Toast.LENGTH_SHORT).show();
                return false;
            } else {
                editor.putBoolean(prefStatus, prefStatusV);
                editor.apply();

                Toast.makeText(this, "Disabled \"" + prefNameV + "\"", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
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
