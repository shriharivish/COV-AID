package com.example.htc20;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class HospitalListActivity extends AppCompatActivity {

    //variables
    private FusedLocationProviderClient client;
    private int PROXIMITY_RADIUS = 1500;
    private ListView hospital_list;
    private Button mapsAcitivity;
    double[] Latitudes;
    double[] Longitudes;
    double Latitude = 0;
    double Longitude = 0;
    int count = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_list);
        hospital_list = findViewById(R.id.lv_hospitalList);

        final ListView listview = (ListView) findViewById(R.id.lv_hospitalList);


        final ArrayList<String> list = new ArrayList<String>();


        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setClickable(true);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String n = listview.getItemAtPosition(position).toString();
                int index = n.charAt(0) - '1';
                Intent i = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + Latitude + "," + Longitude + "&daddr=" + (Latitudes[index]) + "," + (Longitudes[index])));
                i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(i);

            }
        });
        //
        client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnSuccessListener(HospitalListActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    FirebaseFirestore db;
                    Latitude = location.getLatitude();
                    Longitude = location.getLongitude();
                    LatLng myLatLng = new LatLng(Latitude, Longitude);
                    //the code to retrieve nearby places will be written below
                    String data = "";
                    InputStream iStream = null;
                    HttpURLConnection urlConnection = null;
                    String strUrl = getUrl(Latitude, Longitude, "hospital");
                    //String will contain the json output
                    String jsonOutput = null;

                    db = FirebaseFirestore.getInstance();
                    try {
                        jsonOutput = new RequestJsonPlaces().execute(strUrl).get();
                        Log.d("mytag", "values : "+ jsonOutput);
                        JSONArray jsonArray = null;
                        JSONObject jsonObject;
                        String HospitalLat = "";
                        String HospitalLong = "";
                        String HospitalName  = "";
                        try {
                            Log.d("Places", "parse");
                            jsonObject = new JSONObject((String) jsonOutput);
                            Log.d("Places","Values : "+jsonObject);
                            jsonArray = jsonObject.getJSONArray("results");
                            int placesCount = jsonArray.length();
                            Latitudes = new double[placesCount];
                            Longitudes = new double[placesCount];
                            Log.d("Loctag", "value: "+ placesCount);
                            for (int i = 0; i < placesCount; i++) {
                                try{
                                    jsonObject = (JSONObject) jsonArray.get(i);
                                    HospitalName  = jsonObject.getString("name");
                                    HospitalLat = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
                                    HospitalLong = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
                                    Log.d("Latitudes", "valueLat :"+HospitalLat);
                                    Log.d("Longitudes","valueLong : "+HospitalLong);
                                    Latitudes[i] = Double.parseDouble(HospitalLat);
                                    Longitudes[i] = Double.parseDouble(HospitalLong);
                                    CollectionReference ref = db.collection("store");
                                    Query query = ref.whereEqualTo("latitude", Latitudes[i]).whereEqualTo("longitude", Longitudes[i]);
                                    final Map<String, Integer> user = new HashMap<>();
                                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            int lcc;
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    lcc = Integer.parseInt((String) document.getData().get("lcc"));
                                                    user.put("lcc", lcc);
                                                }
                                            }
                                        }
                                    });
                                    Integer lcc = user.get("lcc");
                                    list.add(count + ". " + HospitalName + "\t\t: " + String.valueOf(lcc));
                                    count++;
                                    adapter.notifyDataSetChanged();
                                    //parsing to be done
                                } catch (JSONException e) {
                                    Log.d("Places", "Error in Adding places");
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            Log.d("Places", "parse error");
                            e.printStackTrace();
                        }

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        });
        mapsAcitivity = findViewById(R.id.btn_mapsActivityLauncher);
        mapsAcitivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //Intent Go_to_map = new Intent(HospitalListActivity.this, MapsActivity.class);
                //Go_to_map.putExtra("Latitudes", Latitudes);
                //Go_to_map.putExtra("Longitudes", Longitudes);
                //startActivity(Go_to_map);
                Uri gmmIntentUri = Uri.parse("geo:"+Latitude+","+Longitude+"?q=hospitals");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });

    }

    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        //googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyBpsUyOqhq0MOBN0abTsFFlrAa4WUqkzQQ");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }


}

class RequestJsonPlaces extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {


        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(params[0]);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            try {
                iStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            urlConnection.disconnect();
        }
        return data;
    }
    @Override
    protected void onPostExecute (String result){
        super.onPostExecute(result);
    }

}



