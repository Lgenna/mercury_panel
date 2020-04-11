package android.b.networkingapplication2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class QueryLogCustomAdapter extends RecyclerView.Adapter<QueryLogCustomAdapter.MyViewHolder> {

    private ArrayList<QueryLog> mQueryLogList;
    private static final String TAG = "QueryLogCustomAdapter";
    private Context context;

    public QueryLogCustomAdapter(Context context, ArrayList<QueryLog> mQueryLogList) {
        this.context = context;
        this.mQueryLogList = mQueryLogList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_query_log, parent, false);
        // set the view's size, margins, padding and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        SimpleDateFormat standard = new SimpleDateFormat("MM/dd/yy \n kk:mm:ss", Locale.US);

        holder.time.setText(String.valueOf(standard.format(mQueryLogList.get(position).getTime())));
        holder.domain.setText(mQueryLogList.get(position).getDomain());
        holder.status.setText(mQueryLogList.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return mQueryLogList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView domain;
        TextView status;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            time = itemView.findViewById(R.id.time);
            domain = itemView.findViewById(R.id.domain);
            status = itemView.findViewById(R.id.status);

        }
    }
}
