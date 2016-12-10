package com.nest.calamitycontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NewVolunteerActivity extends AppCompatActivity {

    EditText name, place, number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_volunteer);

        init();
    }

    private void init() {
        name = (EditText) findViewById(R.id.name);
        place = (EditText) findViewById(R.id.place);
        number = (EditText) findViewById(R.id.number);
    }


    public void volunteer(View view) {
        if (name.getText().toString().equals("") || place.getText().toString().equals("") || number.getText().toString().equals("")) {
            Toast.makeText(NewVolunteerActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
        } else {
            sendData();
        }
    }

    private void sendData() {
        DatabaseReference databaseRef;
        databaseRef = FirebaseDatabase.getInstance().getReference("volunteers");

        String key = databaseRef.push().getKey();

        Map<String, Object> postValues = new HashMap<>();
        postValues.put("name", name.getText().toString());
        postValues.put("place", place.getText().toString());
        postValues.put("number", number.getText().toString());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + key, postValues);

        databaseRef.updateChildren(childUpdates);

        Toast.makeText(this, "Thank you for your registering as a volunteer", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NewVolunteerActivity.this, MainActivity.class));
        finish();
    }

}
