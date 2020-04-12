package com.example.htc20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
//import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText Email;
    private TextInputEditText Password;

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

        Email = (EditText) findViewById(R.id.etEmail);
        Password = (TextInputEditText) findViewById(R.id.etPassword);
        Login = (Button) findViewById(R.id.btnLogin);
        LoginPhone = (Button) findViewById(R.id.btnLoginPhone);
        Register = (TextView) findViewById(R.id.tvRegister);
        progressBarLogin = (ProgressBar) findViewById(R.id.pb_Login);

        fbauth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);


        FirebaseUser user = fbauth.getCurrentUser();

//        if (user != null) {
//            finish();
//            startActivity(new Intent(MainActivity.this, DashboardCitizenActivity.class));
//            startActivity(new Intent(MainActivity.this, DashboardCitizenActivity.class));
//        }

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(smallValidate(Email.getText().toString(), Password.getText().toString())) {
                    Validate(Email.getText().toString(), Password.getText().toString());
                    progressBarLogin.setVisibility(View.VISIBLE);
                }
            }
        });

        LoginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhoneLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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
                        checkEmailVerification();
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

        private void checkEmailVerification() {
            FirebaseUser firebaseUser = fbauth.getInstance().getCurrentUser();
            Boolean emailflag = firebaseUser.isEmailVerified();

            if (emailflag) {
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DashboardCitizenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Verify Your Email", Toast.LENGTH_LONG).show();
                fbauth.signOut();
            }
        }
}
