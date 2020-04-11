package android.b.networkingapplication2;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

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

        SharedPreferences FirewallPrefs = context.getSharedPreferences(PREFS_FIREWALL, MODE_PRIVATE);

        boolean appStatus = FirewallPrefs.getBoolean(mApplications.get(position).getProcessName(), false);

        holder.status.setChecked(appStatus);
        holder.prettyPicture.setImageDrawable(mApplications.get(position).getPicture());
        holder.status.setText(mApplications.get(position).getApplication());
        holder.status.setOnClickListener(v -> {

            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_FIREWALL, MODE_PRIVATE).edit();

            if(holder.status.isChecked()) {

                Log.i(TAG, "Changed state of " + mApplications.get(position).getApplication() + " to true");
                editor.putBoolean(mApplications.get(position).getProcessName(), true);

            } else {
                Log.i(TAG, "Changed state of " + mApplications.get(position).getApplication() + " to false");
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
        ImageView prettyPicture;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            status = itemView.findViewById(R.id.application_name);
            prettyPicture = itemView.findViewById(R.id.application_image);

        }
    }
}
