package com.prasanth.ixat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

//This class will be called when Application is Destroyed
public class OnApplicationDestroyed extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //when Application removed
    @Override
    public void onTaskRemoved(Intent rootIntent) {

        super.onTaskRemoved(rootIntent);

        //get current user id
        String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //create database reference for riderAvailable
        DatabaseReference riderAvailableDatabaseReference = FirebaseDatabase.getInstance().getReference().child("riderAvailable");

        //initialize GeoFire object with database reference
        final GeoFire geoFire = new GeoFire(riderAvailableDatabaseReference);

        //remove location of current user from riderAvailable
        geoFire.removeLocation(user_id, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



