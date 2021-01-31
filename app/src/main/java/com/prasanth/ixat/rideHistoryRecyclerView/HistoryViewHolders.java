package com.prasanth.ixat.rideHistoryRecyclerView;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.prasanth.ixat.HistoryActivity;
import com.prasanth.ixat.R;

//This class is extended by HistoryAdapter
//HistoryViewHolder extends RecyclerView.ViewHolder to assign the CardViews in RecyclerView
public class HistoryViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    TextView mUniqueRideId;
    TextView mTime;
    TextView mDay;
    TextView mDate;
    TextView mRideType;
    TextView mPickupLocation;
    TextView mDropLocation;
    ImageView mRideTypeIcon;


    HistoryViewHolders(View itemView) {
        super(itemView);
        //setting OnClickListener to the RecyclerView
        itemView.setOnClickListener(this);

        //initialize TextViews
        mUniqueRideId = itemView.findViewById(R.id.unique_ride_id);
        mTime = itemView.findViewById(R.id.time);
        mDay = itemView.findViewById(R.id.day);
        mDate = itemView.findViewById(R.id.date);
        mRideType = itemView.findViewById(R.id.ride_type);
        mPickupLocation = itemView.findViewById(R.id.pick_up_location);
        mDropLocation = itemView.findViewById(R.id.drop_location);

        //initialize ImageView
        mRideTypeIcon = itemView.findViewById(R.id.ride_type_icon);
    }

    //This method is called when we click any card in the Recycler View
    @Override
    public void onClick(View v) {

        //creating an Intent object to change the current Activity to HistoryActivity
        Intent intent = new Intent(v.getContext(), HistoryActivity.class);

        //passing uniqueRideId to HistoryActivity
        intent.putExtra("uniqueRideId", mUniqueRideId.getText().toString().substring(3));

        //starting HistoryActivity from the present Application Context
        v.getContext().startActivity(intent);
    }
}
