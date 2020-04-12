package com.example.htc20;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;

public class TrendsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference Ref = db.collection("store");
    private ArrayList<Long> RegLCC = new ArrayList<>();
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{

            new DataPoint(1, 2)
    });
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trends);

        //DocumentReference docref = Ref.document("werest");
        //docref.update("lcc",50);


        GraphView graph = (GraphView) findViewById(R.id.graph);

        graph.setTitle("Crowd in Grocery Shops in your Locality");


        //Query for all the nearby stores in the locality
        Query query = Ref.whereEqualTo("shop_type", "Hospital");
        //queryfun(query);

        series.appendData(new DataPoint(2, 1), false, 50);
        series.appendData(new DataPoint(3, 1), false, 50);

        graph.addSeries(series);


        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setMinY(0.0);
        graph.getViewport().setMaxY(50.0);
        graph.getViewport().setXAxisBoundsManual(true);


        graph.getGridLabelRenderer().setHumanRounding(false);


    }

    private void queryfun(Query query) {

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                ArrayList<Long> livecc = new ArrayList<>();
                if (task.isSuccessful()) {
                    if (task.getResult().getDocuments().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            livecc.add((Long) document.get("lcc"));
                        }
                    }
                    updatelist(livecc);

                }

            }
        });
    }

    private void updatelist(ArrayList<Long> LCC) {
        Iterator iterator = LCC.iterator();
        while (iterator.hasNext()) {
            Long temp_lcc = (Long) iterator.next();
            RegLCC.add(temp_lcc);
        }
        Log.d("tag", String.valueOf(RegLCC.get(0)));
        ArrayList<DataPoint> dpt = new ArrayList<>();
        for (int i = 4; i < RegLCC.size() - 4; i++) {
            dpt.add(new DataPoint(i, RegLCC.get(0)));
        }
        setSeries(dpt);

    }

    void setSeries(ArrayList<DataPoint> dp) {
        for (int i = 0; i < dp.size(); i++) {
            series.appendData(dp.get(i), false, 50);
        }
    }
}

