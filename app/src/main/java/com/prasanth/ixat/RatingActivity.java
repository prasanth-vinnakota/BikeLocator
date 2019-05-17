package com.prasanth.ixat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

//This Activity called in LoadingActivity
public class RatingActivity extends AppCompatActivity {

    private TextView mRiderName;
    private TextView mRiderCarName;
    private TextView mRideCost;
    private ImageView mRiderProfilePicture;
    private int total_rating = 0;
    private int number_of_ratings = 0;
    DatabaseReference riderDatabaseReference;
    DatabaseReference ratingDatabaseReference;
    DatabaseReference rideHistoryDatabaseReference;
    String userId;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        //initialize Toolbar
        Toolbar mToolbar = findViewById(R.id.toolbar_rating);
        mToolbar.setTitle("Rate your ride");

        //get data from LoadingActivity
        String riderId = getIntent().getStringExtra("riderId");
        String uniqueRideId = getIntent().getStringExtra("uniqueRideId");

        //initialize ProgressBar
        mProgressBar = findViewById(R.id.progress_rating);

        //get current user id
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //initializing TextViews
        mRiderName = findViewById(R.id.rider_name);
        mRiderCarName = findViewById(R.id.rider_car_name);
        mRideCost = findViewById(R.id.ride_cost);

        //initializing ImageView
        mRiderProfilePicture = findViewById(R.id.rider_profile_picture);

        //declaring and initializing RatingBar View
        RatingBar ratingBar = findViewById(R.id.ratingBar);

        //get database reference for Users -> customerID -> 'user_id' -> rating
        ratingDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(userId).child("rating");

        //set OnRatingBarChangeListener to RatingBar View
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //add current rating to totalRating and store it in totalRating
                total_rating += rating;

                //increase total number of rating by 1
                number_of_ratings++;

                //initialize HashMap object
                HashMap<String, Object> map = new HashMap<>();

                //add items to HashMap object
                map.put("totalRating", total_rating);
                map.put("numberOfRating", number_of_ratings);

                //update the children of Users -> riderId -> 'riderId'
                riderDatabaseReference.updateChildren(map);

                //initialize HashMap object
                HashMap<String, Object> hashMap = new HashMap<>();

                //add items to HashMap object
                hashMap.put("rideRating",rating);

                //update the children of rideHistory -> 'uniqueRideId'
                rideHistoryDatabaseReference.updateChildren(hashMap);

                //remove of Users -> customerId -> rating
                ratingDatabaseReference.removeValue();

                //start CustomerMapActivity
                startActivity(new Intent(RatingActivity.this, CustomerMapActivity.class));

                //hide progress bar
                mProgressBar.setVisibility(View.GONE);

                //finish current Activity
                finish();
            }
        });

        //create a database reference for rideHistory -> 'uniqueRideId'
        rideHistoryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("rideHistory").child(uniqueRideId);

        //add SingleValueEventListener to database reference
        rideHistoryDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //instance exists
                if (dataSnapshot.exists()){

                    //initialize HashMap object with the values of database reference instance
                    HashMap<String, Object> hashMap = (HashMap<String, Object>) dataSnapshot.getValue();

                    if (hashMap == null)
                        return;

                    //rideDistance and RideType are available
                    if (hashMap.get("rideType") != null && hashMap.get("rideDistance") != null){

                        //search for similar rideType value
                        switch (hashMap.get("rideType").toString()){

                            // rideType is Regular
                            case "Regular":

                                //distance traveled is greater than 5 Kms
                                if (Float.parseFloat(hashMap.get("rideDistance").toString()) > 5) {

                                    //multiply distance traveled with 6 and assign it to rideCost variable
                                    int rideCost = (int) ((Float.parseFloat(hashMap.get("rideDistance").toString())) * 6);

                                    //concat string
                                    String ride_cost = "Rs. " + rideCost;

                                    //set mRideCost TextView with String
                                    mRideCost.setText(ride_cost);
                                }
                                //distance traveled is less than 5 Kms
                                else{

                                    //set mRideCost TextView to Rs. 40
                                    String cost = "Rs. 40";
                                    mRideCost.setText(cost);
                                }

                                //break switch statement
                                break;

                            // rideType is Prime
                            case "Prime":

                                //distance traveled is greater than 5 Kms
                                if (Float.parseFloat(hashMap.get("rideDistance").toString()) > 5) {

                                    //multiply distance traveled with 9 and assign it to rideCost variable
                                    int rideCost = (int) ((Float.parseFloat(hashMap.get("rideDistance").toString())) * 9);

                                    //concat String
                                    String cost = "Rs. " + rideCost;

                                    //set mRideCost TextView with rideCost variable
                                    mRideCost.setText(cost);
                                }
                                //distance traveled is less than 5 Kms
                                else{

                                    //set mRideCost TextView to Rs. 60
                                    String cost = "Rs. 60";
                                    mRideCost.setText(cost);
                                }
                                //break switch statement
                                break;

                            // rideType is SUV
                            case "SUV":

                                //distance traveled is greater than 5 Kms
                                if (Float.parseFloat(hashMap.get("rideDistance").toString()) > 5) {

                                    //multiply distance traveled with 12 and assign it to rideCost variable
                                    int rideCost = (int) ((Float.parseFloat(hashMap.get("rideDistance").toString())) * 12);

                                    //concat String
                                    String cost = "Rs. " + rideCost;

                                    //set mRideCost TextView with rideCost variable
                                    mRideCost.setText(cost);
                                }
                                //distance traveled is less than 5 Kms
                                else{

                                    //set mRideCost TextView to Rs. 80
                                    String cost = "Rs. 80";
                                    mRideCost.setText(cost);
                                }
                                //break switch statement
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //create database reference to Users -> riderId -> 'riderId'
        riderDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(riderId);

        //add SingleValueEventListener to the database
        riderDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //instance exists
                if (dataSnapshot.exists()) {

                    //initialize map object with the values of database reference instance
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if (map == null)
                        return;

                    // Users -> riderId -> 'riderId' -> name is available
                    if (map.get("name") != null) {

                        //set mRiderName TextView to Users -> riderId -> 'riderId' -> name value
                        mRiderName.setText(map.get("name").toString());
                    }

                    // Users -> riderId -> 'riderId' -> carType is available
                    if (map.get("carType") != null) {

                        //set mRiderCarName TextView to Users -> riderId -> 'riderId' -> carType value
                        mRiderCarName.setText(map.get("carType").toString());
                    }

                    // Users -> riderId -> 'riderId' -> profileImageUrl is available
                    if (map.get("profileImageUrl") != null) {

                        //get profile image URL
                        String mProfilePicUrl = map.get("profileImageUrl").toString();

                        //set mRiderProfilePicture ImageView with profile image URL
                        Glide.with(getApplication()).load(mProfilePicUrl).into(mRiderProfilePicture);
                    }

                    // Users -> riderId -> 'riderId' -> totalRating is available
                    if (map.get("totalRating") != null){

                        //add Users -> riderId -> 'riderId' -> totalRating value to totalRating and store it in totalRating
                        total_rating += Float.parseFloat(map.get("totalRating").toString());
                    }

                    // Users -> riderId -> 'riderId' -> numberOfRating is available
                    if (map.get("numberOfRating") != null) {

                        //add Users -> riderId -> 'riderId' -> numberOfRating value to numberOfRatings and store it in numberOfRatings
                        number_of_ratings += Float.parseFloat(map.get("numberOfRating").toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
