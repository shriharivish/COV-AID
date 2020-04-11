package com.example.htc20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SecondActivity extends AppCompatActivity {

    private FirebaseAuth fbase;
    CameraView cameraView;
    boolean isDetected = false;
    Button btn_start_again;
    FirebaseVisionBarcodeDetectorOptions options;
    FirebaseVisionBarcodeDetector detector;
    private FirebaseFirestore db;
    int flag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        fbase = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Dexter.withActivity(this)
                .withPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        setupCamera();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();


    }

    private void setupCamera() {
        btn_start_again = findViewById(R.id.btn_again);
        btn_start_again.setEnabled(isDetected);
        btn_start_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDetected = !isDetected;
                btn_start_again.setEnabled(isDetected);
            }
        });

        cameraView = findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                processImage(getVisionImageFromFrame(frame));
            }
        });
        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build();

        detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);


    }

    private FirebaseVisionImage getVisionImageFromFrame(Frame frame) {
        byte[] data = frame.getData();
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setHeight(frame.getSize().getHeight())
                .setWidth(frame.getSize().getWidth())
                .build();

        return FirebaseVisionImage.fromByteArray(data, metadata);
    }

    private void processImage(FirebaseVisionImage image) {
        if (!isDetected) {
            detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                            processResult(firebaseVisionBarcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SecondActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void processResult(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        // Add code to push the lcc updates to database
        if (firebaseVisionBarcodes.size() > 0) {
            btn_start_again.setEnabled(isDetected);
            for (FirebaseVisionBarcode item : firebaseVisionBarcodes) {
                int value_tpe = item.getValueType();
                switch (value_tpe) {
                    case FirebaseVisionBarcode.TYPE_TEXT: {
                        String text = item.getRawValue();
                        int index = text.indexOf('#');
                        final String id = text.substring(0, index);
                        final String email = fbase.getCurrentUser().getEmail();
                        if (isSubstring("entry", text) != -1) {

                            db.collection("citizen").document(email)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    boolean val;
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        val = (Boolean) documentSnapshot.get("scanned");
                                        if (!val) {
                                            db.collection("store")
                                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                                            if (documentSnapshot.getId().equalsIgnoreCase(id)) {
                                                                long lcc = (Long) documentSnapshot.get("lcc");


                                                                lcc++;
                                                                Toast.makeText(SecondActivity.this, "Entry has been recorded!", Toast.LENGTH_LONG).show();
                                                                flag = 1;

                                                                db.collection("store").document(id).update("lcc", lcc);
                                                                db.collection("citizen").document(email).update("scanned", true);

                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        } else {

                                            Toast.makeText(SecondActivity.this, "Your entry is already recorded", Toast.LENGTH_LONG).show();

                                        }
                                    }
                                }
                            });

                            finish();
                            startActivity(new Intent(SecondActivity.this, DashboardCitizenActivity.class));
                        } else if (isSubstring("exit", text) != -1) {
                            db.collection("citizen").document(email)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    boolean val;
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        val = (Boolean) documentSnapshot.get("scanned");
                                        if (val) {
                                            db.collection("store")
                                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                                            if (documentSnapshot.getId().equalsIgnoreCase(id)) {
                                                                long lcc = (Long) documentSnapshot.get("lcc");


                                                                lcc--;
                                                                Toast.makeText(SecondActivity.this, "Entry has been recorded!", Toast.LENGTH_LONG).show();
                                                                flag = 1;

                                                                db.collection("store").document(id).update("lcc", lcc);
                                                                db.collection("citizen").document(email).update("scanned", false);

                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        } else {

                                            Toast.makeText(SecondActivity.this, "Record your entry first", Toast.LENGTH_LONG).show();

                                        }
                                    }
                                }
                            });


                            finish();
                            startActivity(new Intent(SecondActivity.this, DashboardCitizenActivity.class));
                        } else {
                            createDialog("Code is not recognized!");
                        }


                    }
                    break;



                    default:
                        createDialog("Code is not recognized !");
                        break;
                }
            }
        }
    }

    private void createDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    static int isSubstring(String s1, String s2) {
        int M = s1.length();
        int N = s2.length();

        /* A loop to slide pat[] one by one */
        for (int i = 0; i <= N - M; i++) {
            int j;

            /* For current index i, check for
            pattern match */
            for (j = 0; j < M; j++)
                if (s2.charAt(i + j) != s1.charAt(j))
                    break;

            if (j == M)
                return i;
        }

        return -1;
    }

}
