package android.b.networkingapplication2;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import database.BlockListBaseHelper;

public class BlockListCustomAdapter extends RecyclerView.Adapter<BlockListCustomAdapter.MyViewHolder> {

    private ArrayList<BlockList> mBlockLists;
    private static final String TAG = "BlockListCustomAdapter";
    private Context context;

    public BlockListCustomAdapter(Context context, ArrayList<BlockList> mBlockLists) {
        this.context = context;
        this.mBlockLists = mBlockLists;
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

        holder.domain.setText(mBlockLists.get(position).getDomain());
//        holder.status.setChecked(mBlockLists.get(position).getStatus());
        holder.removeBlocklist.setOnClickListener(v -> {

            // Throw some toast at the user stating what happened
            Toast.makeText(context, "Removed: " + (mBlockLists.get(position).getDomain()), Toast.LENGTH_SHORT).show();

            BlockListBaseHelper database = new BlockListBaseHelper(context);

            // grab everything in the database
            Cursor databaseRes = database.getAllData();

            // as long as there's more than zero values, well, not sure how you are deleting anyything
            if (databaseRes.getCount() != 0) {

                // move to the "position" of deletion
                databaseRes.moveToPosition(position);

                // get the id of that position
                String databaseIndex = databaseRes.getString(0);

                // remove that id, and state what the value was
                Log.w(TAG, "Removing ID #" + databaseIndex + " from the database: " + databaseRes.getString(1));
                database.deleteData(databaseIndex);
            }

            // remove the value also from the array list, this ones easier
            Log.w(TAG, "Removing position " + position + " from the ArrayList: " + mBlockLists.get(position).getDomain());
            mBlockLists.remove(position);

            // notify everything
            notifyItemRemoved(position);
            notifyItemRangeRemoved(position, getItemCount());
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return mBlockLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView domain;
//        Switch status;
        ImageButton removeBlocklist;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            domain = itemView.findViewById(R.id.item_text);
//            status = itemView.findViewById(R.id.domain_status);
            removeBlocklist = itemView.findViewById(R.id.remove_button);

        }
    }
}
