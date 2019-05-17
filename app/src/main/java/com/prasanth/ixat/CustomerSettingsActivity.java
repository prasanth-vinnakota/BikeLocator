package com.prasanth.ixat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class CustomerSettingsActivity extends AppCompatActivity {

    private static final int PROFILE_PIC_REQUEST_CODE_FROM_GALLERY = 92;
    private Button mSubmit;
    private EditText mName;
    private EditText mTel;
    private DatabaseReference mDatabaseReference;
    private String mCustomerId;
    private String name;
    private String tel;
    private String mProfilePicUrl;
    private ImageView mProfilePic;
    private Uri uploadUri;
    private InputMethodManager mInputMethodManager;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_settings);

        //set ActivityExitTheme to this Activity
        Slidr.attach(this);

        //initialize ToolBar
        Toolbar mToolBar = findViewById(R.id.toolbar_customer_setting);
        mToolBar.setTitle("Settings");

        //initialize progress bar
        mProgressBar = findViewById(R.id.progress_customer_setting);

        //set orientation to portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //initialize Button
        mSubmit = findViewById(R.id.submit_customer);
        Button mLogout = findViewById(R.id.logout_customer);
        Button mRideHistory = findViewById(R.id.ride_history_customer);
        Button mChangePassword = findViewById(R.id.change_password_customer);

        //initialize ImageView
        mProfilePic = findViewById(R.id.profile_pic);

        //initialize EditText
        mName = findViewById(R.id.name);
        mTel = findViewById(R.id.tel);

        //initialize FirebaseAuth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //get current user id
        mCustomerId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        //database reference for Users -> customerId -> 'mCustomerId'
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(mCustomerId);

        //method call
        getInformationFromDatabase();

        //set OnClickListener to Button
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerSettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //set OnClickListener to Button
        mRideHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerSettingsActivity.this, RideHistoryActivity.class);
                intent.putExtra("currentUserRoot", "customerId");
                startActivity(intent);

            }
        });

        //set OnClickListener to Button
        mTel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //enable submit button
                mSubmit.setEnabled(true);

                //show keyboard
                mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (mInputMethodManager != null) {
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        //set OnClickListener to Button
        mName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enable submit button
                mSubmit.setEnabled(true);

                //show keyboard
                mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (mInputMethodManager != null) {
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        //set OnClickListener to Button
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //get values from database
                name = mName.getText().toString();
                tel = mTel.getText().toString();

                //check for null
                if (name.equals("")) {

                    //show error
                    mName.setError("required field");

                    return;
                }

                //check for null
                if (tel.equals("")) {

                    //show error
                    mTel.setError("required field");

                    return;
                }

                //method call
                saveInformationToDatabase();
            }
        });

        //set OnClickListener to ImageButton
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //intent to open gallery
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PROFILE_PIC_REQUEST_CODE_FROM_GALLERY);

                //enable Button
                mSubmit.setEnabled(true);
            }
        });

        //set OnClickListener to Button
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //start ChangePasswordActivity
                startActivity(new Intent(CustomerSettingsActivity.this, ChangePasswordActivity.class));
            }
        });
    }

    private void getInformationFromDatabase() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //database is exists and children are exist
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    //get values from database
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    //check for null
                    if (map == null)
                        return;

                    // Users -> customerId -> 'mCustomerId' -> name exists
                    if (map.get("name") != null) {

                        //set EditText
                        mName.setText(map.get("name").toString());
                    }

                    // Users -> customerId -> 'mCustomerId' -> tel exists
                    if (map.get("tel") != null) {

                        //set EditText
                        mTel.setText(map.get("tel").toString());
                    }

                    // Users -> customerId -> 'mCustomerId' -> profileImageUrl exists
                    if (map.get("profileImageUrl") != null) {

                        //get url from database
                        mProfilePicUrl = map.get("profileImageUrl").toString();

                        //insert profile pic to ImageView using Glide
                        Glide.with(getApplication()).load(mProfilePicUrl).into(mProfilePic);
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

        //create a HastMap object
        HashMap<String, Object> information = new HashMap<>();

        //put data to HashMap
        information.put("name", name);
        information.put("tel", tel);

        //update children of Users -> customerId -> 'mCustomerId'
        mDatabaseReference.updateChildren(information);

        //check for null
        if (mInputMethodManager != null) {

            //get Input Method Services
            mInputMethodManager = (InputMethodManager) CustomerSettingsActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);

            //get current input method focus
            View view = CustomerSettingsActivity.this.getCurrentFocus();

            //check for null
            if (view != null) {

                //hide keyboard
                mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        //check for null
        if (uploadUri != null) {

            //get reference for FirebaseStorage
            final StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child("profilePic").child(mCustomerId);

            Bitmap mBitMap = null;

            try {

                //convert url to bitmap
                mBitMap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), uploadUri);
            } catch (IOException e) {

                //show message
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            //create a ByteArrayOutputStream object
            ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();

            //check for null
            if (mBitMap != null) {

                //compress ByteOutputStream
                mBitMap.compress(Bitmap.CompressFormat.JPEG, 50, mByteArrayOutputStream);
            }

            //convert ByteArrayOutputStream to ByteArray
            byte[] picture = mByteArrayOutputStream.toByteArray();

            //put bytes to database and get url
            UploadTask mUploadTask = mStorageReference.putBytes(picture);

            //add OnFailureListener to UploadTask
            mUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    //show message
                    Toast.makeText(getApplicationContext(), "Failed to Update", Toast.LENGTH_SHORT).show();
                }
            });

            //add OnSuccessListener to UploadTask
            mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //get image url
                    mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            //create a Map object
                            Map<String, Object> newImage = new HashMap<>();

                            //put values to Map object
                            newImage.put("profileImageUrl", uri.toString());

                            //update children of Users -> customerId -> 'mCustomerId'
                            mDatabaseReference.updateChildren(newImage);

                            //show message
                            Toast.makeText(getApplicationContext(), "Changes Updated", Toast.LENGTH_SHORT).show();

                        }
                    })
                            //add OnFailureListener to StorageReference
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    //show message
                                    Toast.makeText(getApplicationContext(), "Failed to Update", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
        //url is null
        else {
            //show message
            Toast.makeText(getApplicationContext(), "Changes Updated", Toast.LENGTH_SHORT).show();
        }

        //hide progress bar
        mProgressBar.setVisibility(View.GONE);

    }

    //this method will call when ImageView is called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //request code is equal to 92
        if (requestCode == PROFILE_PIC_REQUEST_CODE_FROM_GALLERY) {

            //pic selected
            if (resultCode == Activity.RESULT_OK) {

                //get url from intent
                uploadUri = data.getData();

                //set url to ImageView
                mProfilePic.setImageURI(uploadUri);
            }
        }
    }
}
