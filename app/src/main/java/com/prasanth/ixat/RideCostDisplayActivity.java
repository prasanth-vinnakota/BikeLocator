package com.prasanth.ixat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RideCostDisplayActivity extends AppCompatActivity {

    Button mAmountReceived;
    TextView mRideCost, mRideDistance;
    ImageView mRideType;
    String uniqueRideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ride_cost_display);

        uniqueRideId = getIntent().getStringExtra("uniqueRideId");
        mAmountReceived = findViewById(R.id.amount_received);
        mRideCost = findViewById(R.id.ride_cost);
        mRideDistance = findViewById(R.id.ride_distance);
        mRideType = findViewById(R.id.ride_type_icon);

        mAmountReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DatabaseReference recordRideDatabaseReference = FirebaseDatabase.getInstance().getReference().child("rideHistory").child(uniqueRideId);
        recordRideDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    HashMap<String, Object> hashMap =(HashMap<String, Object>) dataSnapshot.getValue();
                    if (hashMap != null && hashMap.get("rideType") != null && hashMap.get("rideDistance") != null) {
                        switch (hashMap.get("rideType").toString()) {
                            case "Regular":
                                mRideType.setImageResource(R.mipmap.regular_car);
                                mRideDistance.setText(hashMap.get("rideDistance").toString() + " Km");
                                if (Float.parseFloat(hashMap.get("rideDistance").toString()) > 5) {
                                    int rideCost = (int) ((Float.parseFloat(hashMap.get("rideDistance").toString())) * 6);
                                    mRideCost.setText("Rs. " + rideCost);
                                } else {
                                    mRideCost.setText("Rs. 40");
                                }
                                break;
                            case "Prime":
                                mRideType.setImageResource(R.mipmap.prime_car);
                                mRideDistance.setText(hashMap.get("rideDistance").toString() + " Km");
                                if (Float.parseFloat(hashMap.get("rideDistance").toString()) > 5) {
                                    int rideCost = (int) ((Float.parseFloat(hashMap.get("rideDistance").toString())) * 9);
                                    mRideCost.setText("Rs. " + rideCost);
                                } else {
                                    mRideCost.setText("Rs. 60");
                                }
                                break;
                            case "SUV":
                                mRideType.setImageResource(R.mipmap.suv);
                                mRideDistance.setText(hashMap.get("rideDistance").toString() + " Km");
                                if (Float.parseFloat(hashMap.get("rideDistance").toString()) > 5) {
                                    int rideCost = (int) ((Float.parseFloat(hashMap.get("rideDistance").toString())) * 12);
                                    mRideCost.setText("Rs. " + rideCost);
                                } else {
                                    mRideCost.setText("Rs. 80");
                                }
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
