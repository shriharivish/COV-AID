package com.example.htc20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
//import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText Email, Password;

    private Button Login;
    private Button LoginPhone;
    private TextView Register;
    //    private int count = 5;
    private FirebaseAuth fbauth;
    private ProgressDialog progressDialog;
    private ProgressBar progressBarLogin;

//    private ToggleButton PassVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Email = findViewById(R.id.etEmail);
        Password = findViewById(R.id.etPassword);
        Login = findViewById(R.id.btnLogin);
        LoginPhone = findViewById(R.id.btnLoginPhone);
        Register = findViewById(R.id.tvRegister);
        progressBarLogin = findViewById(R.id.pb_Login);
//        PassVisibility = (ToggleButton)findViewById(R.id.tbPassVisibility);

//        PassVisibility.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(Password.getText().toString().isEmpty())
//                {
//                    Password.setError("Please Enter Password");
//                }
//
//                else
//                {
//                    if(PassVisibility.getText().toString().equals("Show Password"))
//                    {
//                        PassVisibility.setText("Hide Password");
//                        Password.setTransformationMethod(null);
//                    }
//
//                    else {
//                        PassVisibility.setText("Show Password");
//                        Password.setTransformationMethod(new PasswordTransformationMethod());
//                    }
//                }
//            }
//        });

        fbauth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);


        FirebaseUser user = fbauth.getCurrentUser();

        if (user != null) {
            finish();
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
        }

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarLogin.setVisibility(View.VISIBLE);
                Validate(Email.getText().toString(), Password.getText().toString());

            }
        });

        LoginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PhoneLoginActivity.class));
            }
        });
    }

    private void Validate(String userName, String userPass) {

        progressDialog.setMessage("Logging you in...");
        fbauth.signInWithEmailAndPassword(userName, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, SecondActivity.class));
                } else {
//                    count --;
                    Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
//                    if(count == 0) {
//                        Login.setEnabled(false);
//                    }
                }
            }
        });
    }
}
