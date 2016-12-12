package com.nest.calamitycontrol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class EmergencyActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RVAdapter mAdapter;
    ArrayList<EmergencyData> dataModelArrayList = new ArrayList<>();
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        setupRecyclerView();
        getData();

        dialog = new ProgressDialog(EmergencyActivity.this);
        dialog.setIndeterminate(true);
        dialog.setTitle("Loading");
        dialog.setMessage("please wait...");
        dialog.show();
    }

    private void getData() {

        DatabaseReference databaseRef;

        databaseRef = FirebaseDatabase.getInstance().getReference("reports");


        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    EmergencyData datamodel = new EmergencyData();
                    datamodel.setName(data.child("name").getValue(String.class));
                    datamodel.setNumber(data.child("number").getValue(String.class));

                    dataModelArrayList.add(datamodel);

                }
                Collections.reverse(dataModelArrayList);
                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAdapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(EmergencyActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RVAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    class RVAdapter extends RecyclerView.Adapter<EmergencyActivity.RVAdapter.Holder> {
        @Override
        public EmergencyActivity.RVAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new EmergencyActivity.RVAdapter.Holder(LayoutInflater.from(EmergencyActivity.this).inflate(R.layout.single_emergency, parent, false));
        }

        @Override
        public void onBindViewHolder(EmergencyActivity.RVAdapter.Holder holder, final int position) {
            holder.name.setText(dataModelArrayList.get(position).getName());
            holder.number.setText(dataModelArrayList.get(position).getNumber());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                    phoneIntent.setData(Uri.parse("tel:" + dataModelArrayList.get(position).getNumber()));
                    if (ActivityCompat.checkSelfPermission(EmergencyActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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

            TextView name, number;

            public Holder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.name);
                number = (TextView) itemView.findViewById(R.id.number);
            }

        }

    }


}
