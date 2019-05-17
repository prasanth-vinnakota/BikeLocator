package com.prasanth.ixat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.ebanx.swipebtn.OnStateChangeListener;
import com.ebanx.swipebtn.SwipeButton;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class RiderMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;

    private Location mLastLocation;

    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private final String TAG = "RiderMapActivity : ";
    private String riderId;
    private String customerId = "";
    private String customerDestination;
    private String rideType;
    private String uniqueRideId;

    //private Button mOTP;

    private LinearLayout mCustomerInfo;

    private ImageView mCustomerProfilePic;

    private TextView mCustomerName;
    private TextView mCustomerTel;
    private TextView mCustomerDestination;

    private Marker mPickupLocationMarker;
    private Marker mDestinationMarker;

    private LatLng myLatLng;
    private LatLng pickupLatLng;
    private LatLng destinationLatLng;

    private ToggleButton mRideStatus;

    private Geocoder geocoder;

    private List<Polyline> polylineList;

    private View mapView;

    private DatabaseReference customer_requested_db;
    private DatabaseReference mCustomerLocation;

    private ValueEventListener mCustomerLocationListener;

    private double customerDestinationLatitude;
    private double customerDestinationLongitude;

    private long rideStartedTimeStamp;

    private boolean moveCamera = false;

    private float DEFAULT_ZOOM = 10;
    private float rideDistance;

    private Switch mAvailableSwitch;

    private SwipeButton mOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rider_map);

        //set orientation to Portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //initialize SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //get map fragment view
        mapView = mapFragment.getView();

        //method call
        mapFragment.getMapAsync(this);

        //get current rider id
        riderId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //request FusedLocationProviderClient from LocationServices class
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //initialize ToggleButton
        mRideStatus = findViewById(R.id.ride_status);

        //initialize LatLng object
        destinationLatLng = new LatLng(0.0, 0.0);

        //initialize ArrayList object
        polylineList = new ArrayList<>();

        //initialize Geocoder object
        geocoder = new Geocoder(RiderMapActivity.this, Locale.getDefault());

        //initialize LinearLayout
        mCustomerInfo = findViewById(R.id.customerInfo);

        //initialize ImageView
        mCustomerProfilePic = findViewById(R.id.customerProfilePic);

        //initialize TextView
        mCustomerName = findViewById(R.id.customerName);
        mCustomerTel = findViewById(R.id.customerTel);
        mCustomerDestination = findViewById(R.id.customerDestination);

        //initialize Button
        Button mZoomIn = findViewById(R.id.zoomIn);
        Button mZoomOut = findViewById(R.id.zoomOut);
        Button mSettings = findViewById(R.id.settings);
        mOTP = findViewById(R.id.enter_otp);

        //initialize Switch
        mAvailableSwitch = findViewById(R.id.rider_available);

        //check Switch through intent value
        mAvailableSwitch.setChecked(getIntent().getBooleanExtra("riderAvailable", false));

        //add OnClickListener to Button
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //create Intent object to RiderSettingsActivity
                Intent intent = new Intent(RiderMapActivity.this, RiderSettingsActivity.class);

                //switch is on
                if (mAvailableSwitch.isChecked()) {

                    //set intent value as true
                    intent.putExtra("status", true);
                }
                //switch is off
                else {

                    //set intent value as false
                    intent.putExtra("status", false);
                }

                //start RiderSettingsActivity
                startActivity(intent);
            }
        });

        //add OnClickListener to Button
        mZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //float value is less than 21
                if (DEFAULT_ZOOM < 21) {

                    //increase float value to 1
                    DEFAULT_ZOOM += 1;

                    //set boolean value for false
                    moveCamera = false;
                }
            }
        });

        //add OnClickListener to Button
        mZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //float value is greater than 3
                if (DEFAULT_ZOOM > 3) {

                    //decrease float value to 1
                    DEFAULT_ZOOM -= 1;

                    //set boolean value for false
                    moveCamera = false;
                }
            }
        });

        //add OnClickListener to ToggleButton
        mRideStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //ride stated
                if (isChecked) {

                    //method call
                    erasePolyLines();

                    //method call
                    rideStartedTimeStamp = getCurrentTimeStamp();

                    //destination LatLng is initialized
                    if (destinationLatLng.latitude != 0.0 && destinationLatLng.longitude != 0.0) {

                        //add marker to destination location
                        mDestinationMarker = mMap.addMarker(new MarkerOptions().title("Drop Location").position(destinationLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.destination_pin)));

                        //method call
                        drawRoute(destinationLatLng);

                        //show message
                        Toast.makeText(RiderMapActivity.this, "Ride Started", Toast.LENGTH_LONG).show();
                    }
                }
                //ride ended
                else {

                    //check for null
                    if (mDestinationMarker != null) {

                        //remove marker
                        mDestinationMarker.remove();
                    }

                    //method call
                    recordRide();

                    //method call
                    calculateAndDisplayRideCost();

                    //method call
                    endRide();
                }
            }
        });

        //add on state change listener to SwipeButton
        mOTP.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {

                if (!active)
                    return;

                //create a AlertDialogBuilder object
                AlertDialog.Builder alert = new AlertDialog.Builder(RiderMapActivity.this);

                //set builder title
                alert.setTitle("Enter OTP to Continue");

                //create a EditText object
                final EditText input = new EditText(RiderMapActivity.this);

                //set attributes of EditText
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setRawInputType(Configuration.KEYBOARD_12KEY);

                //add EditText to builder
                alert.setView(input);

                //set Button to builder
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        //create a SingleValueEventListener to Users -> riderId -> 'riderId' -> customerInfo
                        customer_requested_db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //database instance exists
                                if (dataSnapshot.exists()) {

                                    //get values from database
                                    HashMap<String, Object> hashMap = (HashMap<String, Object>) dataSnapshot.getValue();

                                    //check for null
                                    if (hashMap == null)
                                        return;

                                    //Users -> riderId -> 'riderId' -> customerInfo -> otp available
                                    if (hashMap.get("otp") != null) {

                                        //entered otp is equals to generated otp
                                        if (input.getText().toString().equals(hashMap.get("otp").toString())) {

                                            //hide Button
                                            mOTP.setVisibility(View.GONE);

                                            //show ToggleButton
                                            mRideStatus.setVisibility(View.VISIBLE);
                                        }
                                        //entered wrong otp
                                        else {

                                            //set message
                                            Toast.makeText(getApplicationContext(), "Wrong OTP", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                });

                //build and show builder
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }

        });


        //add OnClickListener to Button
     /*   mOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //create a AlertDialogBuilder object
                AlertDialog.Builder alert = new AlertDialog.Builder(RiderMapActivity.this);

                //set builder title
                alert.setTitle("Enter OTP to Continue");

                //create a EditText object
                final EditText input = new EditText(RiderMapActivity.this);

                //set attributes of EditText
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setRawInputType(Configuration.KEYBOARD_12KEY);

                //add EditText to builder
                alert.setView(input);

                //set Button to builder
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        //create a SingleValueEventListener to Users -> riderId -> 'riderId' -> customerInfo
                        customer_requested_db.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //database instance exists
                                if (dataSnapshot.exists()) {

                                    //get values from database
                                    HashMap<String, Object> hashMap = (HashMap<String, Object>) dataSnapshot.getValue();

                                    //check for null
                                    if (hashMap == null)
                                        return;

                                    //Users -> riderId -> 'riderId' -> customerInfo -> otp available
                                    if (hashMap.get("otp") != null)
                                    {

                                        //entered otp is equals to generated otp
                                        if (input.getText().toString().equals(hashMap.get("otp").toString())) {

                                            //hide Button
                                            mOTP.setVisibility(View.GONE);

                                            //show ToggleButton
                                            mRideStatus.setVisibility(View.VISIBLE);
                                        }
                                        //entered wrong otp
                                        else {

                                            //set text to Button
                                            mOTP.setText("Incorrect OTP.. Enter Again");
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                });

                //build and show builder
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        });*/


        //add OnClickListener to Switch
        mAvailableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //switch on
                if (isChecked) {

                    //method call
                    signIn();
                }
                //switch off
                else {

                    //method call
                    signOut();
                }
            }
        });

        //method call
        getRequestedCustomer();

    }

    private void getRequestedCustomer() {

        //create database reference for Users -> riderId -> riderId -> customerInfo -> customerId
        DatabaseReference customer_requested_db = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(riderId).child("customerInfo").child("customerId");

        //add ValueEventListener to database reference
        customer_requested_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //database reference exists
                if (dataSnapshot.exists()) {

                    //get customer id
                    customerId = (String) dataSnapshot.getValue();

                    ///method calls
                    getRequestedCustomerPickUpLocation();
                    getRequestedCustomerDestination();
                    getRequestedCustomerInformation();
                }
                //database reference os not exists
                else {

                    //method call
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //log message
                Log.d(TAG, "Customer Id request cancelled");
            }
        });
    }

    private void getRequestedCustomerDestination() {

        //get database reference for Users -> riderId -> 'riderId' -> customerInfo
        customer_requested_db = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(riderId).child("customerInfo");

        //add SingleValueEventListener to database reference
        customer_requested_db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //database reference exist and children are available
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    //get database values
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //check for null
                    if (map == null)
                        return;

                    //Users -> riderId -> 'riderId' -> customerInfo -> customerDestination exists
                    if (map.get("customerDestination") != null) {

                        //set TextView
                        customerDestination = map.get("customerDestination").toString();
                        mCustomerDestination.setText(customerDestination);
                    } else {

                        //set TextView
                        mCustomerDestination.setText("");
                    }

                    //Users -> riderId -> 'riderId' -> customerInfo -> customerDestinationLatitude and
                    //Users -> riderId -> 'riderId' -> customerInfo -> customerDestinationLongitude exists
                    if (map.get("customerDestinationLatitude") != null && map.get("customerDestinationLongitude") != null) {

                        //get latitude and longitude
                        customerDestinationLatitude = Double.parseDouble(map.get("customerDestinationLatitude").toString());
                        customerDestinationLongitude = Double.parseDouble(map.get("customerDestinationLongitude").toString());

                        //assign latitude and longitude ti LatLng object
                        destinationLatLng = new LatLng(customerDestinationLatitude, customerDestinationLongitude);
                    }
                    //Users -> riderId -> 'riderId' -> customerInfo -> rideType exists
                    if (map.get("rideType") != null) {

                        //get riderType
                        rideType = map.get("rideType").toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //Log message
                Log.d(TAG, "Customer Destination Request Cancelled");
            }
        });
    }

    private void getRequestedCustomerInformation() {

        //get database reference for Users -> customerId -> 'customerId'
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(customerId);

        //add SingleValueEventListener to database reference
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //database instance exists and children are greater than zero
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    //get value of database
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //check for null
                    if (map == null)
                        return;

                    //Users -> customerId -> 'customerId' -> name exists
                    if (map.get("name") != null) {

                        //set TextView
                        mCustomerName.setText(map.get("name").toString());
                    }

                    //Users -> customerId -> 'customerId' -> tel exists
                    if (map.get("tel") != null) {

                        //set TextView
                        mCustomerTel.setText(map.get("tel").toString());
                    }

                    //Users -> customerId -> 'customerId' -> profileImageUrl exists
                    if (map.get("profileImageUrl") != null) {

                        //get profile picture Url
                        String mProfilePicUrl = map.get("profileImageUrl").toString();

                        //insert image to imageView using Glide
                        Glide.with(getApplication()).load(mProfilePicUrl).into(mCustomerProfilePic);
                    }

                    //show LinearLayout
                    mCustomerInfo.setVisibility(View.VISIBLE);

                    //show Button
                    mOTP.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //log message
                Log.d(TAG, "Customer Information Request Cancelled");
            }
        });
    }

    private void getRequestedCustomerPickUpLocation() {

        //get database reference for customerRequest -> 'customerId' -> l
        mCustomerLocation = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");

        //add ValueEventListener to database reference
        mCustomerLocationListener = mCustomerLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //database reference exists and customerId is not null
                if (dataSnapshot.exists() && !customerId.equals("")) {

                    //get Values from database
                    List<Object> list = (List<Object>) dataSnapshot.getValue();

                    double customerLatitude;
                    double customerLongitude;

                    //check for null
                    if (list == null)
                        return;

                    //check for null
                    if (list.get(0) != null && list.get(1) != null) {

                        //get latitude nd longitude from database
                        customerLatitude = Double.parseDouble(list.get(0).toString());
                        customerLongitude = Double.parseDouble(list.get(1).toString());

                        //assign LatLng object with latitude and longitude
                        pickupLatLng = new LatLng(customerLatitude, customerLongitude);

                        //add marker to LatLng object
                        mPickupLocationMarker = mMap.addMarker(new MarkerOptions().title("customerLocation").position(pickupLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.source_pin)));

                        //method call
                        drawRoute(pickupLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                //log message
                Log.d(TAG, "Customer Pickup Location Request Cancelled");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //add GoogleMap to class GoogleMap object
        mMap = googleMap;

        //create a LocationRequest object
        mLocationRequest = new LocationRequest();

        //set variables for LocationRequest object
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //location permission is not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //request location updates
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());

        //enable location to GoogleMap object
        mMap.setMyLocationEnabled(true);

        //check for null
        if (mapView != null && mapView.findViewById(1) != null) {

            //get the location button view
            View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);

            //get layout params of location button
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();

            //add rules to location button layout
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

            //set margins to location button layout
            layoutParams.setMargins(0, 0, 90, 350);
        }
    }


    private void drawRoute(LatLng end) {

        //create a object for Routing.Builder add parameters for methods
        Routing mRouting = new Routing.Builder()
                .key("AIzaSyCJSXCGslgr7vJarCq4KSYIm6HoqhwFqJM")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .alternativeRoutes(false)
                .waypoints(myLatLng, end)
                .withListener(this)
                .build();

        //execute Routing object
        mRouting.execute();
    }

    private void erasePolyLines() {

        //for every Polyline
        for (Polyline polyline : polylineList)

            //remove polyline
            polyline.remove();

        //clear ArrayList
        polylineList.clear();
    }

    //this will record ride to database and store in rideHistory
    private void recordRide() {

        //get current user id
        String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //get database reference to rideHistory
        DatabaseReference recordRideDatabaseReference = FirebaseDatabase.getInstance().getReference().child("rideHistory");

        //get database reference to Users -> riderId -> 'user_id' -> rideHistory
        DatabaseReference recordRideIntoRiderDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(user_id).child("rideHistory");

        //get database reference to Users -> customerId -> 'customerId -> rating
        final DatabaseReference ratingDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(customerId).child("rating");

        //customerId is equals to null
        if (customerId.equals(""))
            return;

        //get database reference to Users -> customerId -> 'customerId' -> rideHistory
        DatabaseReference recordRideIntoCustomerDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(customerId).child("rideHistory");

        //get unique id for ride
        uniqueRideId = recordRideDatabaseReference.push().getKey();

        //check for null
        if (uniqueRideId == null)
            return;

        //write ride id to customer database
        recordRideIntoCustomerDatabaseReference.child(uniqueRideId).setValue(true);

        //write ride id to rider database
        recordRideIntoRiderDatabaseReference.child(uniqueRideId).setValue(true);

        //create a HashMap object
        HashMap<String, Object> hashMap = new HashMap<>();

        //insert key-value pairs to HashMap object
        hashMap.put("uniqueRideId", uniqueRideId);
        hashMap.put("riderId", user_id);

        //update children of Users -> customerId -> 'customerId -> rating
        ratingDatabaseReference.updateChildren(hashMap);

        List<Address> pickUpAddresses = null;
        List<Address> destinationAddresses = null;

        //assign LatLng object with destination latitude and longitude
        destinationLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        try {

            //get list of addresses from latitude and longitude
            pickUpAddresses = geocoder.getFromLocation(pickupLatLng.latitude, pickupLatLng.longitude, 1);
            destinationAddresses = geocoder.getFromLocation(destinationLatLng.latitude, destinationLatLng.longitude, 1);

        } catch (IOException e) {

            //show exception
            Toast.makeText(RiderMapActivity.this, e.getMessage(), Toast.LENGTH_LONG);

            //log exception
            Log.d(TAG, "Exception while getting addresses" + e.getMessage());
        }

        //create a DecimalFormat object with format
        DecimalFormat df = new DecimalFormat("#.##");

        //set rounding mode to 2
        df.setRoundingMode(RoundingMode.CEILING);

        //create a HashMap object
        HashMap<String, Object> map = new HashMap<>();

        //addresses are not equal to null
        if (pickUpAddresses != null && destinationAddresses != null)

            //size is greater than zero
            if (pickUpAddresses.size() > 0 && destinationAddresses.size() > 0) {

                //put key-value pairs to HashMap object
                map.put("destinationLocation", destinationAddresses.get(0).getAddressLine(0));
                map.put("pickUpLocation", pickUpAddresses.get(0).getAddressLine(0));
            }
        map.put("rideDistance", df.format(rideDistance / 1000));
        map.put("rideStartedTimeStamp", rideStartedTimeStamp);
        map.put("rideEndedTimeStamp", getCurrentTimeStamp());
        map.put("riderId", user_id);
        map.put("customerId", customerId);
        map.put("rideRating", 0);
        map.put("pickUpLatitude", pickupLatLng.latitude);
        map.put("pickUpLongitude", pickupLatLng.longitude);
        map.put("destinationLatitude", destinationLatLng.latitude);
        map.put("destinationLongitude", destinationLatLng.longitude);
        map.put("rideType", rideType);

        //update children of rideHistory -> 'uniqueRideId'
        recordRideDatabaseReference.child(uniqueRideId).updateChildren(map);
    }

    private void calculateAndDisplayRideCost() {

        //create a Intent object to RideCostDisplayActivity
        final Intent intent = new Intent(RiderMapActivity.this, RideCostDisplayActivity.class);

        //send uniqueRideId to intent object
        intent.putExtra("uniqueRideId", uniqueRideId);

        //create a Anonymous Handler object and call postDelayed method
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //start RideCostDisplayActivity with delay 1.5seconds
                startActivity(intent);
            }
        }, 1500);
    }

    private long getCurrentTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    //this method will clear the requests of database
    private void endRide() {

        //method call
        erasePolyLines();

        //get current user
        String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //get database reference for Users -> riderId -> 'user_id' -> customerInfo
        DatabaseReference endRideDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(user_id).child("customerInfo");

        //remove database reference value
        endRideDatabaseReference.removeValue();

        //get database reference for customerRequest
        DatabaseReference customerRequestDatabaseReference = FirebaseDatabase.getInstance().getReference().child("customerRequest");

        //remove location in  customerRequest -> 'customerId'
        GeoFire geoFire = new GeoFire(customerRequestDatabaseReference);
        geoFire.removeLocation(customerId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

                //log message
                Log.d(TAG, "location in customerRequest is removed");
            }
        });

        //set customer id to null
        customerId = "";

        //check for null
        if (mPickupLocationMarker != null) {

            //remove marker at pickupLocation
            mPickupLocationMarker.remove();
        }

        //check for null
        if (mCustomerLocationListener != null) {

            //remove EventListener to customer location
            mCustomerLocation.removeEventListener(mCustomerLocationListener);
        }

        //set visibility gone for LinearLayout
        mCustomerInfo.setVisibility(View.GONE);

        //set visibility gone for ToggleButton
        mRideStatus.setVisibility(View.GONE);
    }

    private void signIn() {

        //request location updates from FusedLocationProviderClient
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());

        //enable location in maps
        mMap.setMyLocationEnabled(true);
    }

    private void signOut() {

        //check for null
        if (mFusedLocationProviderClient != null) {

            //remove location updates of FusedLocationProviderClient
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallBack);
        }

        //get current user id
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //get database reference for riderAvailable
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riderAvailable");

        //create a GeoFire object for database reference
        GeoFire geoFire = new GeoFire(ref);

        //remove Location in database riderAvailable
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

                //log message
                Log.d(TAG, "remove location at RiderAvailable");
            }
        });
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        //exception is raised
        if (e != null) {

            //shoe exception
            Toast.makeText(RiderMapActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

            //log exception
            Log.d(TAG, e.getMessage());
        }
        //exception is not raised
        else {

            //show message
            Toast.makeText(RiderMapActivity.this, "Error While Routing", Toast.LENGTH_SHORT).show();

            //log message
            Log.d(TAG, "Routing failure exception fot found");
        }
    }

    @Override
    public void onRoutingStart() {

        //show message
        Toast.makeText(getApplicationContext(), "Searching Route to Customer", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortRouteIndex) {

        //polylineList exists
        if (polylineList.size() > 0) {

            //for every polyline
            for (Polyline polyline : polylineList) {

                //remove polyline
                polyline.remove();
            }
        }

        //assign ArrayList object
        polylineList = new ArrayList<>();

        //create PolylineOptions object
        PolylineOptions polylineOptions = new PolylineOptions();

        //set color
        polylineOptions.color(getResources().getColor(R.color.colorPrimaryDark));

        //set width
        polylineOptions.width(10);

        //add LatLng list
        polylineOptions.addAll(routes.get(0).getPoints());

        //add Polyline to map
        Polyline polyline = mMap.addPolyline(polylineOptions);

        //add Polyline to ArrayList
        polylineList.add(polyline);

        //create Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(RiderMapActivity.this);

        //set builder title
        builder.setTitle("Customer Location");

        //set builder icon
        builder.setIcon(R.mipmap.customer);

        //set builder message
        builder.setMessage("Distance : " + routes.get(0).getDistanceValue() + "\nDuration : " + routes.get(0).getDurationValue());

        //set Button
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //dismiss dialog
                dialog.dismiss();
            }
        });

        //create and show builder
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRoutingCancelled() {

        //show message
        Toast.makeText(getApplicationContext(), "Routing Cancelled", Toast.LENGTH_SHORT).show();
    }

    //create LocationCallback object
    LocationCallback mLocationCallBack = new LocationCallback() {

        //this method will be called when location is changed
        @Override
        public void onLocationResult(LocationResult locationResult) {

            //for every location
            for (Location location : locationResult.getLocations()) {

                //application has a context
                if (getApplicationContext() != null) {

                    //assign latitude and longitude to LatLng object
                    myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    //check boolean object
                    if (!moveCamera) {

                        //move camera to your location
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));

                        //animate camera to given zoom
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

                        //set boolean value to true
                        moveCamera = true;
                    }

                    //database reference for riderAvailable
                    DatabaseReference rider_available_db = FirebaseDatabase.getInstance().getReference("riderAvailable");

                    //database reference for riderWorking
                    DatabaseReference rider_working_db = FirebaseDatabase.getInstance().getReference("riderWorking");

                    //create a GeoFire reference for riderAvailable
                    GeoFire geoFireAvailable = new GeoFire(rider_available_db);

                    //create a GeoFire reference for riderWorking
                    GeoFire geoFireWorking = new GeoFire(rider_working_db);

                    //no customer is available
                    if ("".equals(customerId)) {

                        //set location at riderAvailable -> latitude and longitude
                        geoFireAvailable.setLocation(riderId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                                //write log
                                Log.d(TAG, "Set RiderAvailable : " + key);
                            }
                        });

                        //remove location at riderWorking -> latitude and longitude
                        geoFireWorking.removeLocation(riderId, new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                                //write log
                                Log.d(TAG, "remove RiderWorking : " + key);
                            }
                        });
                    }
                    //customer is assigned
                    else {

                        //set location at riderWorking -> latitude and longitude
                        geoFireWorking.setLocation(riderId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                                //write log
                                Log.d(TAG, "Set RiderWorking : " + key);
                            }
                        });

                        //remove location at riderAvailable -> latitude and longitude
                        geoFireAvailable.removeLocation(riderId, new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                                //write log
                                Log.d(TAG, "remove RiderAvailable : " + key);
                            }
                        });

                        //increase ride distance
                        rideDistance += mLastLocation.distanceTo(location);
                    }

                    //update last location
                    mLastLocation = location;
                }
            }
        }
    };
}
