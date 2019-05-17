package com.prasanth.ixat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;

    private RadioGroup mUserType;

    private RadioButton mDriver, mCustomer;

    private FirebaseAuth mAuth;

    private ProgressBar mProgressBar;

    private Toolbar mToolbar;

    private final int LOCATION_PERMISSION_CODE = 92;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //initialize Toolbar
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("Login User");

        //initialize ProgressBar
        mProgressBar = findViewById(R.id.progress);

        //initialize EditText
        mEmail = findViewById(R.id.email_id);
        mPassword = findViewById(R.id.pass_word);

        //initialize RadioGroup
        mUserType = findViewById(R.id.user_type);

        //initialize RadioButton
        mDriver = findViewById(R.id.driver);
        mCustomer = findViewById(R.id.customer);

        //initialize Button
        Button mLogin = findViewById(R.id.user_login);
        Button mRegister = findViewById(R.id.user_registration);
        Button mForgetPassword = findViewById(R.id.forget_password);

        //check radio button
        mCustomer.setChecked(true);
        mCustomer.setBackgroundColor(getResources().getColor(R.color.radio));

        //set onCheckChangeListener to RadioGroup
        mUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                //customer is checked
                if (mCustomer.isChecked()) {

                    //change Customer and Driver Background
                    mCustomer.setBackgroundColor(getResources().getColor(R.color.radio));
                    mDriver.setBackgroundColor(Color.WHITE);
                }

                //driver is checked
                if (mDriver.isChecked()) {

                    //change Customer and Driver Background
                    mCustomer.setBackgroundColor(Color.WHITE);
                    mDriver.setBackgroundColor(getResources().getColor(R.color.radio));
                }
            }
        });

        //set OnClickListener to Register
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //set title
                mToolbar.setTitle("Register user");

                //assign email with the text in mEmail EditText view
                final String email = mEmail.getText().toString();

                //assign password with the text in mPassword EditText view
                final String password = mPassword.getText().toString();

                //check if email is null
                if (email.equals("")) {

                    //show error
                    mEmail.setError("Required field");

                    return;
                }

                //check if password is null
                if (password.equals("")) {

                    //show error
                    mPassword.setError("Required field");

                    return;
                }

                //set progress bar visible
                mProgressBar.setVisibility(View.VISIBLE);

                //create a account with given credentials
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //account created
                        if (task.isSuccessful()) {

                            //get current user id
                            String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                            //get user type
                            int selectedId = mUserType.getCheckedRadioButtonId();
                            final RadioButton selectedRadioButton = findViewById(selectedId);
                            String userType = selectedRadioButton.getText().toString();

                            //user is customer
                            if (userType.equals("Customer")) {

                                //get reference to database
                                DatabaseReference db_ref = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(userId);

                                //set value to true
                                db_ref.setValue(true);

                                //get reference to database
                                DatabaseReference db_ref_email = FirebaseDatabase.getInstance().getReference().child("Users").child("customerId").child(userId).child("email");

                                db_ref_email.setValue(email);
                            }

                            //user is driver
                            if (userType.equals("Driver")) {

                                //get reference to database
                                DatabaseReference db_ref = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(userId);

                                //set value to true
                                db_ref.setValue(true);

                                //get reference to database
                                DatabaseReference db_ref_email = FirebaseDatabase.getInstance().getReference().child("Users").child("riderId").child(userId).child("email");

                                db_ref_email.setValue(email);
                            }

                            //create a Builder object
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            //set builder title
                            builder.setTitle("Registered Successfully");

                            //set builder icon
                            builder.setIcon(R.mipmap.completed);

                            //set message
                            builder.setMessage("You have to verify your email address, and login with your credentials");

                            //set Button
                            builder.setPositiveButton("send verification email", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //set progress bar visible
                                    mProgressBar.setVisibility(View.VISIBLE);

                                    //send verification email
                                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            //email send
                                            if (task.isSuccessful()) {

                                                //set progress bar gone
                                                mProgressBar.setVisibility(View.GONE);

                                                //show message
                                                Toast.makeText(getApplicationContext(), "Verification Email Sent", Toast.LENGTH_SHORT).show();

                                            }
                                            //email not sent
                                            else{

                                                //set progress bar gone
                                                mProgressBar.setVisibility(View.GONE);

                                                //show message
                                                Toast.makeText(getApplicationContext(), "Error : " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });

                            //set progress bar gone
                            mProgressBar.setVisibility(View.GONE);

                            //build and show builder
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        //account not created
                        else {

                            //set progress bar gone
                            mProgressBar.setVisibility(View.GONE);

                            //show message
                            Toast.makeText(getApplicationContext(), "Error : " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

        });

        //set OnClickListener to Button
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //set title
                mToolbar.setTitle("Login user");

                //assign email with the text in mEmail EditText view
                final String email = mEmail.getText().toString();

                //assign password with the text in mPassword EditText view
                final String password = mPassword.getText().toString();

                //check if email is null
                if (email.equals("")) {

                    //show error
                    mEmail.setError("Required field");

                    return;
                }

                //check if password is null
                if (password.equals("")) {

                    //show error
                    mPassword.setError("Required field");

                    return;
                }

                //set progress bar visible
                mProgressBar.setVisibility(View.VISIBLE);

                //login user
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            //email is verified
                            if (Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()) {

                                //get user type
                                int selectedId = mUserType.getCheckedRadioButtonId();
                                final RadioButton selectedRadioButton = findViewById(selectedId);
                                String userType = selectedRadioButton.getText().toString();

                                //user is customer
                                if (userType.equals("Customer")) {

                                    //set progress bar gone
                                    mProgressBar.setVisibility(View.GONE);

                                    //show message
                                    Toast.makeText(getApplicationContext(), "Logging in", Toast.LENGTH_SHORT).show();

                                    //start map activity
                                    startActivity(new Intent(MainActivity.this, CustomerMapActivity.class));

                                    //finish current Activity
                                    finish();
                                }

                                //user is driver
                                if (userType.equals("Driver")) {

                                    //set progress bar gone
                                    mProgressBar.setVisibility(View.GONE);

                                    //show message
                                    Toast.makeText(getApplicationContext(), "Logging in", Toast.LENGTH_SHORT).show();

                                    //create intent object to RiderMapActivity
                                    Intent intent = new Intent(MainActivity.this, RiderMapActivity.class);

                                    //set available switch of rider
                                    intent.putExtra("riderAvailable", true);

                                    //start map activity
                                    startActivity(intent);

                                    //finish current Activity
                                    finish();
                                }
                            }
                            //email is not verified
                            else {

                                //create a Builder object
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                //set title to builder
                                builder.setTitle("Email is not verified");

                                //set icon
                                builder.setIcon(R.mipmap.danger);

                                //set message
                                builder.setMessage("Your e-mail address is not verified, please verify your email address and login again");

                                //set Button
                                builder.setPositiveButton("send verification email", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //set progress bar visible
                                        mProgressBar.setVisibility(View.VISIBLE);

                                        //send verification email
                                        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                //email send
                                                if (task.isSuccessful()) {

                                                    //set progress bar gone
                                                    mProgressBar.setVisibility(View.GONE);

                                                    //show message
                                                    Toast.makeText(getApplicationContext(), "Verification Email Sent", Toast.LENGTH_SHORT).show();

                                                }
                                                //email not sent
                                                else{

                                                    //set progress bar gone
                                                    mProgressBar.setVisibility(View.GONE);

                                                    //show message
                                                    Toast.makeText(getApplicationContext(), "Error : " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                });

                                //set progress bar gone
                                mProgressBar.setVisibility(View.GONE);

                                //build and show builder
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                        }
                        //login failed
                        else {

                            //set progress bar gone
                            mProgressBar.setVisibility(View.GONE);

                            //show message
                            Toast.makeText(getApplicationContext(), "Error : " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });

        //set OnClickListener to Button
        mForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //set title
                mToolbar.setTitle("Forget password");

                //assign email with the text in mEmail EditText view
                final String email = mEmail.getText().toString();

                //check if email is null
                if (email.equals("")) {

                    //show error
                    mEmail.setError("Required field");

                    return;
                }

                //set progress bar visible
                mProgressBar.setVisibility(View.VISIBLE);

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        //email sent
                        if (task.isSuccessful()){

                            //set progress bar gone
                            mProgressBar.setVisibility(View.GONE);

                            //show message
                            Toast.makeText(getApplicationContext(), "Password reset email sent to your email", Toast.LENGTH_SHORT).show();
                        }
                        //email not send
                        else {

                            //set progress bar gone
                            mProgressBar.setVisibility(View.GONE);

                            //show message
                            Toast.makeText(getApplicationContext(), "Error : " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //create a Intent object to main action
        Intent i = new Intent(Intent.ACTION_MAIN);

        //add home category
        i.addCategory(Intent.CATEGORY_HOME);

        //set clear top flag
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //start main action intent
        startActivity(i);

        //finish current activity
        finish();

        //exit
        System.exit(0);
    }
}
