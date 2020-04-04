package com.example.htc20;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView distance;

    private Button openMaps;
    private FusedLocationProviderClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d("Test_LOG : ", "onCreate()");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    final double Latitude = location.getLatitude();
                    final double Longitude = location.getLongitude();
                    LatLng myLatLng = new LatLng(Latitude, Longitude);
                    LatLng destLatLng = new LatLng(Latitude -0.01,Longitude-0.01);
                    mMap.addMarker(new MarkerOptions().position(myLatLng).title("My Location"));
                    mMap.addMarker(new MarkerOptions().position(destLatLng).title("My Destination"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

                    //open maps by clicking on the button
                    openMaps = (Button) findViewById(R.id.op_maps);
                    openMaps.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            // click handling code
                            //shows nearby hospitals on maps
                            //Uri gmmIntentUri = Uri.parse("geo:"+Latitude+","+Longitude+"&mode=driving");
                            //Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            //mapIntent.setPackage("com.google.android.apps.maps");
                            //if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            //    startActivity(mapIntent);
                            //}
                            Intent i = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("http://maps.google.com/maps?saddr="+Latitude+","+Longitude+"&daddr="+(Latitude-0.01)+","+(Longitude-0.01)));
                            //i.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                            startActivity(i);



                        }
                    });

                }
            }
        });


    }




     /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //get my location


        // Add a marker at both locations and move the camera
        //mMap.addMarker(new MarkerOptions().position(myLatLng).title("My Location"));
        //mMap.addMarker(new MarkerOptions().position(destLoc).title("Destination"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(destLoc));
    }


}
