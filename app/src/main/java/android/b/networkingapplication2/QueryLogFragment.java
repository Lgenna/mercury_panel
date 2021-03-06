package android.b.networkingapplication2;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import database.QueryLogBaseHelper;

public class QueryLogFragment extends Fragment {
    private RecyclerView mQueryLogRecyclerView;

    private Object mPauseLock;
    private boolean mPaused, mFinished;
    private QueryLogBaseHelper myQueDb;

    private static final String TAG = "QueryLogFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_query_log, container, false);

        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;

        mQueryLogRecyclerView = view.findViewById(R.id.query_log_recycler_view);

        // Builds a new thread
        Thread timer = new Thread() {
            @Override
            public void run() {

                try {

                    myQueDb = OverviewActivity.getMyQueDb();

                    LinearLayoutManager queryLogLinearLayoutManager = new LinearLayoutManager(getContext());
                    mQueryLogRecyclerView.setLayoutManager(queryLogLinearLayoutManager);

                    // while the app is open...
                    while (!mFinished) {

                        // methods that need to update info
                        updateInfo();

                        // update the user interface every 10 seconds
                        Thread.sleep(5000);
                        //create a synchronized boolean mPauseLock
                        synchronized (mPauseLock) {
                            // check to see if the activity was paused
                            while (mPaused) {
                                // if paused, stop updating the ui
                                try {
                                    mPauseLock.wait();
                                } catch (InterruptedException ignore) {
                                    // Panic x2
                                }
                            }
                        }
                    }
                    Log.i(TAG, "Finished Looping, this isn't supposed to happen.");
                } catch (InterruptedException ignore) {
                    // panic.
                }
            }
        };

        // start the thread
        timer.start();

        return view;
    }

    private QueryLogCustomAdapter querylogcustomAdapter;

    private void updateInfo() {

        ArrayList<QueryLog> mQueryLogList = new ArrayList<>();

        Cursor queryLogRes = myQueDb.getAllData();

        if (queryLogRes.getCount() != 0) {
            queryLogRes.moveToLast();
            while (queryLogRes.moveToPrevious()) {
                QueryLog queryLogListItem = new QueryLog();
                queryLogListItem.setTime(queryLogRes.getLong(1));
                queryLogListItem.setDomain(queryLogRes.getString(2));
                queryLogListItem.setStatus(queryLogRes.getString(3));

                mQueryLogList.add(queryLogListItem);
            }
        }

        querylogcustomAdapter = new QueryLogCustomAdapter(getContext(), mQueryLogList);
        getActivity().runOnUiThread(() -> mQueryLogRecyclerView.setAdapter(querylogcustomAdapter));

    }

    public static QueryLogFragment newInstance() {
        return new QueryLogFragment();
    }
}
