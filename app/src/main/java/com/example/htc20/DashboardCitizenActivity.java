package com.example.htc20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardCitizenActivity extends AppCompatActivity {

    private Button getHospitals;
    private FloatingActionButton Fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_citizen);
        getHospitals = findViewById(R.id.btn_viewHospitals);
        getHospitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardCitizenActivity.this, HospitalListActivity.class));
            }
        });

        Fab = findViewById(R.id.fab_scanQR);
        Fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardCitizenActivity.this, SecondActivity.class));
            }
        });

    }
}
