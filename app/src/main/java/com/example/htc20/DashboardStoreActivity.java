package com.example.htc20;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardStoreActivity extends AppCompatActivity {

    ImageView imageView_entry, imageView_exit;
    Button button;
    EditText qrCodeText;
    private FirebaseAuth fbAuth;
    private TextView storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_store);

        fbAuth = FirebaseAuth.getInstance();
        imageView_entry = findViewById(R.id.img_qrCodeEntry);
        imageView_exit = findViewById(R.id.img_qrCodeExit);

        FirebaseUser user = fbAuth.getCurrentUser();
        String email = user.getEmail().toString();


        int index = email.indexOf('@');
        String unique_id = email.substring(0, index);

        storeName = findViewById(R.id.tv_store_name);
        storeName.setText(unique_id + "'s Dashboard");

        String text_entry = unique_id + "entry";
        String text_exit = unique_id + "exit";

        new ImageDownloaderTask(imageView_entry).execute("https://api.qrserver.com/v1/create-qr-code/?size=1000x1000&data=" + text_entry);
        new ImageDownloaderTask(imageView_exit).execute("https://api.qrserver.com/v1/create-qr-code/?size=1000x1000&data=" + text_exit);


    }
}
