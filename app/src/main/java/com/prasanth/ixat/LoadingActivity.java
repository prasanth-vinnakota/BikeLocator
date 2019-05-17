package com.prasanth.ixat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

//LoadingActivity animates and it is the Activity Launched when Application started
public class LoadingActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private FirebaseAuth mAuth;
    private final int LOCATION_PERMISSION_CODE = 92;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);

        //starting OnApplicationDestroyed class as a service when application destroyed
        startService(new Intent(LoadingActivity.this, OnApplicationDestroyed.class));

        //setting ImageView's
        ImageView animate = findViewById(R.id.animate);

        //getting Firebase instance
        mAuth = FirebaseAuth.getInstance();

        //creating FirebaseAuth Listener
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //get current user instance
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //user exist
                if (user != null) {

                    //get current user id from FirebaseAuth
                    final String user_id = user.getUid();

                    //create a database reference for Users -> customerId
                    DatabaseReference customerIdDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId");

                    //add SingleValueEventListener to database reference
                    customerIdDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //instance exists
                            if (dataSnapshot.exists()) {

                                //get children of Users -> customerId
                                for (DataSnapshot children : dataSnapshot.getChildren()) {

                                    //children key equals is user id
                                    if (Objects.requireNonNull(children.getKey()).equals(user_id)) {

                                        //create a database reference for Users -> customerId -> 'user_id' -> rating
                                        DatabaseReference ratingDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(user_id).child("rating");

                                        //add SingleValueEventListener to the database
                                        ratingDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                //instance not exists
                                                if (!dataSnapshot.exists()) {
                                                    //create a Intent object to CustomerMapActivity
                                                    Intent intent = new Intent(LoadingActivity.this, CustomerMapActivity.class);

                                                    //start CustomerMapActivity
                                                    startActivity(intent);

                                                    //finish current Activity
                                                    finish();
                                                }
                                                //instance exists
                                                else {

                                                    //create a HashMap object get the values of snapshot
                                                    HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();

                                                    //check for null
                                                    if (map == null)
                                                        return;

                                                    //Users -> customerId -> 'user_id' -> rating -> riderId and Users -> customerId -> 'user_id' -> rating -> uniqueRideId exists
                                                    if (map.get("riderId") != null && map.get("uniqueRideId") != null) {

                                                        //create Intent object to RatingActivity
                                                        Intent intent = new Intent(LoadingActivity.this, RatingActivity.class);

                                                        //pass Users -> customerId -> 'user_id' -> rating -> riderId and Users -> customerId -> 'user_id' -> rating -> uniqueRideId values to RatingActivity
                                                        intent.putExtra("riderId", map.get("riderId").toString());
                                                        intent.putExtra("uniqueRideId", map.get("uniqueRideId").toString());

                                                        //start RatingActivity
                                                        startActivity(intent);

                                                        //finish current Activity
                                                        finish();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    //create database reference to Users -> riderId
                    DatabaseReference riderIdDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId");

                    //add SingleValueEventListener to database reference
                    riderIdDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            //instance exists
                            if (dataSnapshot.exists()) {

                                //get children of Users -> riderId
                                for (DataSnapshot children : dataSnapshot.getChildren()) {

                                    //children key equals to user id
                                    if (Objects.requireNonNull(children.getKey()).equals(user_id)) {

                                        //create a Intent object to RiderMapActivity
                                        Intent intent = new Intent(LoadingActivity.this, RiderMapActivity.class);

                                        //set available switch of rider
                                        intent.putExtra("riderAvailable", true);

                                        //start RiderMapActivity
                                        startActivity(intent);

                                        //finish current Activity
                                        finish();

                                        //exit
                                        return;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                //user not exist
                else {

                    //create Intent object to MainActivity
                    Intent intent = new Intent(LoadingActivity.this, MainActivity.class);

                    //start MainActivity
                    startActivity(intent);

                    //finish current Activity
                    finish();
                }
            }
        };

        //initialize a ObjectAnimator object with the ImageView, Translation Direction and  Translation Values
        ObjectAnimator animation = ObjectAnimator.ofFloat(animate, "translationX", 2500);

        //set animation duration for 10 seconds
        animation.setDuration(10000);

        //set RepeatMove to Reverse
        animation.setRepeatMode(ValueAnimator.REVERSE);

        //set RepeatCount to infinity
        animation.setRepeatCount(-1);

        //start animation
        animation.start();

        //check Internet Connection
        checkInternetConnection();

        //check Location Permissions
        checkLocationPermission();

    }

    private void checkLocationPermission() {

        //assigning required permissions to permissions array
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        //location permission not granted
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //request for location permission with request code
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    //This method called after ActivityCompat.requestPermissions() method;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //check for request code
        //request code equals to LOCATION_PERMISSION_CODE
        if (requestCode == LOCATION_PERMISSION_CODE) {

            //requests granted length is greater than zero
            if (grantResults.length > 0) {

                //for every granted result
                for (int grantResult : grantResults) {

                    //permission not granted
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {

                        //show message
                        Toast.makeText(this, "Location Permission NOt Granted", Toast.LENGTH_SHORT).show();
                    }
                }

                //show message
                Toast.makeText(this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
            }
            //break switch statement
        }
    }


    public void checkInternetConnection() {

        //initialize connectivityManager to get the statuses of connectivity services
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


        NetworkInfo mobile_data = null;
        NetworkInfo wifi = null;

        //connectivityManager have statuses of connection services
        if (connectivityManager != null) {

            //get the status of mobile data
            mobile_data = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            //get status of wifi
            wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        }

        //mobile data or wifi is connected
        if ((mobile_data != null && mobile_data.isConnected()) || (wifi != null && wifi.isConnected())) {

            //exit
            return;
        }

        // start OfflineActivity
        startActivity(new Intent(LoadingActivity.this, OfflineActivity.class));

        //finish current Activity
        finish();
    }


    @Override
    protected void onStart() {
        super.onStart();

        //add firebaseAuthStateListener
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //remove firebaseAuthStateListener
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
