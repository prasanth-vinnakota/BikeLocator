package com.prasanth.ixat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.r0adkll.slidr.Slidr;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    TextView mEmail;
    EditText mCurrentPassword;
    EditText mNewPassword;
    Button mUpdatePassword;
    String newPassword;
    String currentPassword;
    Toolbar mToolbar;
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        //add exit animation
        Slidr.attach(this);

        //initialize and set title for tool bar
        mToolbar = findViewById(R.id.toolbar_password);
        mToolbar.setTitle("Change Password");

        //initialize ProgressBar
        mProgressBar = findViewById(R.id.progress_password);

        //get current user instance
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //initialize TextView
        mEmail = findViewById(R.id.email);

        //initialize EditText
        mNewPassword = findViewById(R.id.new_password);
        mCurrentPassword = findViewById(R.id.current_password);
        mUpdatePassword = findViewById(R.id.update_password);

        //set text to TextView
        mEmail.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        //add OnClickListener to Button
        mUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                //assign new password to String object
                newPassword = mNewPassword.getText().toString();

                //assign current password to String object
                currentPassword = mCurrentPassword.getText().toString();

                //both passwords are not empty
                if (!newPassword.equals("") && !currentPassword.equals("")) {

                    //create AuthCredential object with current user credentials
                    AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()), currentPassword);

                    //user exists
                    if (user != null) {

                        //re-authenticate user credentials
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                //re-authenticate success
                                if (task.isSuccessful()){

                                    //update user password
                                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            //password updated successfully
                                            if (task.isSuccessful()){

                                                //hide progress bar
                                                mProgressBar.setVisibility(View.GONE);

                                                //show message
                                                Toast.makeText(ChangePasswordActivity.this, "Password Updated Successful", Toast.LENGTH_LONG).show();
                                            }
                                            //password not updated
                                            else {

                                                //hide progress bar
                                                mProgressBar.setVisibility(View.GONE);

                                                //show message
                                                Toast.makeText(ChangePasswordActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                                //re-authenticate failed
                                else{

                                    //hide progress bar
                                    mProgressBar.setVisibility(View.GONE);

                                    //show message
                                    Toast.makeText(ChangePasswordActivity.this,Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
                //mNewPassword or mCurrentPassword fields is empty
                else {

                    //show message
                    Toast.makeText(ChangePasswordActivity.this, "All fields are required", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
