package com.nest.calamitycontrol;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class VolunteerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RVAdapter mAdapter;
    ArrayList<VolunteerData> dataModelArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VolunteerActivity.this, NewVolunteerActivity.class));
                finish();
//                Snackbar.make(view, "Register new Volunteer", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        setupRecyclerView();
        getData();

    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RVAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    private void getData() {
        DatabaseReference databaseRef;

        databaseRef = FirebaseDatabase.getInstance().getReference("volunteers");


        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    VolunteerData datamodel = new VolunteerData();
                    datamodel.setName(data.child("name").getValue(String.class));
                    datamodel.setNumber(data.child("number").getValue(String.class));
                    datamodel.setPlace(data.child("place").getValue(String.class));
                    dataModelArrayList.add(datamodel);
                }
                Collections.reverse(dataModelArrayList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    class RVAdapter extends RecyclerView.Adapter<RVAdapter.Holder> {
        @Override
        public RVAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RVAdapter.Holder(LayoutInflater.from(VolunteerActivity.this).inflate(R.layout.single_volunteer, parent, false));
        }

        @Override
        public void onBindViewHolder(RVAdapter.Holder holder, final int position) {
            holder.name.setText(dataModelArrayList.get(position).getName());
            holder.place.setText(dataModelArrayList.get(position).getPlace());
            holder.number.setText(dataModelArrayList.get(position).getNumber());
            holder.number.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                    phoneIntent.setData(Uri.parse("tel:"+dataModelArrayList.get(position).getNumber()));
                    if (ActivityCompat.checkSelfPermission(VolunteerActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivity(phoneIntent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return dataModelArrayList.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            TextView name, place;
            Button number;

            public Holder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
                number = (Button) itemView.findViewById(R.id.number);
                place = (TextView) itemView.findViewById(R.id.location);
            }

        }

    }

}
