package com.example.htc20;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HospitalListActivity extends AppCompatActivity {

    String hospitals[] = {"Hospital1", "Hospital2", "Hospital3", "Hospital4", "Hospital5"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_list);
        ListView hospital_list = findViewById(R.id.lv_hospitalList);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_hospital_list, hospitals);

        hospital_list.setAdapter(adapter);
    }
}
