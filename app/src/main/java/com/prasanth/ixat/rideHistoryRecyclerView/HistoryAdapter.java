package com.prasanth.ixat.rideHistoryRecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.prasanth.ixat.R;

import java.util.List;

// HistoryAdapter extends HistoryViewHolderClass so we need to override the methods onCreateViewHolder() and onBindViewHolder()
// HistoryAdapter is called in RideHistoryActivity
public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {

    private List<History> rideHistory;

    // Constructors which has a parameter List<History> and initializes it to class variable
    public HistoryAdapter(List<History> historyList) {
        this.rideHistory = historyList;
    }

    // This method will be called when we are ride_history layout is created
    @Override
    public HistoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        // ride_history in inflate() method is the layout file in which we want to display the information in CardView
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_history, parent, false);

        // sets the layout parameter width as match_parent and height as wrap_content and assigning it to layoutParams variable
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // sets the ride_history layout's parameters
        layoutView.setLayoutParams(layoutParams);

        // we need to return a View of type HistoryViewHolder so we are creating a new HistoryViewHolder with a layoutView's view
        return new HistoryViewHolders(layoutView);
    }

    //This method is used to set the views in ride_history layout file
    //holder will have the class variables of HistoryViewHolder
    //rideHistory is the list of History.java objects
    //position tells us which History.java object we accessing
    //get() methods are the methods in History.java
    @Override
    public void onBindViewHolder(HistoryViewHolders holder, int position) {

        //sets the mUniqueRideId view with the String which will returned by getUniqueRideId() method
        holder.mUniqueRideId.setText(rideHistory.get(position).getUniqueRideId());

        //sets the mTime view with the String which will returned by getTime() method
        holder.mTime.setText(rideHistory.get(position).getTime());

        //sets the mDay view with the String which will returned by getDay() method
        holder.mDay.setText(rideHistory.get(position).getDay());

        //sets the mDate view with the String which will returned by getDate() method
        holder.mDate.setText(rideHistory.get(position).getDate());

        //sets the mRideType view with the String which will returned by getRideType() method
        holder.mRideType.setText(rideHistory.get(position).getRideType());

        //sets the mPickupLocation view with the String which will returned by getPickupLocation() method
        holder.mPickupLocation.setText(rideHistory.get(position).getPickupLocation());

        //sets the mDropLocation view with the String which will returned by getDropLocation() method
        holder.mDropLocation.setText(rideHistory.get(position).getDropLocation());

        //sets the mRideTypeIcon with a image
        switch (rideHistory.get(position).getRideType()) {
            case "Regular":
                holder.mRideTypeIcon.setImageResource(R.mipmap.regular_car);
                break;
            case "Prime":
                holder.mRideTypeIcon.setImageResource(R.mipmap.prime_car);
                break;
            case "SUV":
                holder.mRideTypeIcon.setImageResource(R.mipmap.suv);
                break;
        }
    }

    //will return the items in the ride_history layout
    @Override
    public int getItemCount() {
        //we need to return rideHistory.size() because History.java objects are assigned to rideHistory
        //this will tell RecyclerView that how many CardView's should be created
        return rideHistory.size();
    }
}
