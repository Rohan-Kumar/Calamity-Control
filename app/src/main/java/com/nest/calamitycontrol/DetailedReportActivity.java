package com.nest.calamitycontrol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.google.firebase.storage.OnProgressListener;
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

public class DetailedReportActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 54321;
    Spinner calamityType, calamityLevel;
    ImageView imageView;
    int selectedCalamity = 0;
    int selectedLevel = 0;
    boolean selectedImage = false;
    List<String> list = new ArrayList<String>();
    List<String> list2 = new ArrayList<String>();
    ProgressDialog dialog;
    TextInputEditText description, area, city, landmark, phone;


    double latitude;
    double longitude;

    Uri downloadUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        selectedImage = false;

        calamityType = (Spinner) findViewById(R.id.calamityType);
        calamityLevel = (Spinner) findViewById(R.id.level);
        description = (TextInputEditText) findViewById(R.id.descEt);
        area = (TextInputEditText) findViewById(R.id.areaEt);
        city = (TextInputEditText) findViewById(R.id.cityEt);
        landmark = (TextInputEditText) findViewById(R.id.landmarkEt);
        phone = (TextInputEditText) findViewById(R.id.phoneEt);

        list.add("Select a Calamity");
        list.add("Cyclone");
        list.add("Earthquake");
        list.add("Tsunami");
        list.add("Hurricane");
        list.add("Tornado");
        list.add("Floods");
        list.add("Drought");
        list.add("Others");

        list2.add("Select Level");
        list2.add("1 (I'm safe)");
        list2.add("2");
        list2.add("3");
        list2.add("4");
        list2.add("5 (I'm in danger!)");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        calamityType.setAdapter(dataAdapter);
        calamityType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCalamity = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        calamityLevel.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list2));
        calamityLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedLevel = i;
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

        if (selectedCalamity == 0 || selectedLevel == 0)
            Toast.makeText(this, "Please select something to report", Toast.LENGTH_SHORT).show();
        else {



            if(area.getText().toString() == null || area.getText().toString().equals("") || city.getText().toString() == null || city.getText().toString().equals("")){
                Toast.makeText(this, "Please fill all the required details", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog = new ProgressDialog(DetailedReportActivity.this);
            dialog.setMessage("loading...");
            dialog.show();

            updateDatabase();
        }

    }

   /* private void sendData() {
        if (selectedCalamity == 0)
            Toast.makeText(this, "Please select something to report", Toast.LENGTH_SHORT).show();
        else {
            if (selectedImage) {
//                dialog.setTitle("Sending Report");
//                dialog.setMessage("Please wait...");
//                dialog.show();

            }else
                updateDatabase();



        }

    }*/

    private void updateDatabase() {

        DatabaseReference databaseRef;
        databaseRef = FirebaseDatabase.getInstance().getReference("reports");
        String key = databaseRef.push().getKey();

        final Map<String, Object> postValues = new HashMap<>();
        postValues.put("lat", latitude);
        postValues.put("lng", longitude);
        postValues.put("time", getCurrentTimeStamp());
        postValues.put("description", description.getText().toString());
        postValues.put("calamity", list.get(selectedCalamity));
        postValues.put("level", list2.get(selectedLevel));
        postValues.put("area", area.getText().toString());
        postValues.put("city", city.getText().toString());
        postValues.put("landmark", landmark.getText().toString()+",");
        postValues.put("phone", phone.getText().toString());

        if (selectedImage) {
            postValues.put("isImagePresent", true);
        }

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + key, postValues);

        databaseRef.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                downloadUrl = null;
                Log.d("TAG", "onSuccess: updated to db");
                if (!selectedImage) {
                    dialog.dismiss();
                    Toast.makeText(DetailedReportActivity.this, "Reported Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        new HttpCall("http://204.152.203.111/test-cgi/genTweet.py?tweet=" + URLEncoder.encode(description.getText().toString() + " #CalamityControl"), findViewById(R.id.content_report), DetailedReportActivity.this, new HttpCall.CallBack() {
            @Override
            public void completed() {

            }
        }).execute();


        if (selectedImage) {
            uploadPic(key);
        } else {
//            dialog.dismiss();
//                startActivity(new Intent(ReportActivity.this, MainActivity.class));
//                finish();
        }

    }

    private void uploadPic(String key) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://calamity-control-1478121312942.appspot.com/images");
//        storageRef = storageRef.child(getCurrentTimeStamp().replaceAll(" ", "") + ".png");
        storageRef = storageRef.child(key + ".png");
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure( Exception exception) {
                // Handle unsuccessful uploads
                dialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d("TAG", "onSuccess: " + downloadUrl);
                dialog.dismiss();
//                        startActivity(new Intent(ReportActivity.this, MainActivity.class));
                Toast.makeText(DetailedReportActivity.this, "Reported Successfully!", Toast.LENGTH_SHORT).show();
                finish();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("TAG", "onProgress: " + taskSnapshot.getBytesTransferred() + " / " + taskSnapshot.getTotalByteCount());
            }
        });
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
