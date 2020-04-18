package android.b.networkingapplication2;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import database.BlockListBaseHelper;
import database.MasterBlockListBaseHelper;

public class BlockListCustomAdapter extends RecyclerView.Adapter<BlockListCustomAdapter.MyViewHolder> {

    private ArrayList<BlockList> mBlockLists;
    private static final String TAG = "BlockListCustomAdapter";
    private Context context;
//    List<String> idList;
    Thread removeBlocklist;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        holder.domain.setText(mBlockLists.get(position).getDomain());

        holder.removeBlocklist.setOnClickListener(v -> {

            String sBlockList = mBlockLists.get(position).getDomain();

            builder.setMessage(context.getResources().getString(R.string.removing) + sBlockList)
                    .setCancelable(false);

            AlertDialog alert2 = builder.create();
            alert2.setTitle(R.string.please_wait);
            alert2.show();

            BlockListBaseHelper database = new BlockListBaseHelper(context);

            // grab everything in the database
            Cursor databaseRes = database.getAllData();

            // move to the "position" of deletion
            databaseRes.moveToPosition(position);

            // get the id of that position
            String databaseIndex = databaseRes.getString(0);

            // remove that id, and state what the value was
            Log.w(TAG, "Removing ID #" + databaseIndex + " from the database: " + databaseRes.getString(1));
            database.deleteData(databaseIndex);

            // Remove all domains added from that particular blocklist from the master blocklist

            long startTime = new Date().getTime();

            removeBlocklist = new Thread() {
                @Override
                public void run() {

                    MasterBlockListBaseHelper myMasDb = new MasterBlockListBaseHelper(context);

                    Cursor myMasDbRes = myMasDb.getAllData();

                    ArrayList<String> idList = new ArrayList<>();

                    while (myMasDbRes.moveToNext()) {
                        if (sBlockList.equals(myMasDbRes.getString(1))) {
                            idList.add(myMasDbRes.getString(0));
                        }
                    }

                    myMasDb.deleteData(idList);

                    // add a slight delay if it returns too fast
                    long completionTime = new Date().getTime();
                    if (startTime + 2500 >= completionTime) {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException ignore) {
                            // the user closed the application while the
                            //  application was sleeping, that wasn't very nice
                        }
                    }

                    alert2.cancel();
                }
            };

            removeBlocklist.start();

            // remove the value also from the array list, this ones easier
            Log.w(TAG, "Removing position " + position + " from the ArrayList: " + sBlockList);
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
