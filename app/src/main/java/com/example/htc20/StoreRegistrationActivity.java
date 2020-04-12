package com.example.htc20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StoreRegistrationActivity extends AppCompatActivity {
    private EditText userEmail, uniqueID, shopName;
    private TextInputEditText userPassword;
    private Button register;
    private TextView userLogin;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private Spinner service_category;
    private FusedLocationProviderClient client;
    private LocationRequest mLocationRequest = null;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    double Latitude;
    double Longitude;

    private static final String TAG = "DocSnippets";

    protected void setup(){
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    private void sSetLocationCoordinates(double latitude, double longitude){
        this.Latitude = latitude;
        this.Longitude = longitude;
        Log.d("Latitude_3", "value " + Latitude);
    }

    protected void createLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(100);
            mLocationRequest.setFastestInterval(50);
        }
    }

    //get location
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            client.flushLocations();
            return;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_registration);

        userPassword = findViewById(R.id.etSetPassword);
        userEmail = findViewById(R.id.etEmail);
        userLogin = findViewById(R.id.etNotLogin);
        register = findViewById(R.id.etRegister);
        uniqueID = findViewById(R.id.etUniqueID);
        shopName = findViewById(R.id.etShopName);
        service_category = findViewById(R.id.etCategories);

        client = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        requestLocationPermission();
        client.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        client.getLastLocation().addOnSuccessListener(StoreRegistrationActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    double latitude =  location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d("Latitude_1", "value: " + latitude);
                    sSetLocationCoordinates(latitude, longitude);
                }
                else{
                    Toast.makeText(StoreRegistrationActivity.this, "Unable to access current location of the shop. Please try again in sometime", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StoreRegistrationActivity.this, StoreLoginActivity.class));
                }
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
//        setup();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    final String email = userEmail.getText().toString().trim();
                    String pass = userPassword.getText().toString().trim();
                    String strUniqueID = uniqueID.getText().toString().trim();
                    String shop_name = shopName.getText().toString().trim();
                    String category = String.valueOf(service_category.getSelectedItem());

                    final Map<String, Object> user = new HashMap<>();
                    user.put("unique_id", strUniqueID);
                    user.put("email", email);
                    user.put("shop_name", shop_name);
                    user.put("service_category", category);
                    user.put("lcc", 0);
                    user.put("latitude", Latitude);
                    user.put("longitude", Longitude);
                    Log.d("Latitude_4", "value: " + Latitude);
                    db = FirebaseFirestore.getInstance();
                    db.collection("store")
                            .document(strUniqueID)
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Latitude", "DocumentSnapshot added");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("Latitude", "Error adding document", e);
                                }
                            });

                    String temp_email = strUniqueID + "@htc2020.com";
                    firebaseAuth.createUserWithEmailAndPassword(temp_email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
//                                sendEmailVerification(email);
                                Toast.makeText(StoreRegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(StoreRegistrationActivity.this, StoreLoginActivity.class));

                            } else {

                                Toast.makeText(StoreRegistrationActivity.this, String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoreRegistrationActivity.this, StoreLoginActivity.class));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Latitude", "Reached in OnRequestPermissionsResult");
                        client.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied :( | We need to location permission to run this app", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(StoreRegistrationActivity.this, LauncherActivity.class));
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public void requestLocationPermission() {
        Log.d("Latitude", "REached in permissions");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            Toast.makeText(this, "Location permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean validate() {
        Boolean result = false;
        String strUniqueID = uniqueID.getText().toString();
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String shop_name = shopName.getText().toString();
        String category = String.valueOf(service_category.getSelectedItem());

        if (strUniqueID.isEmpty() || email.isEmpty() || password.isEmpty() || category.equalsIgnoreCase("Choose a Service category") || shop_name.isEmpty()) {
            Toast.makeText(this, "Please Enter All Details", Toast.LENGTH_SHORT).show();
        }
        else {
            result = true;
        }
        return result;
    }
}
