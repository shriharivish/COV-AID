package com.example.htc20;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PlaceListActivity extends AppCompatActivity {

    //variables
    private FusedLocationProviderClient client;
    //radius for searching the nearby essential places
    private int PROXIMITY_RADIUS = 1500;
    // list of hospitals to be displayed
    private ListView places_list;
    // button to view all the nearby essential places
    private Button mapsAcitivity;
    ArrayList<String> list = new ArrayList<String>();
    private EditText searchBar;

    // Latitudes and Longitudes of all the essential places
    HashMap<String, Point> mapCoordinates = new HashMap<>();

    //global variables as location will be used outside the local blocks
    double Latitude = 0;
    double Longitude = 0;
    //serial number of the essential places to be displayed
    int count = 1;

    private SeekBar sb_distance;
    private TextView tv_distance;
    private ProgressBar spinner;

    private ArrayAdapter adapter;
    private static final double MIN_LAT = Math.toRadians(-90d);
    private static final double MAX_LAT = Math.toRadians(90d);
    private static final double MIN_LON = Math.toRadians(-180d);
    private static final double MAX_LON = Math.toRadians(180d);
    ArrayList<ArrayList<String>> RegStores = new ArrayList<ArrayList<String>>();







    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);

        //Initialization of Elements in layout file
        places_list = findViewById(R.id.lv_places_list);
        final ListView listview = findViewById(R.id.lv_places_list);
        mapsAcitivity = findViewById(R.id.btn_mapsActivityLauncher);
        sb_distance = findViewById(R.id.sb_distance);
        tv_distance = findViewById(R.id.tv_distance);
        searchBar = findViewById(R.id.et_searchBar);
        spinner = (ProgressBar)findViewById(R.id.progressBar1);

        sb_distance.setMin(1);

        tv_distance.setText("Proximity Radius: " + sb_distance.getMin() * 0.5 + " km.");

        spinner.setVisibility(View.VISIBLE);
        setupList(listview);
        setupSeekBar(listview, sb_distance, tv_distance);


        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("CHECK", "ENTERS ON TEXT CHANGES");
                (PlaceListActivity.this).adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void setupSeekBar(final ListView listview, SeekBar sb_distance, final TextView tv_distance) {
        final int[] progressValue = new int[1];
        sb_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue[0] = progress;
                double d = progressValue[0] * 0.5;
                tv_distance.setText("Proximity Radius: " + d + " km.");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                count = 1;
                PROXIMITY_RADIUS = progressValue[0] * 500;
                //clear the list again and call setuplist
                list.clear();
                setupList(listview);
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void setupList(final ListView listview) {

        //specify the type of service
        Bundle extras = getIntent().getExtras();
        final int store_type = extras.getInt("number");

        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setClickable(true);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String n = listview.getItemAtPosition(position).toString();
                final int index = n.indexOf('\t');
                final String name = n.substring(0, index);
                //add pop up with two buttons
                if(store_type == 1 || store_type == 2 || store_type == 4) {

                    final Dialog dialog = new Dialog(PlaceListActivity.this);
                    dialog.setContentView(R.layout.custom_dialog_layout);

                    Button one = (Button) dialog.getWindow().findViewById(R.id.btn_1);
                    Button two = (Button) dialog.getWindow().findViewById(R.id.btn_2);


                    if (store_type == 1 || store_type == 2) {

                        one.setText("order");
                        two.setText("directions");

                        one.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onClick(View v) {

                                dialog.cancel();
                            }
                        });
                        two.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent i = new Intent(android.content.Intent.ACTION_VIEW,
                                        Uri.parse("http://maps.google.com/maps?saddr=" + Latitude + "," + Longitude + "&daddr=" + (mapCoordinates.get(name).lattitude) + "," + (mapCoordinates.get(name).longitude)));
                                i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                startActivity(i);
                                dialog.cancel();
                            }
                        });
                    }else if (store_type == 4){

                        one.setText("Yes");
                        two.setText("No");

                        //TextView textcontent = (TextView)findViewById(R.id.txt_dia);
                        //Log.d("txt",textcontent.getText().toString());
                        
                        one.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onClick(View v) {
                                //add lcc as corona patient
                                Toast.makeText(getApplicationContext(), "Response Submitted!", 200).show();
                                dialog.cancel();
                            }
                        });

                        two.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onClick(View v) {
                                //add lcc as normal patient
                                Toast.makeText(getApplicationContext(), "Response Submitted!", 200).show();
                                dialog.cancel();
                            }
                        });
                    }
                        dialog.show();

                }else{
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=" + Latitude + "," + Longitude + "&daddr=" + (Objects.requireNonNull(mapCoordinates.get(name)).lattitude) + "," + (Objects.requireNonNull(mapCoordinates.get(name)).longitude)));
                    i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    startActivity(i);
                }

            }
        });



        client = LocationServices.getFusedLocationProviderClient(this);

        client.getLastLocation().addOnSuccessListener(PlaceListActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    FirebaseFirestore db;
                    Latitude = location.getLatitude();
                    Longitude = location.getLongitude();

                    //the code to retrieve nearby places will be written below


                    String nearbyPlace = "";
                    //String will contain the json output

                    JSONArray jsonArray = null;
                    JSONObject jsonObject;
                    String PlaceLat = "";
                    String PlaceLong = "";
                    String PlaceName = "";
                    int placesCount = 0;


                    switch (store_type) {
                        case 1:
                            nearbyPlace = "pharmacy|drugstore";
                            break;
                        case 2:
                            nearbyPlace = "grocery_or_supermarket";
                            break;
                        case 3:
                            nearbyPlace = "atm";
                            break;
                        case 4:
                            nearbyPlace = "hospital";
                            break;
                        default:
                            Log.d("errtag", "Unexpected entry! check DashboardCitizenActivity");
                    }

                    try {
                        jsonArray = getAllresults(Latitude, Longitude, nearbyPlace);
                        placesCount = jsonArray.length();

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("listlog", "val:" + list.size());
                    db = FirebaseFirestore.getInstance();


                    Log.d("Loctag", "value: " + placesCount);

                    for (int i = 0; i < placesCount; i++) {
                        try {

                            jsonObject = (JSONObject) jsonArray.get(i);
                            PlaceName = jsonObject.getString("name");
                            PlaceLat = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
                            PlaceLong = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
                            Log.d("Latitudes", "valueLat :" + PlaceLat);
                            Log.d("Longitudes", "valueLong : " + PlaceLong);

                            Point coordinate = new Point(0, 0);
                            coordinate.lattitude = Double.parseDouble(PlaceLat);
                            coordinate.longitude = Double.parseDouble(PlaceLong);
                            mapCoordinates.put(PlaceName, coordinate);
                            CollectionReference ref = db.collection("store");
                            Query query = ref.whereEqualTo("latitude", coordinate.lattitude).whereEqualTo("longitude", coordinate.longitude);
                            queryfunLcc(query, PlaceName, count);
                            count++;

                            //parsing to be done
                        } catch (JSONException e) {
                            Log.d("Places", "Error in Adding places");
                            e.printStackTrace();
                        }
                    }

                    //append the registered stores in the proximity radius to the list
                    CollectionReference addref = db.collection("store");

                    //get the minimum and maximum latitudes and longitudes
                    LatLng[] latLng1 = boundingCoordinates(PROXIMITY_RADIUS);
                    Log.d("mmtag", "val:" + latLng1[0]);

                   Query addquery = addref.whereGreaterThanOrEqualTo("latitude", latLng1[0].latitude).
                            whereLessThanOrEqualTo("latitude",latLng1[1].latitude);

                   Query addquery1 = addref.whereGreaterThanOrEqualTo("longitude", latLng1[0].longitude).
                            whereLessThanOrEqualTo("longitude",latLng1[1].longitude);

                   queryfunArraylist(addquery, addquery1);

                }
            }
        });
        if(list.size()==0){
            Toast.makeText(getApplicationContext(), "There are no registered stores in this area:(", 1000).show();

        }

        spinner.setVisibility(View.GONE);

        mapsAcitivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude);
                switch (store_type) {
                    case 1:     gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude + "?q=pharmacy|drugstore"); break;
                    case 2:     gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude + "?q=grocery_or_supermarket|store");  break;
                    case 3:     gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude + "?q=atm");  break;
                    case 4:     gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude + "?q=hospital");
                    default:    Log.d("errtag", "Unexpected entry! check DashboardCitizenActivity");
                }

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
    }

    synchronized private void queryfunLcc(Query query, final String PlaceName, final Integer count){

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Integer lcc = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //string containing the nearby stores
                        lcc = (Integer) document.getData().get("lcc");
                    }
                    updateLCC(lcc, PlaceName, count );
                }
            }
        });
    }

    private void queryfunArraylist(Query addquery, Query addquery1) {

        final List str = new ArrayList<String>();

        addquery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> strr = new ArrayList<>();
                if (task.isSuccessful()) {
                    Integer lcc = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //string containing the nearby stores
                        lcc = (Integer)document.get("lcc");
                        Log.d("LCC","val:"+document.contains("lcc"));
                        strr.add(document.getData().get("shop_name").toString()+"\t\t:"+lcc);

                    }
                    updatelist(strr);
                }
            }

        });
        addquery1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> strr = new ArrayList<>();
                if (task.isSuccessful()) {
                    Integer lcc = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //string containing the nearby stores
                        lcc = (Integer) document.getData().get("lcc");
                        strr.add(document.getData().get("shop_name").toString()+"\t\t:"+lcc);

                    }
                    updatelist(strr);
                }
            }

        });

    }
    synchronized private void updateLCC(Integer lcc, String PlaceName, Integer count){
        list.add(PlaceName + "\t\t: " + String.valueOf(lcc));
        adapter.notifyDataSetChanged();

    }
    private void updatelist(ArrayList<String> element){

        RegStores.add(element);
        Log.d("taggg","val: "+RegStores);
        if(RegStores.size() > 1){
            RegStores.get(0).retainAll(RegStores.get(1));
            Iterator iterator = RegStores.get(0).iterator();
            while (iterator.hasNext()) {
                list.add((String) iterator.next());
                adapter.notifyDataSetChanged();
            }
            list = new ArrayList<String>(new LinkedHashSet<String>(list));
            adapter.notifyDataSetChanged();
        }

    }

    private JSONArray getAllresults(double Latitude,double  Longitude, String nearbyPlace) throws UnsupportedEncodingException, ExecutionException, InterruptedException, JSONException {

        String strUrl = null;
        String jsonOutput = null;
        JSONObject jsonObject = null;

        strUrl = getUrl(Latitude, Longitude, nearbyPlace);
        jsonOutput = new RequestJsonPlaces().execute(strUrl).get();
        jsonObject = new JSONObject((String) jsonOutput);
        Log.d("mytag","value:"+jsonObject);
        JSONArray jsonArray1 = jsonObject.getJSONArray("results");
        Log.d("mytag","value: "+jsonArray1.length());
        return  jsonArray1;
    }


    private String getUrl(double latitude, double longitude, String nearbyPlace) throws UnsupportedEncodingException {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlacesUrl.append("&types=" + URLEncoder.encode(nearbyPlace,"UTF-8"));
        googlePlacesUrl.append("&sensor=false");
        //googlePlacesUrl.append("hasNextPage=true&nextPage()=true");
        googlePlacesUrl.append("&key=" + "AIzaSyBpsUyOqhq0MOBN0abTsFFlrAa4WUqkzQQ");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }


    public LatLng[] boundingCoordinates(double distance) {
        double radLat = Math.toRadians(Latitude);
        double radLon = Math.toRadians(Longitude);
        double radius = 6371*1000.0;
        if (radius < 0d || distance < 0d)
            throw new IllegalArgumentException();

        // angular distance in radians on a great circle
        double radDist = distance / radius;
        Log.d("mmtagrad","val:"+radDist);
        double minLat = radLat - radDist;
        double maxLat = radLat + radDist;
        Log.d("mmtagmm","valm:"+minLat+" "+maxLat);

        double minLon, maxLon;
        if (minLat > MIN_LAT && maxLat < MAX_LAT) {
            double deltaLon = Math.asin(Math.sin(radDist) /
                    Math.cos(radLat));
            minLon = radLon - deltaLon;
            if (minLon < MIN_LON) minLon += 2d * Math.PI;
            maxLon = radLon + deltaLon;
            if (maxLon > MAX_LON) maxLon -= 2d * Math.PI;
        } else {
            // a pole is within the distance
            minLat = Math.max(minLat, MIN_LAT);
            maxLat = Math.min(maxLat, MAX_LAT);
            minLon = MIN_LON;
            maxLon = MAX_LON;
        }

        return new LatLng[]{ fromRadians(minLat, minLon), fromRadians(maxLat, maxLon) };

    }

    private void checkBounds(double radLat, double radLon) {
        if (radLat < MIN_LAT || radLat > MAX_LAT ||
                radLon < MIN_LON || radLon > MAX_LON)
            throw new IllegalArgumentException();
    }

    public LatLng fromRadians(double radLat, double radLon) {

       checkBounds(radLat, radLon);
       radLat = Math.toDegrees(radLat);
       radLon = Math.toDegrees(radLon);
       Log.d("mmtag1","val:"+radLat+" "+radLon);
       return new LatLng(radLat, radLon);
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


class Point {
    double lattitude;
    double longitude;

    public Point(double lattitude, double longitude) {
        this.lattitude = lattitude;
        this.longitude = longitude;
    }
}
