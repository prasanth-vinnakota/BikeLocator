package com.prasanth.ixat;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ToggleButton mNotify;
    private boolean mBookARide = false;
    private String TAG = "CustomerMapActivity : ", mDestination, mRideTypeString, customerId;
    private int riderTotalRating, riderTotalNumberOfRatings;
    private TextView mInfo, mOTP;
    private float DEFAULT_ZOOM = 10;
    private Marker mPickupLocationMarker, mDestinationMarker;
    private ImageView mRiderProfilePic;
    private TextView mRiderName;
    private TextView mRiderTel;
    private TextView mRiderCarType;
    private TextView mRiderRating;
    private LinearLayout mRiderInfo, mRideTypeInfo;
    private RadioGroup mRideType;
    private RadioButton mRegular, mPrime, mSUV;
    private double mDestinationLatitude, mDestinationLongitude;
    private LatLng mDestinationLatLng;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private View mapView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_map);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapView = mapFragment.getView();

        mRiderInfo = findViewById(R.id.riderInfo);
        mRideTypeInfo = findViewById(R.id.ride_type_layout);

        //initialize ProgressBar
        mProgressBar = findViewById(R.id.progress_customer);

        mRideType = findViewById(R.id.ride_type);

        mRegular = findViewById(R.id.ride_type_regular);
        mPrime = findViewById(R.id.ride_type_prime);
        mSUV = findViewById(R.id.ride_type_suv);

        mPrime.setChecked(true);
        mPrime.setBackgroundColor(Color.GRAY);

        mRiderProfilePic = findViewById(R.id.riderProfilePic);

        mRiderName = findViewById(R.id.riderName);
        mRiderTel = findViewById(R.id.riderTel);
        mRiderCarType = findViewById(R.id.riderCarType);
        mRiderRating = findViewById(R.id.driver_rating);

        mOTP = findViewById(R.id.otp);
        mInfo = findViewById(R.id.driverInfo);
        mInfo.setAllCaps(true);
        mInfo.setTypeface(null, Typeface.BOLD);
        mInfo.setGravity(Gravity.CENTER);

        mNotify = findViewById(R.id.notify);

        Button mSettings = findViewById(R.id.settings);
        Button mZoomIn = findViewById(R.id.zoomIn);
        Button mZoomOut = findViewById(R.id.zoomOut);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCJSXCGslgr7vJarCq4KSYIm6HoqhwFqJM");
        }

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);


        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment.setPlaceFields(fields);
        autocompleteSupportFragment.setHint("Destination Location");


        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getName();
                mDestinationLatLng = place.getLatLng();
                mDestinationLatitude = mDestinationLatLng.latitude;
                mDestinationLongitude = mDestinationLatLng.longitude;
                if (mDestinationMarker != null) {
                    mDestinationMarker.remove();
                }
                mDestinationMarker = mMap.addMarker(new MarkerOptions().title("Destination").position(mDestinationLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.destination_pin)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mDestinationLatLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            }

            @Override
            public void onError(@NonNull Status status) {
            }
        });

        mRideType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mRegular.isChecked()) {
                    mRegular.setBackgroundColor(Color.GRAY);
                    mPrime.setBackgroundColor(Color.WHITE);
                    mSUV.setBackgroundColor(Color.WHITE);
                }
                if (mPrime.isChecked()) {
                    mPrime.setBackgroundColor(Color.GRAY);
                    mSUV.setBackgroundColor(Color.WHITE);
                    mRegular.setBackgroundColor(Color.WHITE);
                }
                if (mSUV.isChecked()) {
                    mSUV.setBackgroundColor(Color.GRAY);
                    mPrime.setBackgroundColor(Color.WHITE);
                    mRegular.setBackgroundColor(Color.WHITE);
                }
            }
        });


        mZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEFAULT_ZOOM < 21) {
                    moveCamera = false;
                    DEFAULT_ZOOM += 1;
                }
                Log.d(TAG, "ZoomIn : " + DEFAULT_ZOOM);
            }
        });

        mZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DEFAULT_ZOOM > 3) {
                    moveCamera = false;
                    DEFAULT_ZOOM -= 1;
                }
                Log.d(TAG, "ZoomOut : " + DEFAULT_ZOOM);
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
                startActivity(intent);
            }
        });
        mNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    //show progress bar
                    mProgressBar.setVisibility(View.VISIBLE);

                    driverAssigned = true;
                    int rideType = mRideType.getCheckedRadioButtonId();
                    final RadioButton mSelectedRideType = findViewById(rideType);
                    mRideTypeString = mSelectedRideType.getText().toString();

                    mBookARide = true;
                    String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference db_ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(db_ref);
                    geoFire.setLocation(user_id, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                        }
                    });

                    mInfo.setText("Getting Your Ride...");
                    getClosestRider();

                } else {

                    //show progress bar
                    mProgressBar.setVisibility(View.VISIBLE);

                    mInfo.setText("");
                    endRide();
                }
            }
        });
    }


    private GeoQuery geoQuery;
    private int mRadius = 1;
    private boolean riderFound = false;
    private String riderID;

    private void getClosestRider() {
        DatabaseReference riderLocation = FirebaseDatabase.getInstance().getReference("riderAvailable");
        GeoFire geoFire = new GeoFire(riderLocation);
        mInfo.setText("Looking For Available Rider...");
        geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), mRadius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!riderFound && mBookARide) {
                    DatabaseReference mRiderDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(key);
                    mRiderDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Map<String, Object> riderMap = (Map<String, Object>) dataSnapshot.getValue();

                                if (riderFound)
                                    return;

                                if (riderMap == null)
                                    return;

                                if (riderMap.get("rideType").equals(mRideTypeString)) {
                                    Log.d(TAG, "Rider Found");
                                    riderFound = true;
                                    riderID = dataSnapshot.getKey();

                                    int otp = new Random().nextInt(9999 - 1000) + 1000;
                                    mOTP.setText("OTP :" + otp);
                                    DatabaseReference rider_db = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(riderID).child("customerInfo");
                                    customerId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("otp", otp);
                                    map.put("customerId", customerId);
                                    map.put("customerDestination", mDestination);
                                    map.put("customerDestinationLatitude", mDestinationLatitude);
                                    map.put("customerDestinationLongitude", mDestinationLongitude);
                                    map.put("rideType", mRideTypeString);
                                    rider_db.updateChildren(map);

                                    getRiderInformation();
                                    getRiderLocation();
                                    isRideEnded();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });


                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG, "Rider Not Found");
                if (!riderFound) {
                    mRadius++;
                    getClosestRider();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getRiderInformation() {
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(riderID);
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mRiderName.setText(map.get("name").toString());
                    }
                    if (map.get("tel") != null) {
                        mRiderTel.setText(map.get("tel").toString());
                    }
                    if (map.get("carType") != null) {
                        mRiderCarType.setText(map.get("carType").toString());
                    }
                    if (map.get("profileImageUrl") != null) {
                        String mProfilePicUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfilePicUrl).into(mRiderProfilePic);
                    }
                    if (map.get("totalRating") != null && map.get("numberOfRating") != null) {
                        riderTotalRating = Integer.parseInt(map.get("totalRating").toString());
                        riderTotalNumberOfRatings = Integer.parseInt(map.get("numberOfRating").toString());
                        DecimalFormat df = new DecimalFormat("#.#");
                        df.setRoundingMode(RoundingMode.CEILING);
                        mRiderRating.setText(df.format(riderTotalRating / riderTotalNumberOfRatings));
                    }
                    mRideTypeInfo.setVisibility(View.GONE);
                    mRiderInfo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference mRiderLocation;
    private ValueEventListener mRiderLocationListener;
    private Marker mRiderLocationMarker;

    private void getRiderLocation() {
        mRiderLocation = FirebaseDatabase.getInstance().getReference().child("riderWorking").child(riderID).child("l");
        mRiderLocationListener = mRiderLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //hide progress bar
                mProgressBar.setVisibility(View.GONE);

                if (dataSnapshot.exists() && mBookARide) {
                    mInfo.setText("Calculating Rider Distance...");
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double riderLatitude = 0;
                    double riderLongitude = 0;
                    if (map.get(0) != null && map.get(1) != null) {
                        riderLatitude = Double.parseDouble(map.get(0).toString());
                        riderLongitude = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng riderLatLng = new LatLng(riderLatitude, riderLongitude);
                    if (mRiderLocationMarker != null) {
                        mRiderLocationMarker.remove();
                    }
                    Location mRiderLocation = new Location("");
                    mRiderLocation.setLatitude(riderLatitude);
                    mRiderLocation.setLongitude(riderLongitude);

                    Location mPickupLocation = new Location("");
                    mPickupLocation.setLatitude(mLastLocation.getLatitude());
                    mPickupLocation.setLongitude(mLastLocation.getLongitude());

                    float distanceBetweenRiderAndCustomer = mPickupLocation.distanceTo(mRiderLocation);
                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.CEILING);
                    if (distanceBetweenRiderAndCustomer >= 50) {
                        mInfo.setText("Rider Distance: " + df.format(distanceBetweenRiderAndCustomer / 1000) + "Km");
                    } else {
                        mInfo.setText("Rider Has Arrived");
                        sendNotificationToCustomer("Your Ride Has Arrived", mRiderName.getText().toString() + " Is Arrived At PickUp Location And Waiting For You");
                    }
                    mRiderLocationMarker = mMap.addMarker(new MarkerOptions().title("riderLocation").position(riderLatLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_marker)));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference rideEndedDatabaseReference;
    private ValueEventListener rideEndedValueEventListener;

    private void isRideEnded() {
        rideEndedDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(riderID).child("customerInfo");
        rideEndedValueEventListener = rideEndedDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mNotify.setChecked(false);
                    endRide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotificationToCustomer(String title, String message) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(CustomerMapActivity.this);
        mBuilder.setSmallIcon(R.drawable.icon2);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setLights(Color.BLACK, 500, 500);
        long[] pattern = {50, 100, 150, 200, 250, 300, 350, 400, 500};
        mBuilder.setVibrate(pattern);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setOnlyAlertOnce(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int NOTIFICATION_ID = 12;
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    boolean driverAssigned = false;
    boolean moveCamera = false;
    LocationCallback mLocationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mLastLocation = location;

                    if (!driverAssigned)
                        getNearByRiders();

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (!moveCamera) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                        moveCamera = true;
                    }
                    if (mPickupLocationMarker != null) {
                        mPickupLocationMarker.remove();
                    }
                    mPickupLocationMarker = mMap.addMarker(new MarkerOptions().title("PickUp Location").position(latLng).icon(BitmapDescriptorFactory.fromResource(R.mipmap.source_pin)));

                }
            }
        }
    };


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());
        mMap.setMyLocationEnabled(true);

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 90, 350);
        }
    }


    private void endRide() {
        mBookARide = false;
        mInfo.setText("");
        autocompleteSupportFragment.setText("");
        autocompleteSupportFragment.setHint("Destination Location");
        geoQuery.removeAllListeners();
        if (mRiderLocationListener != null) {
            mRiderLocation.removeEventListener(mRiderLocationListener);
        }

        if (rideEndedValueEventListener != null) {
            rideEndedDatabaseReference.removeEventListener(rideEndedValueEventListener);
        }
        if (mDestinationMarker != null) {
            mDestinationMarker.remove();
        }


        if (riderID != null) {
            DatabaseReference mRiderID = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(riderID).child("customerInfo");
            mRiderID.removeValue();
            riderID = null;
        }
        if (mRiderLocationMarker != null) {
            mRiderLocationMarker.remove();
        }
        riderFound = false;
        mRadius = 1;
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference db_ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(db_ref);
        geoFire.removeLocation(user_id, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        mInfo.setText("");
        mRiderInfo.setVisibility(View.GONE);
        mRideTypeInfo.setVisibility(View.VISIBLE);

        //hide progress bar
        mProgressBar.setVisibility(View.GONE);

    }

    ArrayList<Marker> markers = new ArrayList<>();

    private void getNearByRiders() {
        DatabaseReference driversAvailable = FirebaseDatabase.getInstance().getReference().child("riderAvailable");

        GeoFire geoFire = new GeoFire(driversAvailable);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 1000);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for (Marker marker : markers) {
                    if (marker.getTag().equals(key)) {
                        return;
                    }
                }

                LatLng riderLocation = new LatLng(location.latitude, location.longitude);

                Marker riderMarker = mMap.addMarker(new MarkerOptions().title(key).position(riderLocation).icon(BitmapDescriptorFactory.fromResource(R.mipmap.regular_car)));
                riderMarker.setTag(key);

                markers.add(riderMarker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker : markers) {
                    if (marker.getTag().equals(key)) {
                        markers.remove(marker);
                        marker.remove();
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker marker : markers) {
                    if (marker.getTag().equals(key)) {
                        marker.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}
