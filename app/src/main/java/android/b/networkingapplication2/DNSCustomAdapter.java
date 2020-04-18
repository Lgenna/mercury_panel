package android.b.networkingapplication2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import static android.b.networkingapplication2.OverviewActivity.PREFS_DNS;
import static android.content.Context.MODE_PRIVATE;

public class DNSCustomAdapter extends RecyclerView.Adapter<DNSCustomAdapter.MyViewHolder> {

    private ArrayList<DNS> mDNSEntries;
    private static final String TAG = "DNSCustomAdapter";

    private Context context;

    public DNSCustomAdapter(Context context, ArrayList<DNS> mDNSEntries) {
        this.context = context;
        this.mDNSEntries = mDNSEntries;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dns, parent, false);
        // set the view's size, margins, padding and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        SharedPreferences DNSPrefs = context.getSharedPreferences(PREFS_DNS, MODE_PRIVATE);

        String DNSTextbox = DNSPrefs.getString("s" + mDNSEntries.get(position).getTitle(), "");
        boolean DNSStatus = DNSPrefs.getBoolean("b" + mDNSEntries.get(position).getTitle(), false);

        holder.status.setText(mDNSEntries.get(position).getTitle());
        holder.textbox.setText(DNSTextbox);
        holder.status.setChecked(DNSStatus);

        holder.status.setOnClickListener(v -> {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_DNS, MODE_PRIVATE).edit();

            String DNSEntryTitle = mDNSEntries.get(position).getTitle();
            String entereredText = holder.textbox.getText().toString();

            // Check what was entered
            if (Patterns.IP_ADDRESS.matcher(entereredText).matches()) {

                if (holder.status.isChecked()) {

                    if (!entereredText.equals("")) {
                        Toast.makeText(DNSCustomAdapter.this.context, "Set \"" + entereredText + "\" as " + DNSEntryTitle + "!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Changed state of " + DNSEntryTitle + " to enabled");
                        editor.putString("s" + DNSEntryTitle, entereredText);
                        editor.putBoolean("b" + DNSEntryTitle, true);

                    } else {
                        Toast.makeText(DNSCustomAdapter.this.context, "DNS server can not be empty.", Toast.LENGTH_SHORT).show();
                        holder.status.setChecked(false);
                    }
                } else {
                    if (!entereredText.equals("")) {
                        Toast.makeText(DNSCustomAdapter.this.context, "Disabled " + DNSEntryTitle, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Changed state of " + DNSEntryTitle + " to disabled");
                        editor.putBoolean("b" + DNSEntryTitle, false);
                    } else {
                        editor.putString("s" + DNSEntryTitle, "");
                        editor.putBoolean("b" + DNSEntryTitle, false);
                        Toast.makeText(DNSCustomAdapter.this.context, "Removed " + DNSEntryTitle, Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Changed state of " + DNSEntryTitle + " to removed");
                    }
                }
                editor.apply();
            } else {
                holder.status.setChecked(false);
                Toast.makeText(DNSCustomAdapter.this.context, "Invalid input for " + DNSEntryTitle, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Invalid input for " + DNSEntryTitle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDNSEntries.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        Switch status;
        EditText textbox;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            status = itemView.findViewById(R.id.title);
            textbox = itemView.findViewById(R.id.textbox);

        }
    }
}
