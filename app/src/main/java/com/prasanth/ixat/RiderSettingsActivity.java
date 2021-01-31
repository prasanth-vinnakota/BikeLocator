package com.prasanth.ixat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.r0adkll.slidr.Slidr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RiderSettingsActivity extends AppCompatActivity {

    private static final int PROFILE_PIC_REQUEST_CODE_FROM_GALLERY = 92;
    private Button mSubmit;
    private EditText mName;
    private EditText mTel;
    private EditText mCarType;
    private DatabaseReference mDatabaseReference;
    private String riderId;
    private String name;
    private String tel;
    private String profilePicUrl;
    private String carType;
    private String rideType;
    private ImageView mProfilePic;
    private Uri uploadUri;
    private RadioGroup mRideType;
    private InputMethodManager mInputMethodManager;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rider_settings);

        //set ActivityExitTheme to this Activity
        Slidr.attach(this);

        //apply screen orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //initialize Toolbar
        Toolbar mToolbar = findViewById(R.id.toolbar_rider_setting);
        mToolbar.setTitle("Settings");

        //initialize ProgressBar
        mProgressBar = findViewById(R.id.progress_rider_setting);

        //initialize Button
        mSubmit = findViewById(R.id.submit_rider);
        Button mLogout = findViewById(R.id.logout_rider);
        Button mRideHistory = findViewById(R.id.ride_history_rider);
        Button mChangePassword = findViewById(R.id.change_password_rider);


        //initialize RadioGroup
        mRideType = findViewById(R.id.ride_type);

        //initialize ImageView
        mProfilePic = findViewById(R.id.profile_pic);

        //initialize EditText
        mName = findViewById(R.id.name);
        mTel = findViewById(R.id.tel);
        mCarType = findViewById(R.id.car_type);

        //create a object for FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //get current user id
        riderId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        //create database reference for Users -> riderId -> 'riderId'
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(riderId);

        //method call
        getInformationFromDatabase();

        //set OnCheckChangeListener to RadioGroup
        mRideType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                //enable submit Button
                mSubmit.setEnabled(true);
            }
        });

        //set OnClickListener to EditText
        mTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //enable submit Button
                mSubmit.setEnabled(true);

                //open keyboard on focus
                mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                //check for null
                if (mInputMethodManager != null) {

                    //hide keyboard
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

            }
        });

        //set OnClickListener to EditText
        mName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //enable submit Button
                mSubmit.setEnabled(true);

                //open keyboard on focus
                mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                //check for null
                if (mInputMethodManager != null) {

                    //hide keyboard
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        //set OnClickListener to EditText
        mCarType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //enable submit Button
                mSubmit.setEnabled(true);

                //open keyboard on focus
                mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

                //check for null
                if (mInputMethodManager != null) {

                    //hide keyboard
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        //set OnClickListener to Button
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //assign String objects with the text in EditText
                name = mName.getText().toString();
                tel = mTel.getText().toString();
                carType = mCarType.getText().toString();

                //get the checked radio button
                final RadioButton mSelectedRideType = findViewById(mRideType.getCheckedRadioButtonId());

                    //rideType is available
                    if ( mSelectedRideType.getText() != null) {

                        //get the value of selected RadioButton
                        rideType = mSelectedRideType.getText().toString();

                        //method call
                        saveInformationToDatabase();
                    }
            }
        });

        //set OnClickListener to ImageView
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //open gallery through intent
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                //start Activity with a user defined code
                startActivityForResult(intent,PROFILE_PIC_REQUEST_CODE_FROM_GALLERY);

                //enable submit button
                mSubmit.setEnabled(true);
            }
        });

        //set OnClickListener to Button
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //available switch is on
               if (getIntent().getBooleanExtra("status",true)){

                   //show message
                   Toast.makeText(getApplicationContext(), "Turn off available switch and try again", Toast.LENGTH_LONG).show();

                   return;
               }

                //delete instance of current user
                FirebaseAuth.getInstance().signOut();

                //start MainActivity
                startActivity(new Intent(RiderSettingsActivity.this, MainActivity.class));

                //finish current Activity
                finish();
            }
        });

        //set OnClickListener to Button
        mRideHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //create Intent object to RideHistoryActivity
                Intent intent = new Intent(RiderSettingsActivity.this, RideHistoryActivity.class);

                //pass String through Activities
                intent.putExtra("currentUserRoot","riderId");

                //start RideHistoryActivity
                startActivity(intent);
            }
        });

        //set OnClickListener to Button
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //start ChangePasswordActivity
                startActivity(new Intent(RiderSettingsActivity.this , ChangePasswordActivity.class));
            }
        });
    }

    private void getInformationFromDatabase(){

        //add SingleValueEventListener to database reference
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //instance exists and children are available
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    //initialize Map object with the values of dataSnapshot
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //check for null
                    if (map == null)
                        return;

                    //Users -> riderId -> 'riderId' -> name available
                    if (map.get("name") != null){

                        //set text to TextView
                        mName.setText(map.get("name").toString());
                    }

                    //Users -> riderId -> 'riderId' -> tel available
                    if (map.get("tel") != null){

                        //set text to TextView
                        mTel.setText(map.get("tel").toString());
                    }

                    //Users -> riderId -> 'riderId' -> carType available
                    if (map.get("carType") != null){

                        //set text to TextView
                        mCarType.setText(map.get("carType").toString());
                    }

                    //Users -> riderId -> 'riderId' -> rideType available
                    if (map.get("rideType") != null){

                        //assign value to String object
                        rideType = map.get("rideType").toString();

                        switch (rideType){

                            //ride type id Regular
                            case "Regular" :

                                //check RadioButton
                                mRideType.check(R.id.ride_type_regular);

                                //break switch statement
                                break;

                            //ride type id Prime
                            case "Prime" :

                                //check RadioButton
                                mRideType.check(R.id.ride_type_prime);

                                //break switch statement
                                break;

                            //ride type id SUV
                            case "SUV" :

                                //check RadioButton
                                mRideType.check(R.id.ride_type_suv);

                                //break switch statement
                                break;
                        }
                    }

                    //Users -> riderId -> 'riderId' -> profileImageUrl available
                    if (map.get("profileImageUrl") != null){

                        //get pic url from database
                        profilePicUrl = map.get("profileImageUrl").toString();

                        //insert url into ImageView
                        Glide.with(getApplication()).load(profilePicUrl).into(mProfilePic);
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

    private void saveInformationToDatabase() {

        //create HAshMap object
        HashMap<String, Object> information = new HashMap<>();

        //add items to object
        information.put("name",name);
        information.put("tel",tel);
        information.put("carType",carType);
        information.put("rideType",rideType);

        //update children values of database
        mDatabaseReference.updateChildren(information);

        //show Message
        Toast.makeText(getApplicationContext(), "Changes Updated", Toast.LENGTH_SHORT).show();

        //keyboard is open
        if (mInputMethodManager != null) {

            //close keyboard
            mInputMethodManager = (InputMethodManager) RiderSettingsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = RiderSettingsActivity.this.getCurrentFocus();

            //check for null
            if (view != null) {

                //hide keyboard
                mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        //profile picture data is available
        if (uploadUri != null){

            //get FirebaseStorage reference
            final StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child("profilePic").child(riderId);
            Bitmap mBitMap = null;
            try {

                //get image data and put it in BitMap object
                mBitMap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),uploadUri);
            } catch (IOException e) {

                //show message
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG);
            }

            //create ByteArrayOutputStream object
            ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();

            //check for null
            if (mBitMap != null) {

                //compress profile pic data
                mBitMap.compress(Bitmap.CompressFormat.JPEG,50,mByteArrayOutputStream);
            }

            //get byte information from picture data
            byte[] picture = mByteArrayOutputStream.toByteArray();

            //upload picture to StorageReference
            UploadTask mUploadTask = mStorageReference.putBytes(picture);

            mUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //show message
                    Toast.makeText(getApplicationContext(), "Failed to Update", Toast.LENGTH_SHORT).show();
                }
            });

            //upload successfully completed
            mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //get url of the picture
                    mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            //create a Map object
                            HashMap<String, Object> newImage = new HashMap<>();

                            //put data in object
                            newImage.put("profileImageUrl",uri.toString());

                            //insert picture url into database reference
                            mDatabaseReference.updateChildren(newImage);

                            //show Message
                            Toast.makeText(getApplicationContext(), "Changes Updated", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            //show message
                            Toast.makeText(getApplicationContext(), "Failed to Update", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            });

            //show Message
            Toast.makeText(getApplicationContext(), "Changes Updated", Toast.LENGTH_SHORT).show();
        }

        //hide progress bar
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //case equals PROFILE_PIC_REQUEST_CODE_FROM_GALLERY
        if (requestCode == PROFILE_PIC_REQUEST_CODE_FROM_GALLERY) {//picture is selected
            if (resultCode == Activity.RESULT_OK) {

                //get Uri data
                uploadUri = data.getData();

                //set ImageView
                mProfilePic.setImageURI(uploadUri);
            }
        }
    }

}