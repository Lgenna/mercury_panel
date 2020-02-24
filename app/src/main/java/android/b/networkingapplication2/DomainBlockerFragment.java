package android.b.networkingapplication2;

import android.app.admin.DevicePolicyManager;
import android.app.admin.NetworkEvent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;
import java.util.List;

public class DomainBlockerFragment extends Fragment {

    private static final String TAG = "DomainBlockerFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_domain_blocker, container, false);
        try {

            Date startupDate = new Date();
            long startupTime = startupDate.getTime();
            TextView totalQueries = view.findViewById(R.id.total_queries_number);
            totalQueries.setText(startupTime + "");

            GraphView graph = view.findViewById(R.id.graph);
            addData(graph);
        }
        catch (NullPointerException e) {
            Toast.makeText(getContext(), "OH NO! : " + e, Toast.LENGTH_LONG).show();
            Log.w(TAG, "Now just stop tryin' ta mess with my contraptions : " + e);
        }
        return view;

    }




//    LineGraphSeries<DataPoint> series1;
//    LineGraphSeries<DataPoint> series2;

    private void addData(GraphView graph) {

//        DataPoint[] totalQueriesData;
//        DataPoint[] blockedQueriesData;

//        int[] anotherList = {2, 7, 3, 2, 8, 9};
//        totalQueriesData = new DataPoint[6];
//
//        for (int i = 0; i < 6; i++) {
//            totalQueriesData[i] = new DataPoint(i, anotherList[i]);
//        }

        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 2),
                new DataPoint(1, 6),
                new DataPoint(2, 4),
                new DataPoint(3, 3),
                new DataPoint(4, 12),
                new DataPoint(5, 3),
                new DataPoint(6, 4)
        });

//        series1 = new LineGraphSeries<>(totalQueriesData);
        series1.setTitle("Total Queries");
        series1.setDrawBackground(true);
        series1.setBackgroundColor(Color.argb(10, 95, 210, 70));
        series1.setColor(Color.argb(100, 80, 180, 60));

        graph.addSeries(series1);

//        blockedQueriesData = new DataPoint[6];
//        int[] values = {1, 5, 3, 2, 6, 8};
//
//        for (int i = 0; i < 6; i++) {
//            blockedQueriesData[i] = new DataPoint(i, values[i]);
//        }

        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });

//        series2 = new LineGraphSeries<>(blockedQueriesData);
        series2.setTitle("Blocked Queries");
        series2.setDrawBackground(true);
        series2.setBackgroundColor(Color.argb(40, 70, 150, 210));
        series2.setColor(Color.argb(100, 50, 110, 150));

        graph.addSeries(series2);

        //-----------------------------------------------------

        graph.setTitle(getText(R.string.chart_title).toString());
//        graph.getGridLabelRenderer().setVerticalAxisTitle("Queries"); // if it renders this, the graph is cut off
//        graph.getGridLabelRenderer().setHorizontalAxisTitle("Hours");

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(6);


//        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
//        staticLabelsFormatter.setHorizontalLabels(new String[] {">0", "1", "2", "3", "4", "5"});
//        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);

    }

    public static DomainBlockerFragment newInstance() {
        return new DomainBlockerFragment();
    }

}
