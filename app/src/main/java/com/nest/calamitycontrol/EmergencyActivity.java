package com.nest.calamitycontrol;

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

import java.util.ArrayList;

public class EmergencyActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RVAdapter mAdapter;
    ArrayList<EmergencyData> dataModelArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        setupRecyclerView();
        setData();
    }

    private void setData() {
        EmergencyData data;
        data = new EmergencyData();
        data.setName("Rohan");
        data.setNumber("8792414258");
        dataModelArrayList.add(data);
        data = new EmergencyData();
        data.setName("Darshan");
        data.setNumber("9742934099");
        dataModelArrayList.add(data);
        data = new EmergencyData();
        data.setName("Amruth");
        data.setNumber("9845336113");
        dataModelArrayList.add(data);
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

    class EmergencyData {
        String name;
        String number;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
