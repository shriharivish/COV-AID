package com.example.htc20;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class DashboardCitizenActivity extends AppCompatActivity {

    private Button getPharmacies;
    private Button getGroceries;
    private Button getBanks;
    private Button getHospitals;

    private FloatingActionButton Fab;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    private FirebaseAuth fbAuth;

    private Button signOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_citizen);
        //view nearby pharmacies
        getPharmacies = findViewById(R.id.btn_viewPharmacies);
        getPharmacies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DashboardCitizenActivity.this, PlaceListActivity.class);
                i.putExtra("number", 1);
                startActivity(i);
            }
        });

        // view nearby groceries and supermarkets
        getGroceries = findViewById(R.id.btn_Groceries);
        getGroceries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DashboardCitizenActivity.this, PlaceListActivity.class);
                i.putExtra("number", 2);
                startActivity(i);
            }
        });

        //view nearby banks and atms
        getBanks = findViewById(R.id.btn_viewBanks);
        getBanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DashboardCitizenActivity.this, PlaceListActivity.class);
                i.putExtra("number", 3);
                startActivity(i);
            }
        });

        // view nearby Hospitals
        getHospitals = findViewById(R.id.btn_viewHospitals);
        getHospitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DashboardCitizenActivity.this, PlaceListActivity.class);
                i.putExtra("number", 4);
                startActivity(i);
            }
        });

        fbAuth = FirebaseAuth.getInstance();
        Fab = findViewById(R.id.fab_scanQR);
        Fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardCitizenActivity.this, SecondActivity.class));
            }
        });

        requestLocationPermission();

        signOut = findViewById(R.id.btn_signout);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbAuth.signOut();
                finish();
                startActivity(new Intent(DashboardCitizenActivity.this, LauncherActivity.class));

            }
        });
    }
    //permission//
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("requestCode", "value : "+requestCode);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();

        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);

        }
    }
}


