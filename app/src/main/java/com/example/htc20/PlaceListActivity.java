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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.Map;
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
    // Latitudes and Longitudes of all the essential places
    double[] Latitudes;
    double[] Longitudes;
    //global variables as location will be used outside the local blocks
    double Latitude = 0;
    double Longitude = 0;
    //serial number of the essential places to be displayed
    int count = 1;

    private SeekBar sb_distance;
    private TextView tv_distance;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);

        //Initialization of Elements in layout file
        places_list = findViewById(R.id.lv_places_list);
        final ListView listview = (ListView) findViewById(R.id.lv_places_list);
        mapsAcitivity = findViewById(R.id.btn_mapsActivityLauncher);
        sb_distance = findViewById(R.id.sb_distance);
        tv_distance = findViewById(R.id.tv_distance);

        sb_distance.setMin(3);

        tv_distance.setText("Proximity Radius: " + sb_distance.getMin() * 0.5 + " km.");

        setupList(listview);
        setupSeekBar(listview, sb_distance, tv_distance);

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
                setupList(listview);
            }
        });
    }

    private void setupList(final ListView listview) {
        final ArrayList<String> list = new ArrayList<String>();

        //specify the type of service
        Bundle extras = getIntent().getExtras();
        final int store_type = extras.getInt("number");

        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setClickable(true);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String n = listview.getItemAtPosition(position).toString();
                final int index = n.charAt(0) - '1';

                //add pop up with two buttons
                if(store_type == 1 || store_type == 2) {
                    Dialog dialog = new Dialog(PlaceListActivity.this);
                    dialog.setContentView(R.layout.custom_dialog_layout);
                    Button order = (Button) dialog.getWindow().findViewById(R.id.btn_order);
                    Button directions = (Button) dialog.getWindow().findViewById(R.id.btn_directions);
                    order.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onClick(View v) {
                            popUpEditText();
                        }
                    });
                    directions.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?saddr=" + Latitude + "," + Longitude + "&daddr=" + (Latitudes[index]) + "," + (Longitudes[index])));
                            i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                            startActivity(i);
                        }
                    });
                    dialog.show();
                }else{
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=" + Latitude + "," + Longitude + "&daddr=" + (Latitudes[index]) + "," + (Longitudes[index])));
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
                            case 1:     nearbyPlace = "pharmacy|drugstore";    break;
                            case 2:     nearbyPlace = "grocery_or_supermarket";     break;
                            case 3:     nearbyPlace = "atm";   break;
                            case 4:     nearbyPlace = "department_store";   break;
                            default:    Log.d("errtag", "Unexpected entry! check DashboardCitizenActivity");
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


                    db = FirebaseFirestore.getInstance();

                    Latitudes = new double[placesCount];
                    Longitudes = new double[placesCount];

                    Log.d("Loctag", "value: " + placesCount);

                    for (int i = 0; i < placesCount; i++) {
                        try {

                            jsonObject = (JSONObject) jsonArray.get(i);
                            PlaceName = jsonObject.getString("name");
                            PlaceLat = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat");
                            PlaceLong = jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng");
                            Log.d("Latitudes", "valueLat :" + PlaceLat);
                            Log.d("Longitudes", "valueLong : " + PlaceLong);
                            Latitudes[i] = Double.parseDouble(PlaceLat);
                            Longitudes[i] = Double.parseDouble(PlaceLong);
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
                            list.add(count + ". " + PlaceName + "\t\t: " + String.valueOf(lcc));
                            count++;
                            adapter.notifyDataSetChanged();
                            //parsing to be done
                        } catch (JSONException e) {
                            Log.d("Places", "Error in Adding places");
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

        mapsAcitivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude);
                switch (store_type) {
                    case 1:     gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude + "?q=pharmacy|drugstore"); break;
                    case 2:     gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude + "?q=grocery_or_supermarket|store");  break;
                    case 3:     gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude + "?q=atm");  break;
                    case 4:     gmmIntentUri = Uri.parse("geo:" + Latitude + "," + Longitude + "?q=department_store");
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

    private void popUpEditText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please enter your order:");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // do something here on OK
                Toast.makeText(getApplicationContext(), "Enter your order", 200).show();
                String myorder = input.getText().toString();
                Log.d("myorder",myorder);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

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



