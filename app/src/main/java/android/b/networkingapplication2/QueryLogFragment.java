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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import database.QueryLogBaseHelper;

public class QueryLogFragment extends Fragment {
    private RecyclerView mQueryLogRecyclerView;
    public static View view;

    private Thread timer;
    private Object mPauseLock;
    private boolean mPaused, mFinished;
    private QueryLogBaseHelper myQueDb;

    private static final String TAG = "QueryLogFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_query_log, container, false);

        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;

        mQueryLogRecyclerView = view.findViewById(R.id.query_log_recycler_view);

        // Builds a new thread
        timer = new Thread() {
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
        if (myQueDb != null) {

            Cursor checkInitial = myQueDb.getAllData();

            if (checkInitial.getCount() == 0) {
                Log.w(TAG, "DB empty, adding test data");
                myQueDb.insertData("", getResources().getString(R.string.no_data), "");
            } else if (checkInitial.getCount() != 1) {
                checkInitial.moveToFirst();
                if (checkInitial.getString(2).equals(getResources().getString(R.string.no_data))) {
                    myQueDb.deleteData("1");
                    Log.w(TAG, "Removing initial row");
                }
            }
        }

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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mQueryLogRecyclerView.setAdapter(querylogcustomAdapter);
            }
        });




    }

    public static QueryLogFragment newInstance() {
        return new QueryLogFragment();
    }
}
