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

    private static final String TAG = "DomainBlockerFragment";

    private Thread timer, graphTimer;
    private Object mPauseLock;
    private boolean mPaused, mFinished;

    private QueryLogBaseHelper myQueDb = OverviewActivity.getMyQueDb();
    private MasterBlockListBaseHelper myMasDb = OverviewActivity.getMyMasDb();

    private TextView totalQueries, blockedDomains, blockedQueries, percentBlocked;


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
        timer = new Thread() {
            @Override
            public void run() {

                try {
                    // while the app is open...
                    while (!mFinished) {

                        // methods that need to update info
                        updateInfo();
                        graphTotalQueries.resetData(getTotalQueriesGraphData());
                        graphBlockedQueries.resetData(getBlockedQueriesGraphData());
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

    private long lBlockedQueries = 0;
    private long lMasterListSize = 0;
    private long lTotalQueries = 0;
    private long lPercentBlocked = 0;

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


//    LineGraphSeries<DataPoint> series1;
//    LineGraphSeries<DataPoint> series2;

    private LineGraphSeries<DataPoint> graphTotalQueries;
    private LineGraphSeries<DataPoint> graphBlockedQueries;

    private void graphFiddler(View view) {
        GraphView graph = view.findViewById(R.id.graph);

        graph.setTitle("Queries over 6 minutes");
//        graph.setTitle(getText(R.string.chart_title).toString());
//        graph.getGridLabelRenderer().setVerticalAxisTitle("Queries"); // if it renders this, the graph is cut off
//        graph.getGridLabelRenderer().setHorizontalAxisTitle("Hours");

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(6);
//        graph.getGridLabelRenderer().setHorizontalAxisTitle("Minutes");
//        graph.getGridLabelRenderer().setVerticalAxisTitle("Queries");

        graphTotalQueries = new LineGraphSeries<>(getTotalQueriesGraphData());
        graphTotalQueries.setTitle("Total Queries");
        graphTotalQueries.setDrawBackground(true);
        graphTotalQueries.setBackgroundColor(Color.argb(10, 95, 210, 70));
        graphTotalQueries.setColor(Color.argb(100, 80, 180, 60));

        graph.addSeries(graphTotalQueries);
//        graphTotalQueries.resetData(getTotalQueriesGraphData());

        graphBlockedQueries = new LineGraphSeries<>(getBlockedQueriesGraphData());
        graphBlockedQueries.setTitle("Blocked Queries");
        graphBlockedQueries.setDrawBackground(true);
        graphBlockedQueries.setBackgroundColor(Color.argb(40, 70, 150, 210));
        graphBlockedQueries.setColor(Color.argb(100, 50, 110, 150));

        graph.addSeries(graphBlockedQueries);
//        graphBlockedQueries.resetData(getBlockedQueriesGraphData());


        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[] {">0", "1", "2", "3", "4", "5"});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }

    private DataPoint[] getTotalQueriesGraphData() {

        long currentTime = System.currentTimeMillis();
        long startupTime = OverviewActivity.startupTime;

        int[] tempList = {0, 0, 0, 0, 0, 0};

        DataPoint[] totalQueriesData = new DataPoint[6];

        if (myQueDb != null) {
            int i = 0;
            for (int j = 0; j < 360000; j += 60000) {
                String sSpanStart = String.valueOf(currentTime - j - 60000);
//                for (int j = 0; j < 21600000; j += 3600000) {
//                    String sSpanStart = String.valueOf(currentTime - j - 3600000);
                String sSpanEnd = String.valueOf(currentTime - j);

                int totalQueriesOverLastMin = myQueDb.specialSelectData("TIME", ">=", sSpanStart, "<", sSpanEnd).getCount();

                tempList[i] = totalQueriesOverLastMin;
                i++;
            }

        }

        for (int i = 0; i < tempList.length; i++) {
            totalQueriesData[i] = new DataPoint(i + 1, tempList[i]);
        }

        return totalQueriesData;
    }

    private DataPoint[] getBlockedQueriesGraphData() {

        long currentTime = System.currentTimeMillis();
        long startupTime = OverviewActivity.startupTime;

        int[] tempList = {0, 0, 0, 0, 0, 0};

        DataPoint[] blockedQueriesData = new DataPoint[6];

        if (myQueDb != null) {
            int i = 0;
            for (int j = 0; j < 360000; j += 60000) {
                String sSpanStart = String.valueOf(currentTime - j - 60000);
//                for (int j = 0; j < 21600000; j += 3600000) {
//                    String sSpanStart = String.valueOf(currentTime - j - 3600000);
                String sSpanEnd = String.valueOf(currentTime - j);

                int blockedQueriesOverLastMin =  myQueDb.moreSpecialSelectData("TIME", ">=", sSpanStart, "<", sSpanEnd, "Blocked (trashed)").getCount();

                tempList[i] = blockedQueriesOverLastMin;
                i++;
            }

        }

        for (int i = 0; i < tempList.length; i++) {
            blockedQueriesData[i] = new DataPoint(i + 1, tempList[i]);
        }

        return blockedQueriesData;
    }

//        if(graphTotalQueries == null) {
//            graphTotalQueries.resetData();
//        }
//
//        graphTotalQueries = new LineGraphSeries<>(totalQueriesData);
//        graphTotalQueries.setTitle("Total Queries");
//        graphTotalQueries.setDrawBackground(true);
//        graphTotalQueries.setBackgroundColor(Color.argb(10, 95, 210, 70));
//        graphTotalQueries.setColor(Color.argb(100, 80, 180, 60));
//
//        graph.addSeries(graphTotalQueries);
//
////        blockedQueriesData = new DataPoint[6];
////        int[] values = {1, 5, 3, 2, 6, 8};
////
////        for (int i = 0; i < 6; i++) {
////            blockedQueriesData[i] = new DataPoint(i, values[i]);
////        }
//
//        LineGraphSeries<DataPoint> blockedQueries = new LineGraphSeries<>(new DataPoint[] {
//                new DataPoint(0, 1),
//                new DataPoint(1, 5),
//                new DataPoint(2, 3),
//                new DataPoint(3, 2),
//                new DataPoint(4, 6)
//        });
//
////        series2 = new LineGraphSeries<>(blockedQueriesData);
//        blockedQueries.setTitle("Blocked Queries");
//        blockedQueries.setDrawBackground(true);
//        blockedQueries.setBackgroundColor(Color.argb(40, 70, 150, 210));
//        blockedQueries.setColor(Color.argb(100, 50, 110, 150));
//
//
//        graph.addSeries(blockedQueries);
//
//        graph.getSeries();
//        //-----------------------------------------------------
//
//
//
//
////        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
////        staticLabelsFormatter.setHorizontalLabels(new String[] {">0", "1", "2", "3", "4", "5"});
////        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
//
//    }

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
