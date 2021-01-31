package com.prasanth.ixat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prasanth.ixat.rideHistoryRecyclerView.History;
import com.prasanth.ixat.rideHistoryRecyclerView.HistoryAdapter;
import com.r0adkll.slidr.Slidr;

import android.text.format.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;


public class RideHistoryActivity extends AppCompatActivity {

    private String currentUserRoot;
    private String user_id;

    private RecyclerView.Adapter mHistoryAdapter;

    private String  time;
    private String day;
    private String date;
    private String pickupLocation;
    private String dropLocation;
    private String rideType;

    private ArrayList<History> rideHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        //set ActivityExitTheme to this Activity
        Slidr.attach(this);

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mHistoryLayoutManager = new LinearLayoutManager(RideHistoryActivity.this);
        ((LinearLayoutManager) mHistoryLayoutManager).setStackFromEnd(true);
        ((LinearLayoutManager) mHistoryLayoutManager).setReverseLayout(true);

        mRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getRideHistoryData());
        mRecyclerView.setAdapter(mHistoryAdapter);

        currentUserRoot = Objects.requireNonNull(getIntent().getExtras()).getString("currentUserRoot");
        user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        getUniqueRideIds();
    }

    private void getUniqueRideIds() {
        DatabaseReference currentUserRideHistoryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserRoot).child(user_id).child("rideHistory");
        currentUserRideHistoryDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                        for (DataSnapshot rideHistory : dataSnapshot.getChildren()){
                            getInformationFromUniqueRideId(rideHistory.getKey());
                        }
                }
                else{
                    History history = new History("Book a Ride To See Ride History","","","","","","");
                    rideHistory.add(history);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getInformationFromUniqueRideId(final String uniqueRideId) {
        DatabaseReference rideHistoryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("rideHistory").child(uniqueRideId);
        rideHistoryDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot children : dataSnapshot.getChildren()){
                        if (children.getKey().equals("rideStartedTimeStamp")){
                            long timeStamp = Long.parseLong(children.getValue().toString());
                            date = getDateFromTimeStamp(timeStamp);
                            time = getTimeFromTimeStamp(timeStamp);
                            day = getDayFromTimeStamp(timeStamp);
                        }
                        if (children.getKey().equals("destinationLocation")){
                            dropLocation = children.getValue().toString();
                        }
                        if (children.getKey().equals("pickUpLocation")){
                            pickupLocation = children.getValue().toString();
                        }
                        if (children.getKey().equals("rideType")){
                            rideType = children.getValue().toString();
                        }
                    }
                    History history = new History("RID"+uniqueRideId, time, day, date, rideType, pickupLocation, dropLocation) ;
                    rideHistory.add(history);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDateFromTimeStamp(long timeStamp) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timeStamp*1000);
        String date = DateFormat.format("dd MMM yyyy", calendar).toString();
        return date;
    }

    private String getTimeFromTimeStamp(long timeStamp) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timeStamp*1000);
        String date = DateFormat.format(" hh:mm", calendar).toString();
        return date;
    }

    private String getDayFromTimeStamp(long timeStamp) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timeStamp*1000);
        String day = DateFormat.format("EEEE",calendar).toString();
        return day;
    }

    private ArrayList<History> getRideHistoryData() {
        return rideHistory;
    }
}
