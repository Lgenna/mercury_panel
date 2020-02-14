package android.b.networkingapplication2;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import database.FirewallBaseHelper;

import java.util.ArrayList;

import static android.b.networkingapplication2.OverviewActivity.PREFS_NAME;
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

        SharedPreferences FirewallPrefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        boolean appStatus = FirewallPrefs.getBoolean(mApplications.get(position).getProcessName(), false);

            //Boolean.parseBoolean(mApplications.get(position).getStatus())
        holder.status.setChecked(appStatus);
        holder.prettyPicture.setImageDrawable(mApplications.get(position).getPicture());
        holder.status.setText(mApplications.get(position).getApplication());
        holder.status.setOnClickListener(v -> {

            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

            if(holder.status.isChecked()) {

                Log.i(TAG, "Changed state of " + mApplications.get(position).getApplication() + " to true");
                editor.putBoolean(mApplications.get(position).getProcessName(), true);
            } else {

                Log.i(TAG, "Changed state of " + mApplications.get(position).getApplication() + " to false");
                editor.putBoolean(mApplications.get(position).getProcessName(), false);
            }
            editor.apply();

//            FirewallBaseHelper database = new FirewallBaseHelper(context);
//
//            Cursor databaseRes = database.getAllData();
//
//            if (databaseRes.getCount() > 0) {
//
//                if(holder.status.isChecked()) {
//
//                    while (databaseRes.moveToNext()) {
//                        if (databaseRes.getString(position).equals(mApplications.get(position).getProcessName())) {
//                            Log.i(TAG, "Found " + mApplications.get(position).getApplication() + " in the database");
//                            holder.status.setChecked(true);
//                        } else {
//                            Log.i(TAG, "Adding " + mApplications.get(position).getApplication() + " to the database");
//                            database.insertData(mApplications.get(position).getProcessName());
//                        }
//                    }
//
//
//                } else {
//
//                    databaseRes.moveToPosition(position);
//
//                    String databaseIndex = databaseRes.getString(0);
//
//                    Log.w(TAG, "Removing ID #" + databaseIndex + " from the database: " + databaseRes.getString(1));
//                    database.deleteData(databaseIndex);
//                }
//            }
        });
    }

//            Toast.makeText(context, "Removed: " + (mBlockLists.get(position).getDomain()), Toast.LENGTH_SHORT).show();
//
//            BlockListBaseHelper database = new BlockListBaseHelper(context);
//
//            // grab everything in the database
//            Cursor databaseRes = database.getAllData();
//
//            // as long as there's more than zero values, well, not sure how you are deleting anything
//            if (databaseRes.getCount() != 0) {
//
//                // move to the "position" of deletion
//                databaseRes.moveToPosition(position);
//
//                // get the id of that position
//                String databaseIndex = databaseRes.getString(0);
//
//                // remove that id, and state what the value was
//                Log.w(TAG, "Removing ID #" + databaseIndex + " from the database: " + databaseRes.getString(1));
//                database.deleteData(databaseIndex);
//            }
//
//            // remove the value also from the array list, this ones easier
//            Log.w(TAG, "Removing position " + position + " from the ArrayList: " + mBlockLists.get(position).getDomain());
//            mBlockLists.remove(position);
//
//            // notify everything
//            notifyItemRemoved(position);
//            notifyItemRangeRemoved(position, getItemCount());
//            notifyDataSetChanged();
//        });
//    }

    @Override
    public int getItemCount() {
        return mApplications.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
//        TextView application;
        Switch status;
        ImageView prettyPicture;
//        ImageButton removeBlocklist;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
//            application = itemView.findViewById(R.id.fire)
//            domain = itemView.findViewById(R.id.item_text);
            status = itemView.findViewById(R.id.application_name);
            prettyPicture = itemView.findViewById(R.id.application_image);
//            removeBlocklist = itemView.findViewById(R.id.remove_button);

        }
    }
}
