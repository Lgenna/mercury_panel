package android.b.networkingapplication2;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import database.MasterBlockListBaseHelper;
import database.QueryLogBaseHelper;

public class DomainBlockerFragment extends Fragment {

    private Object mPauseLock;
    private boolean mPaused, mFinished;
    private long lBlockedQueries = 0, lMasterListSize = 0, lTotalQueries = 0,
            lPercentBlocked = 0, prevTotalQueries = 0, prevBlockedQueries = 0;
    private LineGraphSeries<DataPoint> graphTotalQueries, graphBlockedQueries;
    private QueryLogBaseHelper myQueDb = OverviewActivity.getMyQueDb();
    private MasterBlockListBaseHelper myMasDb = OverviewActivity.getMyMasDb();
    private TextView totalQueries, blockedDomains, blockedQueries, percentBlocked;
    private long[] totalQueriesArray = {0, 0, 0, 0, 0, 0}, blockedQueriesArray = {0, 0, 0, 0, 0, 0};

    private static final String TAG = "DomainBlockerFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_domain_blocker, container, false);

        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;

        totalQueries = view.findViewById(R.id.total_queries_number);
        blockedDomains = view.findViewById(R.id.blocklist_domains_number);
        blockedQueries = view.findViewById(R.id.queries_blocked_number);
        percentBlocked = view.findViewById(R.id.percent_blocked_number);

        graphFiddler(view);

        // Builds a new thread
        Thread timer = new Thread() {
            @Override
            public void run() {
                int minuteCounter = 0;

                try {
                    // while the app is open...
                    while (!mFinished) {

                        // methods that need to update info
                        updateInfo();
                        if (minuteCounter >= 12) {
                            graphTotalQueries.resetData(getQueriesGraphData(true, totalQueriesArray, prevTotalQueries));
                            graphBlockedQueries.resetData(getQueriesGraphData(false, blockedQueriesArray, prevBlockedQueries));
                            minuteCounter = 0;
                        }
                        // update the user interface every 5 seconds
                        Thread.sleep(5000);

                        if (minuteCounter == 0) {
                            prevTotalQueries = lTotalQueries;
                            prevBlockedQueries = lBlockedQueries;
                        }

                        minuteCounter++;
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

    private void updateInfo() {

        if (myQueDb != null) {
            lTotalQueries = myQueDb.countData();
            Cursor blockedQueriesRes = myQueDb.selectData("STATUS", "Blocked (trashed)");

            if (blockedQueriesRes != null) {
                lBlockedQueries = blockedQueriesRes.getCount();
            }

            if (lBlockedQueries != 0) {
                lPercentBlocked = (long)((float)lBlockedQueries / lTotalQueries * 100);
            }
        }

        if(myMasDb != null) {
            lMasterListSize = myMasDb.countData();
        }

        if(getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                totalQueries.setText(String.valueOf(lTotalQueries));
                blockedQueries.setText(String.valueOf(lBlockedQueries));
                blockedDomains.setText(String.valueOf(lMasterListSize));
                percentBlocked.setText(String.valueOf(lPercentBlocked));
            });
        }
    }

    private void graphFiddler(View view) {
        GraphView graph = view.findViewById(R.id.graph);

        graph.setTitle("Queries over 6 minutes");

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(6);

        graphTotalQueries = new LineGraphSeries<>(getQueriesGraphData(true, totalQueriesArray, prevTotalQueries));
        graphTotalQueries.setTitle("Total Queries");
        graphTotalQueries.setDrawBackground(true);
        graphTotalQueries.setBackgroundColor(Color.argb(10, 95, 210, 70));
        graphTotalQueries.setColor(Color.argb(100, 80, 180, 60));

        graph.addSeries(graphTotalQueries);

        graphBlockedQueries = new LineGraphSeries<>(getQueriesGraphData(false, blockedQueriesArray, prevBlockedQueries));
        graphBlockedQueries.setTitle("Blocked Queries");
        graphBlockedQueries.setDrawBackground(true);
        graphBlockedQueries.setBackgroundColor(Color.argb(40, 70, 150, 210));
        graphBlockedQueries.setColor(Color.argb(100, 50, 110, 150));

        graph.addSeries(graphBlockedQueries);

        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {">0", "1", "2", "3", "4", "5"});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }

    private DataPoint[] getQueriesGraphData(boolean isTotalQueriesCalling, long[] dataArray, long previousTotal) {

        // move each dataArray value down one
        System.arraycopy(dataArray, 0, dataArray, 1, dataArray.length - 1);
        // WOAH that's efficient!

        // depending on where you are coming from, get the number of queries minus the previous
        long queriesValue = lBlockedQueries - previousTotal;

        if (isTotalQueriesCalling) {
            queriesValue = lTotalQueries - previousTotal;
        }

        // set the first to that computed value
        dataArray[0] = queriesValue;

        // make a dataPoint array
        DataPoint[] dataPointArray = new DataPoint[6];

        // fill each value of the dataPoint array with a value from the dataArray
        for (int i = 0; i < dataArray.length; i++) {
            dataPointArray[i] = new DataPoint(i + 1, dataArray[i]);
        }

        return dataPointArray;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateInfo();

        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static DomainBlockerFragment newInstance() {
        return new DomainBlockerFragment();
    }

}
