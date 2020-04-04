package com.example.htc20;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class DashboardStoreActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    EditText qrCodeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_store);

        button = findViewById(R.id.btn_getQrCode);
        qrCodeText = findViewById(R.id.et_qrCodeText);
        imageView = findViewById(R.id.img_qrCodeImage);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = qrCodeText.getText().toString();
                if (!text.equals("")) {
                    new ImageDownloaderTask(imageView).execute("https://api.qrserver.com/v1/create-qr-code/?size=1000x1000&data=" + text);
                } else {
                    Toast.makeText(DashboardStoreActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
