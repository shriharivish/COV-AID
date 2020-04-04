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
import android.widget.Spinner;
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

public class StoreRegistrationActivity extends AppCompatActivity {
    private EditText userEmail, uniqueID, shopName;
    private TextInputEditText userPassword;
    private Button register;
    private TextView userLogin;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private MapsActivity map;
    private Spinner service_category;

    private static final String TAG = "DocSnippets";

    protected void setup(){
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_registration);

        userPassword = (TextInputEditText) findViewById(R.id.etSetPassword);
        userEmail = (EditText) findViewById(R.id.etEmail);
        userLogin = (TextView) findViewById(R.id.etNotLogin);
        register = (Button) findViewById(R.id.etRegister);
        uniqueID = (EditText) findViewById(R.id.etUniqueID);
        shopName = (EditText) findViewById(R.id.etShopName);
        service_category = (Spinner) findViewById(R.id.etCategories);
        // map = (MapActivity)......................

        firebaseAuth = FirebaseAuth.getInstance();
//        setup();
        db = FirebaseFirestore.getInstance();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    final String email = userEmail.getText().toString().trim();
                    String pass = userPassword.getText().toString().trim();
                    String strUniqueID = uniqueID.getText().toString().trim();
                    String shop_name = shopName.getText().toString().trim();
                    String category = String.valueOf(service_category.getSelectedItem());

                    Map<String, Object> user = new HashMap<>();
                    user.put("unique_id", strUniqueID);
                    user.put("email", email);
                    user.put("shop_name", shop_name);
                    user.put("service_category", category);

                    db.collection("store")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                    String temp_email = uniqueID + "@htc2020.com";
                    firebaseAuth.createUserWithEmailAndPassword(temp_email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendEmailVerification(email);

                            } else {
                                Toast.makeText(StoreRegistrationActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private Boolean validate() {
        Boolean result = false;
        String strUniqueID = uniqueID.getText().toString();
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String shop_name = shopName.getText().toString();
        String category = String.valueOf(service_category.getSelectedItem());

        if (strUniqueID.isEmpty() || email.isEmpty() || password.isEmpty() || category.equalsIgnoreCase("Choose a Service category") || shop_name.isEmpty()) {
            Toast.makeText(this, "Please Enter All Details", Toast.LENGTH_SHORT).show();
        }
        else {
            result = true;
        }
        return result;
    }

    private void sendEmailVerification(String email) {
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        final String temp_email = firebaseUser.getEmail();
        firebaseUser.updateEmail(email);
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(StoreRegistrationActivity.this, "A Verification Link has been sent to you E-mail", Toast.LENGTH_LONG).show();
                        firebaseAuth.signOut();
                        finish();
                        if (temp_email != null) {
                            firebaseUser.updateEmail(temp_email);
                        }
                    } else {
                        Toast.makeText(StoreRegistrationActivity.this, "Please Try Again Later or check if entered email is correct", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
