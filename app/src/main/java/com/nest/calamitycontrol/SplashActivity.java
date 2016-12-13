package com.nest.calamitycontrol;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {

    int permissionCode = 555;
    boolean permissionLocation = true;
    boolean permissionCall = true;
    boolean permissionStorage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        requestPermission();
        enableOfflineData();

    }

    private void enableOfflineData() {

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void requestPermission() {
        if ((ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED))
            thread.start();
        else {
            if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, permissionCode);
                permissionLocation = false;
            }
            if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.CALL_PHONE}, permissionCode + 1);
                permissionCall = false;
            }
            if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, permissionCode + 2);
                permissionStorage = false;
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permissionCode) {
            //If permission is granted
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) && permissionCall && permissionStorage) {
                thread.start();
            } else {
                //Displaying another toast if permission is not granted
//                Intent intent = new Intent(SplashActivity.this, IVRActivity.class);
//                startActivity(intent);
                finish();
            }
        }
        if (requestCode == permissionCode + 1) {
            //If permission is granted
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) && permissionLocation && permissionStorage) {
                thread.start();
            } else {
                //Displaying another toast if permission is not granted
//                Intent intent = new Intent(SplashActivity.this, IVRActivity.class);
//                startActivity(intent);
                finish();
            }
        }
        if (requestCode == permissionCode + 2) {
            //If permission is granted
            if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) && permissionCall && permissionLocation) {
                thread.start();
            } else {
                //Displaying another toast if permission is not granted
//                Intent intent = new Intent(SplashActivity.this, IVRActivity.class);
//                startActivity(intent);
                finish();
            }
        }
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {

            try {
                Thread.sleep(1000);

                Intent intent;
                if (hasConnection(SplashActivity.this))
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                else
                    intent = new Intent(SplashActivity.this, IVRActivity.class);
                startActivity(intent);
                finish();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    });

    public static boolean hasConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiNetwork = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }

        return false;
    }

}
