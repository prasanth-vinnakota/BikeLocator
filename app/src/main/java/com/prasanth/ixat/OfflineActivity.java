package com.prasanth.ixat;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//This class checks for internet connection
public class OfflineActivity extends AppCompatActivity {

    //boolean variable sets true when internet is connected
    private boolean internet_connection = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.offline);

        //initializing Button views
        Button mBookOffline = findViewById(R.id.book_offline);
        Button mGoOnline = findViewById(R.id.go_online);

        //setting OnClickListener to mBookOffline Button View
        mBookOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //creating intent object to open Action View
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);

                //set intent type to send mms or sms
                smsIntent.setType("vnd.android-dir/mms-sms");

                //enter mobile number
                smsIntent.putExtra("address"  , "9100362607");

                //enter message
                smsIntent.putExtra("sms_body"  , "Need a Cab at");

                try {

                    //starting Action View intent
                    startActivity(smsIntent);

                    //finish current Activity
                    finish();
                } catch (android.content.ActivityNotFoundException ex) {

                    //show error message
                    Toast.makeText(OfflineActivity.this,
                            "SMS failed, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //setting OnClickListener to mGoOnline Button View
        mGoOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //checking Internet Connection
                checkInternetConnection();

                //internet connected
                if (internet_connection){

                    //create intent object to change the current Activity to LoadingActivity
                    Intent intent = new Intent(OfflineActivity.this, LoadingActivity.class);

                    //start LoadingActivity
                    startActivity(intent);

                    //finish current Activity
                    finish();
                }
                // no internet connection
                else{

                    //show message
                    Toast.makeText(OfflineActivity.this,"No Internet Connection",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //This method sets mInternetConnection to true if internet is connected
    private void checkInternetConnection(){

        //initialize connectivityManager to get the statuses of connectivity services
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

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

            //set mInternetConnection to true
            internet_connection = true;
        }
    }
}
