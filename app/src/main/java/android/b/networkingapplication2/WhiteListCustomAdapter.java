package android.b.networkingapplication2;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import database.WhiteListBaseHelper;

public class WhiteListCustomAdapter extends RecyclerView.Adapter<WhiteListCustomAdapter.MyViewHolder> {

    private ArrayList<BlackWhiteList> mWhiteLists;
    private static final String TAG = "WhiteListCustomAdapter";
    private Context context;

    public WhiteListCustomAdapter(Context context, ArrayList<BlackWhiteList> mWhiteLists) {
        this.context = context;
        this.mWhiteLists = mWhiteLists;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_blank, parent, false);
        // set the view's size, margins, padding and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        holder.domain.setText(mWhiteLists.get(position).getDomain());
        holder.removeWhiteList.setOnClickListener(v -> {

            // Throw some toast at the user stating what happened
            Toast.makeText(context, "Removed: " + (mWhiteLists.get(position).getDomain()), Toast.LENGTH_SHORT).show();

            WhiteListBaseHelper database = new WhiteListBaseHelper(context);

            // grab everything in the database
            Cursor databaseRes = database.getAllData();

            // as long as there's more than zero values, well, not sure how you are deleting anyything

            if (databaseRes.getCount() != 1) {

                // move to the "position" of deletion
                databaseRes.moveToPosition(position);

                // get the id of that position
                String databaseIndex = databaseRes.getString(0);

                // remove that id, and state what the value was
                Log.w(TAG, "Removing ID #" + databaseIndex + " from the database: " + databaseRes.getString(1));
                database.deleteData(databaseIndex);
            }

            // remove the value also from the array list, this ones easier
            Log.w(TAG, "Removing position " + position + " from the ArrayList: " + mWhiteLists.get(position).getDomain());
            mWhiteLists.remove(position);

            // notify everything
            notifyItemRemoved(position);
            notifyItemRangeRemoved(position, getItemCount());
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return mWhiteLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView domain;
        ImageButton removeWhiteList;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            domain = itemView.findViewById(R.id.item_text);
            removeWhiteList = itemView.findViewById(R.id.remove_button);

        }
    }
}
