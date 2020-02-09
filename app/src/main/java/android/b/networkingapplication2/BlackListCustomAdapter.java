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

import database.BlackListBaseHelper;

public class BlackListCustomAdapter extends RecyclerView.Adapter<BlackListCustomAdapter.MyViewHolder> {

    private ArrayList<BlackWhiteList> mBlackLists;
    private static final String TAG = "BlackListCustomAdapter";
    private Context context;

    public BlackListCustomAdapter(Context context, ArrayList<BlackWhiteList> mBlackLists) {
        this.context = context;
        this.mBlackLists = mBlackLists;
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

        holder.domain.setText(mBlackLists.get(position).getDomain());
        holder.removeBlacklist.setOnClickListener(v -> {

            // Throw some toast at the user stating what happened
            Toast.makeText(context, "Removed: " + (mBlackLists.get(position).getDomain()), Toast.LENGTH_SHORT).show();

            BlackListBaseHelper database = new BlackListBaseHelper(context);

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
            Log.w(TAG, "Removing position " + position + " from the ArrayList: " + mBlackLists.get(position).getDomain());
            mBlackLists.remove(position);

            // notify everything
            notifyItemRemoved(position);
            notifyItemRangeRemoved(position, getItemCount());
            notifyDataSetChanged();

//            // hey, if the user deletes the last row, add a "no data" row. not sure what happens
//            //  if they delete these however...
//            if (databaseRes.getCount() == 1) {
//
//
//                database.insertData(context.getString(R.string.no_data));
//                Log.w(TAG, "Adding initial row");
//
//                notifyItemRemoved(position);
//                notifyItemRangeRemoved(position, getItemCount());
//                notifyDataSetChanged();
//
//            }

        });
    }

    @Override
    public int getItemCount() {
        return mBlackLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView domain;
        ImageButton removeBlacklist;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            domain = itemView.findViewById(R.id.item_text);
            removeBlacklist = itemView.findViewById(R.id.remove_button);

        }
    }
}
