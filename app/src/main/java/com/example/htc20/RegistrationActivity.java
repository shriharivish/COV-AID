package com.example.htc20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText userFirstName, userEmail, userLastName, userPhoneNumber;
    private TextInputEditText userPassword;
    private Button register;
    private TextView userLogin;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private static final String TAG = "DocSnippets";

    protected void setup(){
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        userFirstName = (EditText) findViewById(R.id.etFirstName);
        userLastName = (EditText) findViewById(R.id.etLastName);
        userPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        userPassword = (TextInputEditText) findViewById(R.id.etUserPassword);
        userEmail = (EditText) findViewById(R.id.etUserEmail);
        userLogin = (TextView) findViewById(R.id.tvLogin);
        register = (Button) findViewById(R.id.btnRegister);

        firebaseAuth = FirebaseAuth.getInstance();
//        setup();
        db = FirebaseFirestore.getInstance();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    String user_email = userEmail.getText().toString().trim();
                    String user_pass = userPassword.getText().toString().trim();
                    String user_firstName = userFirstName.getText().toString().trim();
                    String user_lastName = userLastName.getText().toString().trim();
                    Long user_phoneNumber = Long.parseLong(userPhoneNumber.getText().toString().trim());

                    Map<String, Object> user = new HashMap<>();
                    user.put("first_name", user_firstName);
                    user.put("last_name", user_lastName);
                    user.put("phone_number", user_phoneNumber);
                    user.put("email", user_email);
                    db.collection("citizen")
                            .document(user_email+String.valueOf(user_phoneNumber))
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot added");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendEmailVerification();

                            } else {
                                Toast.makeText(RegistrationActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            }
        });

    }

    private Boolean validate() {
        Boolean result = false;
        String firstName = userFirstName.getText().toString();
        String lastName = userLastName.getText().toString();
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String temp = userPhoneNumber.getText().toString().trim();
        Long phoneNumber = Long.parseLong(temp);

        if (firstName.isEmpty() || email.isEmpty() || password.isEmpty() || lastName.isEmpty() || phoneNumber == null) {
            Toast.makeText(this, "Please Enter All Details", Toast.LENGTH_SHORT).show();
        }
        else {
            result = true;
        }

        return result;
    }

    private void sendEmailVerification() {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegistrationActivity.this, "A Verification Link has been sent to you E-mail", Toast.LENGTH_LONG).show();
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Please Try Again Later", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
