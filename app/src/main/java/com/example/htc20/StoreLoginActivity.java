package com.example.htc20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StoreLoginActivity extends AppCompatActivity {

    private EditText uniqueID;
    private TextInputEditText password;
    private Button login;
    private TextView notRegister;
    //    private int count = 5;
    private FirebaseAuth fbauth;
    private ProgressDialog progressDialog;
    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_login);

        password = (TextInputEditText) findViewById(R.id.etPassword);
        uniqueID = (EditText) findViewById(R.id.editUniqueID);
        login = (Button) findViewById(R.id.etLogin);
        notRegister = (TextView) findViewById(R.id.etNotRegistered);
        progressBarLogin = findViewById(R.id.pb_Login);

        fbauth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        FirebaseUser user = fbauth.getCurrentUser();

//        if (user != null) {
//            finish();
//            startActivity(new Intent(StoreLoginActivity.this, DashboardStoreActivity.class));
//        }

        notRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoreLoginActivity.this, StoreRegistrationActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = uniqueID.getText().toString().trim() + "@htc2020.com";
                if(smallValidate(uniqueID.getText().toString().trim(), password.getText().toString().trim())) {
                    Validate(email, password.getText().toString().trim());
                    progressBarLogin.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    private boolean smallValidate(String userName, String password){
        boolean result = false;
        if(userName.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please Enter All Details", Toast.LENGTH_SHORT).show();
        }
        else{
            result = true;

        }
        return result;
    }

    private void Validate(String userName, String userPass) {

        progressDialog.setMessage("Logging you in...");
        fbauth.signInWithEmailAndPassword(userName, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBarLogin.setVisibility(View.GONE);
                if (task.isSuccessful()) {
//                    checkEmailVerification();
                    Toast.makeText(StoreLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(StoreLoginActivity.this, DashboardStoreActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    finish();
                    startActivity(intent);
                } else {
//                    count --;
                    Toast.makeText(StoreLoginActivity.this, String.valueOf(task.getException()), Toast.LENGTH_SHORT).show();
//                    if(count == 0) {
//                        Login.setEnabled(false);
//                    }
                }
            }
        });
    }
//
//    private void checkEmailVerification() {
//        FirebaseUser firebaseUser = fbauth.getInstance().getCurrentUser();
//        Boolean emailflag = firebaseUser.isEmailVerified();
//
//        if (emailflag) {
//            Toast.makeText(StoreLoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(StoreLoginActivity.this, DashboardStoreActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            finish();
//            startActivity(intent);
//        } else {
//            Toast.makeText(StoreLoginActivity.this, "Verify Your Email", Toast.LENGTH_LONG).show();
//            fbauth.signOut();
//        }
//    }
}
