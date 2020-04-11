package com.example.htc20;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
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
import java.util.concurrent.ExecutionException;

public class PlaceListActivity extends AppCompatActivity {

    //variables
    private FusedLocationProviderClient client;
    //radius for searching the nearby essential places
    private int PROXIMITY_RADIUS = 1500;
    // list of hospitals to be displayed
    // button to view all the nearby essential places
    private Button mapsAcitivity;
    ArrayList<String> list = new ArrayList<String>();
    private EditText searchBar;

    // Latitudes and Longitudes of all the essential places
    ArrayList<NearbyPlaces> nearbyList;
    NearbyPlaces temp_loc;
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
    int store_type = 0;
  
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);

        //Initialization of Elements in layout file
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
        store_type = extras.getInt("number");

        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        listview.setClickable(true);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String n = listview.getItemAtPosition(position).toString();
                final int index = n.indexOf('\t');
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
                                if(nearbyList.get(index).in_database == true) {
                                    Intent i = new Intent(PlaceListActivity.this, CitizenPurchaseActivity.class);
                                    i.putExtra("store_info", nearbyList.get(index).toString());
                                    startActivity(i);
                                    dialog.cancel();
                                }
                                else{
                                    serviceUnavailableNotification();
                                }
                            }
                        });

                        two.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=" + Latitude + "," + Longitude + "&daddr=" + nearbyList.get(index).getLatitude() + "," + nearbyList.get(index).getLongitude()));
                                i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                                startActivity(i);
                                dialog.cancel();
                            }
                        });
                    }
                    else if (store_type == 4){

                        one.setText("Yes");
                        two.setText("No");
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

                }
                else{
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=" + Latitude + "," + Longitude + "&daddr=" + nearbyList.get(index).getLatitude() + "," + nearbyList.get(index).getLongitude()));
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
                    String location_type = "";

                    JSONArray jsonArray = null;
                    JSONObject jsonObject;
                    int placesCount = 0;
                    switch (store_type) {
                        case 1:     location_type = "pharmacy|drugstore";    break;
                        case 2:     location_type = "grocery_or_supermarket";     break;
                        case 3:     location_type = "atm";   break;
                        case 4:     location_type = "hospital";   break;
                        default:    Log.d("errtag", "Unexpected entry! check DashboardCitizenActivity");
                    }
                    try{
                        jsonArray = getAllresults(Latitude, Longitude, location_type);
                        placesCount = jsonArray.length();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    nearbyList = new ArrayList<NearbyPlaces>(placesCount);
                    db = FirebaseFirestore.getInstance();

                    for (int i = 0; i < placesCount; i++) {
                        try {
                            jsonObject = (JSONObject) jsonArray.get(i);
                            Point coordinate = new Point(Double.parseDouble(jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat")), Double.parseDouble(jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng")));
                            temp_loc = new NearbyPlaces(jsonObject.getString("name"),coordinate);
                            nearbyList.add(temp_loc);
                            mapCoordinates.put(temp_loc.getPlaceName(), coordinate);
                            list.add(temp_loc.getPlaceName()+"\t\t:"+0);
                            adapter.notifyDataSetChanged();
                          
                        } catch (JSONException e) {
                            Log.d("Places", "Error in Adding places");
                            e.printStackTrace();
                        }
                    }
                    //append the registered stores in the proximity radius to the list
                    CollectionReference addref = db.collection("store");
                  
                    //get the minimum and maximum latitudes and longitudes
                    LatLng[] latLng1 = boundingCoordinates(PROXIMITY_RADIUS);
                    Log.d("mmtag", "val:" + latLng1[0]+""+latLng1[1]);

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
  
    private void queryfunArraylist(Query addquery, Query addquery1) {

        addquery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint("WrongConstant")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                ArrayList<String> strr = new ArrayList<>();
                int shop_check =0;
                if (task.isSuccessful()) {
                    Integer lcc = 0;
                    //check if list is empty
                    if (task.getResult().getDocuments().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String service_category = (String) document.get("service_category");
                            Log.d("mt", service_category);
                            Integer service_category_no = 0;
                            switch(service_category){
                                case "Pharmacy": service_category_no = 1; break;
                                case "Grocery Shop" : service_category_no = 2;   break;
                                case "Bank"   : service_category_no = 3;    break;
                                case "Hospital" : service_category_no = 4; break;
                                default:
                            }

                            if(service_category_no == store_type) {
                                shop_check =1;
                                String lati = document.getData().get("latitude").toString();

                                String longi = document.getData().get("longitude").toString();

                                String unique_id = document.getId();

                                strr.add(document.getData().get("shop_name").toString() + "\t\t:" + lcc + "|" + lati + "|" + longi + "|" + unique_id);
                            }
                        }
                                if (shop_check == 1)
                                updatelist(strr);

                    }
                    else{
                        Toast.makeText(getApplicationContext(), "There are no registered stores in the database:(", 1000).show();
                    }
                }
            }
        });


        addquery1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @SuppressLint("WrongConstant")
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int shop_check = 0;
                ArrayList<String> strr = new ArrayList<>();
                if (task.isSuccessful()) {
                    Integer lcc = 0;
                    //check if list is empty
                    if (task.getResult().getDocuments().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //string containing the nearby stores
                            String service_category = (String) document.get("service_category");
                            Integer service_category_no = 0;
                            switch (service_category) {
                                case "Pharmacy":
                                    service_category_no = 1;
                                    break;
                                case "Grocery Shop":
                                    service_category_no = 2;
                                    break;
                                case "Bank":
                                    service_category_no = 3;
                                    break;
                                case "Hospital":
                                    service_category_no = 4;
                                    break;
                                default:
                            }

                            if (service_category_no == store_type) {
                                shop_check = 1;
                                String lati = document.getData().get("latitude").toString();

                                String longi = document.getData().get("longitude").toString();

                                String unique_id = document.getId();

                                strr.add(document.getData().get("shop_name").toString() + "\t\t:" + lcc + "|" + lati + "|" + longi + "|" + unique_id);
                            }
                        }
                        if (shop_check == 1)
                            updatelist(strr);
                    } else {
                        Toast.makeText(getApplicationContext(), "There are no registered stores in the database:(", 1000).show();
                    }
                }
            }
        });
    }
  
    private void updateLCC(Integer lcc, String PlaceName, String doc_id){
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
                String temp_str = (String) iterator.next();
                String answer = temp_str.split("|")[0];
                list.add(answer);
                adapter.notifyDataSetChanged();
            }
            ArrayList<String> registeredResults = RegStores.get(0);
            for(String temp: registeredResults){
              String[] parts = temp.split("|");
              String lcc_shopName = parts[0];
              String shopName = lcc_shopName.split("\t\t")[0];
              Double lati = Double.parseDouble(parts[1]);
              Double longi = Double.parseDouble(parts[2]);
              Point tempCoordinate = new Point(lati, longi);
              String unique_id = parts[3];
              NearbyPlaces np = new NearbyPlaces(shopName, tempCoordinate);
              np.setShop_unique_id(unique_id);
              nearbyList.add(np);
            }
            list = new ArrayList<String>(new LinkedHashSet<String>(list));
            //The line below is throwing error because the RegStores list is empty
            //nearbyList = new ArrayList<NearbyPlaces>(new LinkedHashSet<NearbyPlaces>(nearbyList));
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
        JSONArray jsonArray1 = jsonObject.getJSONArray("results");
        return jsonArray1;
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

    private void serviceUnavailableNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Service Unavailable !x!");

        final TextView input = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText("We're Sorry, but the service is not currently offered for this outlet");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public LatLng[] boundingCoordinates(double distance) {
        double radLat = Math.toRadians(Latitude);
        Log.d("LATLON","val:"+Latitude+" "+Longitude);
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

class NearbyPlaces{

    private String place_name;
    public Point coordinate;
    public boolean in_database;
    private String shop_unique_id;

    public NearbyPlaces(String place_name, Point coordinate){
        this.place_name = place_name;
        this.coordinate = coordinate;
        in_database = false;
        shop_unique_id = null;
    }

    public Double getLatitude(){
        return coordinate.latitude;
    }

    public Double getLongitude(){
        return coordinate.longitude;
    }

    public String getPlaceName(){
        return place_name;
    }
    public int hashCode(){
      int hashcode = 0;
      hashcode += place_name.hashCode();
      hashcode += shop_unique_id.hashCode();
      return hashcode;
    }
     
    public boolean equals(Object obj){
        boolean result = Boolean.parseBoolean(null);
      if (obj instanceof NearbyPlaces) {
        NearbyPlaces pp = (NearbyPlaces) obj;
        if(pp.place_name.equals(this.place_name) && pp.shop_unique_id.equals(this.shop_unique_id))
        result = true;
      } 
      else{
        result = false;
      }
      return result;
    }

    @Override
    public String toString(){
        String result = String.valueOf(coordinate.latitude) + "|" + String.valueOf(coordinate.longitude) + "|" + place_name + "|" + String.valueOf(in_database) + "|" + shop_unique_id;
        return result;
    }

    public void setShop_unique_id(String doc_id){
        shop_unique_id = doc_id;
        in_database = true;
    }

}

class Point {
    double latitude;
    double longitude;

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
