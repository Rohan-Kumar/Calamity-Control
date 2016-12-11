package com.nest.calamitycontrol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 54321;
    Spinner spinner;
    ImageView imageView;
    int selectedCalamity = 0;
    boolean selectedImage = false;
    List<String> list = new ArrayList<String>();
//    ProgressDialog dialog;
    TextInputEditText description;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        selectedImage = false;

        spinner = (Spinner) findViewById(R.id.spinner);
        description = (TextInputEditText) findViewById(R.id.desc);

        list.add("Select a Calamity");
        list.add("Earthquake");
        list.add("Tsunami");
        list.add("Cyclone");
        list.add("Hurricane");
        list.add("Tornado");
        list.add("Floods");
        list.add("Drought");
        list.add("Others");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCalamity = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });
    }

    private void sendData() {
        if (selectedCalamity == 0)
            Toast.makeText(this, "Please select something to report", Toast.LENGTH_SHORT).show();
        else {

            if (selectedImage) {
//                dialog.setTitle("Sending Report");
//                dialog.setMessage("Please wait...");
//                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://calamity-control-1478121312942.appspot.com");
                storageRef = storageRef.child(getCurrentTimeStamp().replaceAll(" ", "") + ".png");
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = imageView.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = storageRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                        dialog.dismiss();
                        Log.d("TAG", "onSuccess: " + downloadUrl);
//                        startActivity(new Intent(ReportActivity.this, MainActivity.class));
//                        finish();

                    }
                });
            }

            DatabaseReference databaseRef;
            databaseRef = FirebaseDatabase.getInstance().getReference("reports");
            String key = databaseRef.push().getKey();

            final Map<String, Object> postValues = new HashMap<>();
            postValues.put("lat", latitude);
            postValues.put("lng", longitude);
            postValues.put("time", getCurrentTimeStamp());
            postValues.put("description", description.getText().toString());
            postValues.put("calamity", list.get(selectedCalamity));

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + key, postValues);

            databaseRef.updateChildren(childUpdates);

            new HttpCall("http://204.152.203.111/test-cgi/genTweet.py?tweet=" + URLEncoder.encode(description.getText().toString() + " #CalamityControl"), findViewById(R.id.content_report),ReportActivity.this).execute();


            Toast.makeText(ReportActivity.this, "Thank you for your registering as a volunteer", Toast.LENGTH_SHORT).show();
            if (!selectedImage) {
//                startActivity(new Intent(ReportActivity.this, MainActivity.class));
//                finish();
            }

        }

    }

    public void selectPicture(View view) {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImage = true;
            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date());

            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }


}
