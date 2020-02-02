package android.b.networkingapplication2;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import database.QueryLogBaseHelper;

public class QueryLogFragment extends Fragment {
    private QueryLogBaseHelper myQueDb;
    private RecyclerView mQueryLogRecyclerView;
    public static View view;

    private static final String TAG = "QueryLogFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_query_log, container, false);

        myQueDb = new QueryLogBaseHelper(getContext());

        mQueryLogRecyclerView = view.findViewById(R.id.query_log_recycler_view);

        LinearLayoutManager queryLogLinearLayoutManager = new LinearLayoutManager(getContext());
        mQueryLogRecyclerView.setLayoutManager(queryLogLinearLayoutManager);

        try {

            if (myQueDb.getAllData().getCount() == 0) {
                Log.w(TAG, "DB empty, adding test data");
                myQueDb.insertData("",getResources().getString(R.string.no_data), "");
//                myQueDb.insertData("time-tastic","2222", "false");
//                myQueDb.insertData("time-tastic","3333", "true");
//                myQueDb.insertData("time-tastic","4444", "false");
//                myQueDb.insertData("time-tastic","5555", "true");
            }

            // just a precautionary measure because there might somehow be nothing in the list
            try {
                Cursor checkInitial = myQueDb.getAllData();

                if (checkInitial.getCount() == 1) {
                    checkInitial.moveToFirst();
                    if (checkInitial.getString(1).equals(getResources().getString(R.string.no_data))) {
                        myQueDb.deleteData("1");
                        Log.w(TAG, "Removing initial row");
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "Phew, we were just grazed by an apocalypse...");
            }


            updateUI();

        } catch (NullPointerException e) {
            Toast.makeText(getContext(), "[NullPointerException]", Toast.LENGTH_LONG).show();
            System.err.println("[NullPointerException] Now just stop tryin' ta mess with my contraptions.");
        }

        return view;
    }

    public void updateUI() {

        ArrayList<QueryLog> mQueryLogList = new ArrayList<>();

        Cursor queryLogRes = myQueDb.getAllData();

        if (queryLogRes.getCount() != 0) {
            while (queryLogRes.moveToNext()) {
                QueryLog queryLogListItem = new QueryLog();
                queryLogListItem.setTime(queryLogRes.getString(1));
                queryLogListItem.setDomain(queryLogRes.getString(2));
                queryLogListItem.setStatus(queryLogRes.getString(3));

                mQueryLogList.add(queryLogListItem);
            }
        }

        QueryLogCustomAdapter querylogcustomAdapter = new QueryLogCustomAdapter(getContext(), mQueryLogList);
        mQueryLogRecyclerView.setAdapter(querylogcustomAdapter);

//        System.out.println("You know where ya' oughta' hide next time? Back in France.");

    }

    public static QueryLogFragment newInstance() {
        return new QueryLogFragment();
    }
}
