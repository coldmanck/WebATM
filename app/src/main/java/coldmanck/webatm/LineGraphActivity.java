package coldmanck.webatm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.*;
import com.jjoe64.graphview.series.*;

import java.util.ArrayList;

public class LineGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        Intent intent = getIntent();
        ArrayList<String> cashHistory = intent.getStringArrayListExtra(MainActivity.EXTRA_ACCOUNT_MESSAGE);

        int historySize = cashHistory.size();
        DataPoint[] dp = new DataPoint[historySize];
        for(int i = 0; i < historySize; i++)
            dp[i] = new DataPoint(i, Integer.valueOf(cashHistory.get(i)));

        GraphView graph = (GraphView) findViewById(R.id.graph);
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
//                new DataPoint(0, 1),
//                new DataPoint(1, 5),
//                new DataPoint(2, 3),
//                new DataPoint(3, 2),
//                new DataPoint(4, 6)
//        });
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp);
        graph.addSeries(series);
    }
}
