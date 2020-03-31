package com.example.htc20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SecondActivity extends AppCompatActivity {

    private Button Logout;
    private FirebaseAuth fbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Logout = (Button) findViewById(R.id.btnLogout);
        fbase = FirebaseAuth.getInstance();

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbase.signOut();
                finish();
                Toast.makeText(SecondActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SecondActivity.this, MainActivity.class));
            }
        });


    }
}
