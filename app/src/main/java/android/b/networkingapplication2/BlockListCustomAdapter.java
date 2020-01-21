package android.b.networkingapplication2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BlockListCustomAdapter extends RecyclerView.Adapter<BlockListCustomAdapter.MyViewHolder> {

    ArrayList<BlockList> mBlockLists;
//    Context context;

    public BlockListCustomAdapter(Context context, ArrayList<BlockList> mBlockLists) {
//        this.context = context;
        this.mBlockLists = mBlockLists;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_blocklist, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.domain.setText(mBlockLists.get(position).getDomain());
        holder.status.setChecked(mBlockLists.get(position).getStatus());
    }


    @Override
    public int getItemCount() {
        return mBlockLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView domain;
        Switch status;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            domain = itemView.findViewById(R.id.domain_title);
            status = itemView.findViewById(R.id.domain_status);

        }
    }
}
