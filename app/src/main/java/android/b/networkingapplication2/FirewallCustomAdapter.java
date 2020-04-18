package android.b.networkingapplication2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import static android.b.networkingapplication2.OverviewActivity.PREFS_FIREWALL;
import static android.content.Context.MODE_PRIVATE;

public class FirewallCustomAdapter extends RecyclerView.Adapter<FirewallCustomAdapter.MyViewHolder> {

    private ArrayList<Firewall> mApplications;
    private static final String TAG = "FirewallCustomAdapter";

    private Context context;

    public FirewallCustomAdapter(Context context, ArrayList<Firewall> mApplications) {
        this.context = context;
        this.mApplications = mApplications;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_firewall_application, parent, false);
        // set the view's size, margins, padding and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        Firewall currentApp = mApplications.get(position);

        SharedPreferences FirewallPrefs = context.getSharedPreferences(PREFS_FIREWALL, MODE_PRIVATE);

        String processName = currentApp.getProcessName();
        boolean appStatus = FirewallPrefs.getBoolean(processName, false);

        holder.processName.setText(processName);
        holder.status.setChecked(appStatus);
        holder.uid.setText(currentApp.getUid());
        holder.prettyPicture.setImageDrawable(currentApp.getPicture());
        holder.status.setText(currentApp.getApplication());
        holder.status.setOnClickListener(v -> {

            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_FIREWALL, MODE_PRIVATE).edit();

            if(holder.status.isChecked()) {

                Log.i(TAG, "Changed state of " + currentApp.getApplication() + " to true");
                editor.putBoolean(mApplications.get(position).getProcessName(), true);

            } else {
                Log.i(TAG, "Changed state of " + currentApp.getApplication() + " to false");
                editor.remove(mApplications.get(position).getProcessName());
            }

            editor.apply();
        });
    }

    @Override
    public int getItemCount() {
        return mApplications.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        Switch status;
        TextView uid;
        TextView processName;
        ImageView prettyPicture;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            uid = itemView.findViewById(R.id.application_uid);
            status = itemView.findViewById(R.id.application_display_name);
            processName = itemView.findViewById(R.id.process_name);
            prettyPicture = itemView.findViewById(R.id.application_image);

        }
    }
}
