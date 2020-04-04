package com.example.htc20;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.rb_citizen:
                if (checked) {
                    startActivity(new Intent(LauncherActivity.this, MainActivity.class));
                }
                break;
            case R.id.rb_storeowner:
                if (checked) {
                    startActivity(new Intent(LauncherActivity.this, StoreLoginActivity.class));
                }
                break;
        }
    }
}
