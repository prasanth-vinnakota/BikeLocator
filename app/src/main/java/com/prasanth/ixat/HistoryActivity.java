package com.prasanth.ixat;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.r0adkll.slidr.Slidr;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

//This class is called by onClick() method of HistoryViewHolder
public class HistoryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String currentUserId;
    private String customerId;
    private String riderId;

    private TextView mName;
    private TextView mTel;
    private TextView mPickupLocation;
    private TextView mDestinationLocation;
    private TextView mRideStartedTimeStamp;
    private TextView mRideEndedTimeStamp;
    private TextView mCarType;
    private TextView mRideType;
    private TextView mDate;
    private TextView mDay;
    private TextView mRideTime;
    private TextView mRideCost;
    private TextView mRideDistance;

    private ImageView mUserImage;
    private ImageView mRideTypeImage;

    private DatabaseReference rideHistoryDatabaseReference;

    private LinearLayout mRideAndCarInformation;

    private LatLng destinationLatLng;
    private LatLng pickUpLatLng;

    private View mDivider;

    private RatingBar mRating;

    private Toolbar mToolbar;

    private ProgressBar mProgressBar;

    private double destination_latitude;
    private double destination_longitude;
    private double pick_up_latitude;
    private double pick_up_longitude;

    private long ride_time;
    private long ride_start_time;
    private long ride_end_time;
    private float ride_distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        //set ActivityExitTheme to this Activity
        Slidr.attach(this);

        //get data from HistoryViewHolder Activity
        String uniqueRideId = getIntent().getStringExtra("uniqueRideId");

        //initialize ans set title for tool bar
        mToolbar = findViewById(R.id.toolbar_history);

        //initialize progress bar
        mProgressBar = findViewById(R.id.progress_history);

        //initialize map fragment
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //call onMapReady() method using getMapAsync() method
        mMapFragment.getMapAsync(this);

        //initialize ImageViews
        mUserImage = findViewById(R.id.userImage);
        mRideTypeImage = findViewById(R.id.ride_type_icon);

        //initialize RatingBar
        mRating = findViewById(R.id.ride_rating);
        mRating.setFocusable(false);

        //initialize layout's
        mRideAndCarInformation = findViewById(R.id.ride_and_car_information);
        LinearLayout mRideHistoryLayout = findViewById(R.id.ride_history_layout);

        //enable nested scrolling for layout
        mRideHistoryLayout.setNestedScrollingEnabled(true);

        //initialize View
        mDivider = findViewById(R.id.divider);

        //initialize TextViews
        mName = findViewById(R.id.userName);
        mTel = findViewById(R.id.userTel);
        mRideType = findViewById(R.id.ride_type);
        mCarType = findViewById(R.id.car_type);
        mPickupLocation = findViewById(R.id.pickup_location);
        mDestinationLocation = findViewById(R.id.destination_location);
        mRideStartedTimeStamp = findViewById(R.id.rideStartedTimeStamp);
        mRideEndedTimeStamp = findViewById(R.id.rideEndedTimeStamp);
        mDate = findViewById(R.id.date);
        mDay = findViewById(R.id.day);
        mRideCost = findViewById(R.id.ride_cost);
        mRideDistance = findViewById(R.id.ride_distance);
        mRideTime = findViewById(R.id.ride_time);

        //get current user id
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //create database reference for rideHistory -> 'uniqueRideId'
        rideHistoryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("rideHistory").child(uniqueRideId);

        //call for method
        getRideInformation();

    }

    private void getRideInformation() {

        //add SingleValueEventListener to database reference
        rideHistoryDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //instance exists
                if (dataSnapshot.exists()) {

                    //loop to get all values of the database reference
                    for (DataSnapshot children : dataSnapshot.getChildren()) {

                        //check for null
                        if (children.getKey() != null && children.getValue() != null) {

                            //rideHistory -> 'uniqueRideId' -> customerId
                            if (children.getKey().equals("customerId")) {

                                //assign children value to String
                                customerId = children.getValue().toString();

                                //current user is Driver
                                if (!customerId.equals(currentUserId)) {

                                    //get customer information
                                    getCustomerInformation(customerId);
                                }
                            }


                            //rideHistory -> 'uniqueRideId' -> riderId
                            if (children.getKey().equals("riderId")) {

                                //assign children value to String
                                riderId = children.getValue().toString();

                                //current user is Customer
                                if (!riderId.equals(currentUserId)) {

                                    //set driver information
                                    getRiderInformation(riderId);
                                }
                            }

                            //rideHistory -> 'uniqueRideId' -> destinationLocation
                            if (children.getKey().equals("destinationLocation")) {

                                //set children value to TextView
                                mDestinationLocation.setText(children.getValue().toString());
                            }

                            //rideHistory -> 'uniqueRideId' -> pickUpLocation
                            if (children.getKey().equals("pickUpLocation")) {

                                //set children value to TextView
                                mPickupLocation.setText(children.getValue().toString());
                            }

                            //rideHistory -> 'uniqueRideId' -> rideStartedTimeStamp
                            if (children.getKey().equals("rideStartedTimeStamp")) {
                                //get time from the value of children i.e timestamp and set to TextView
                                mRideStartedTimeStamp.setText(getTimeFromTimeStamp(Long.parseLong(children.getValue().toString())));

                                //get day from the value of the children i.e timestamp and set to TextView
                                mDay.setText(getDayFromTimeStamp(Long.parseLong(children.getValue().toString())));

                                //get date from the value of the children i.e timestamp and set to TextView
                                mDate.setText(getDateFromTimeStamp(Long.parseLong(children.getValue().toString())));

                                //set title
                                mToolbar.setTitle(getDateFromTimeStamp(Long.parseLong(children.getValue().toString())));

                                //set children value to long identifier
                                ride_start_time = Long.parseLong(children.getValue().toString());

                                //calculate ride time
                                ride_time = ride_end_time - ride_start_time;

                                //set hours : minutes of the ride time to TextView
                                mRideTime.setText(ride_time / (60 * 1000) % 60 + ":" + ride_time / 1000 % 60);
                            }

                            //rideHistory -> 'uniqueRideId' -> rideEndedTimeStamp
                            if (children.getKey().equals("rideEndedTimeStamp")) {

                                //set children value to long identifier
                                ride_end_time = Long.parseLong(children.getValue().toString());

                                //get time from the value of children i.e timestamp and set to TextView
                                mRideEndedTimeStamp.setText(getTimeFromTimeStamp(Long.parseLong(children.getValue().toString())));
                            }

                            //rideHistory -> 'uniqueRideId' -> pickUpLatitude
                            if (children.getKey().equals("pickUpLatitude")) {

                                //assign value of children to double identifier
                                pick_up_latitude = Double.parseDouble(children.getValue().toString());
                            }

                            //rideHistory -> 'uniqueRideId' -> pickUpLongitude
                            if (children.getKey().equals("pickUpLongitude")) {

                                //assign value of children to double identifier
                                pick_up_longitude = Double.parseDouble(children.getValue().toString());

                                //initialize LatLng object
                                pickUpLatLng = new LatLng(pick_up_latitude, pick_up_longitude);

                                //call to method
                                autoZoomCamera();

                                //add a marker to pick up location
                                mMap.addMarker(new MarkerOptions().position(pickUpLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.source_pin)));
                            }

                            //rideHistory -> 'uniqueRideId' -> destinationLatitude
                            if (children.getKey().equals("destinationLatitude")) {

                                //assign value of children to double identifier
                                destination_latitude = Double.parseDouble(children.getValue().toString());
                            }

                            //rideHistory -> 'uniqueRideId' -> destinationLongitude
                            if (children.getKey().equals("destinationLongitude")) {

                                //assign value of children to double identifier
                                destination_longitude = Double.parseDouble(children.getValue().toString());

                                //initialize LatLng object
                                destinationLatLng = new LatLng(destination_latitude, destination_longitude);

                                //add marker to drop location
                                mMap.addMarker(new MarkerOptions().position(destinationLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.destination_pin)));
                            }

                            //rideHistory -> 'uniqueRideId' -> rideType
                            if (children.getKey().equals("rideType")) {

                                //set value of children to TextView
                                mRideType.setText(children.getValue().toString());

                                //ride type
                                switch (children.getValue().toString()) {

                                    //ride type is Regular
                                    case "Regular":

                                        //set ImageView
                                        mRideTypeImage.setImageResource(R.mipmap.regular_car);

                                        //break switch statement
                                        break;

                                    //ride type is Prime
                                    case "Prime":

                                        //set ImageView
                                        mRideTypeImage.setImageResource(R.mipmap.prime_car);

                                        //break switch statement
                                        break;

                                    //ride type is SUV
                                    case "SUV":

                                        //set ImageView
                                        mRideTypeImage.setImageResource(R.mipmap.suv);

                                        //break switch statement
                                        break;
                                }

                                calculateRideCost();
                            }

                            //rideHistory -> 'uniqueRideId' -> rideRating
                            if (children.getKey().equals("rideRating")) {

                                //set float value of children to RatingBar
                                mRating.setRating(Float.parseFloat(children.getValue().toString()));
                            }

                            //rideHistory -> 'uniqueRideId' -> rideDistance
                            if (children.getKey().equals("rideDistance")) {

                                //assign ride distance to float identifier
                                ride_distance = Float.parseFloat(children.getValue().toString());

                                //set value of children to TextView
                                mRideDistance.setText(children.getValue().toString() + " Km");
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //hide progress bar
        mProgressBar.setVisibility(View.GONE);
    }

    private void calculateRideCost() {

        //ride type
        switch (mRideType.getText().toString()) {

            //ride type is Regular
            case "Regular":

                //ride distance is greater than 5
                if (ride_distance > 5) {

                    //multiply ride distance with 6
                    int rideCost = (int) (ride_distance * 6);

                    //set ride cost to TextView
                    mRideCost.setText("Rs. " + rideCost);
                }
                //ride distance is less than 5
                else {

                    //set TextView
                    mRideCost.setText("Rs. 40");
                }

                //break switch statement
                break;

            //ride type is Prime
            case "Prime":

                //ride distance is greater than 5
                if (ride_distance > 5) {

                    //multiply ride distance with 9
                    int rideCost = (int) (ride_distance * 9);

                    //set ride cost to TextView
                    mRideCost.setText("Rs. " + rideCost);
                }
                //ride distance is less than 5
                else {

                    //set TextView
                    mRideCost.setText("Rs. 60");
                }

                //break switch statement
                break;

            //ride type is SUV
            case "SUV":

                //ride distance is greater than 5
                if (ride_distance > 5) {

                    //multiply ride distance with 12
                    int rideCost = (int) (ride_distance * 12);

                    //set ride cost to TextView
                    mRideCost.setText("Rs. " + rideCost);
                }
                //ride distance is less than 5
                else {

                    //set TextView
                    mRideCost.setText("Rs. 80");
                }

                //break switch statement
                break;
        }
    }

    private void autoZoomCamera() {

        //create a LatLngBound builder
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //include the LatLng objects to builder
        builder.include(pickUpLatLng);
        builder.include(destinationLatLng);

        //build builder
        LatLngBounds bounds = builder.build();

        //get user screen width in pixels
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        //multiply screen width with 0.15
        int padding = (int) (screenWidth * 0.15);

        //set bounds and padding to map
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

        //disable scroll, zoom and rotate to map
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    private void getCustomerInformation(String userId) {

        //create database reference for Users -> customerId -> 'userId'
        DatabaseReference userTypeDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(userId);

        //set SingleValueEventListener to the database reference
        userTypeDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //instance exists
                if (dataSnapshot.exists()) {

                    //assign database values to Map object
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //check for null
                    if (map == null)
                        return;

                    //Users -> customerId -> 'userId' -> name available
                    if (map.get("name") != null) {

                        //set customer name to TextView
                        mName.setText(map.get("name").toString());
                    }

                    //Users -> customerId -> 'userId' -> tel available
                    if (map.get("tel") != null) {

                        //set customer number to TextView
                        mTel.setText(map.get("tel").toString());
                    }

                    //Users -> customerId -> 'userId' -> profileImageUrl available
                    if (map.get("profileImageUrl") != null) {

                        //assign profile picture Url to String object
                        String mProfilePicUrl = map.get("profileImageUrl").toString();

                        //insert image to ImageView
                        Glide.with(getApplication()).load(mProfilePicUrl).into(mUserImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getRiderInformation(String userId) {

        //create database reference for Users -> riderId -> 'userId'
        DatabaseReference userTypeDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(userId);

        //set SingleValueEventListener to the database reference
        userTypeDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //instance exists
                if (dataSnapshot.exists()) {

                    //assign database values to Map object
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //check for null
                    if (map == null)
                        return;

                    //Users -> riderId -> 'userId' -> name available
                    if (map.get("name") != null) {

                        //set driver name to TextView
                        mName.setText(map.get("name").toString());
                    }

                    //Users -> riderId -> 'userId' -> tel available
                    if (map.get("tel") != null) {

                        //set driver number to TextView
                        mTel.setText(map.get("tel").toString());
                    }

                    //Users -> riderId -> 'userId' -> profileImageUrl available
                    if (map.get("profileImageUrl") != null) {

                        //assign profile picture Url to String object
                        String mProfilePicUrl = map.get("profileImageUrl").toString();

                        //insert image to ImageView
                        Glide.with(getApplication()).load(mProfilePicUrl).into(mUserImage);
                    }

                    //Users -> riderId -> 'userId' -> carType available
                    if (map.get("carType") != null) {

                        //set driver car type to TextView
                        mCarType.setText(map.get("carType").toString());
                    }

                    //set layout to visible
                    mRideAndCarInformation.setVisibility(View.VISIBLE);

                    //set view to visible
                    mDivider.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private String getDateFromTimeStamp(long timeStamp) {

        //create a Calender instance
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        //set Calender's object time
        calendar.setTimeInMillis(timeStamp * 1000);

        //return date
        return DateFormat.format("dd MMM yyyy hh:mm", calendar).toString();
    }

    private String getTimeFromTimeStamp(long timeStamp) {

        //create a Calender instance
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        //set Calender's object time
        calendar.setTimeInMillis(timeStamp * 1000);

        //return time
        return DateFormat.format("hh:mm", calendar).toString();
    }

    private String getDayFromTimeStamp(long timeStamp) {

        //create a Calender instance
        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        //set Calender's object time
        calendar.setTimeInMillis(timeStamp * 1000);

        //return String identifier
        return DateFormat.format("EEEE", calendar).toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
