package com.prasanth.ixat.rideHistoryRecyclerView;

//This class is used as a bridge to set the views of CardView
//History is called in RideHistoryActivity
public class History {

    private String uniqueRideId;
    private String time;
    private String day;
    private String date;
    private String rideType;
    private String pickupLocation;
    private String dropLocation;

    public History(String uniqueRideId, String time, String day, String date,String rideType, String pickupLocation, String dropLocation){

        //initialize class variables
        this.uniqueRideId = uniqueRideId;
        this.time = time;
        this.day = day;
        this.date = date;
        this.rideType = rideType;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
    }

    //This method returns uniqueRideId
    String getUniqueRideId(){
        return uniqueRideId;
    }

    //This method returns time
    String getTime(){
        return time;
    }

    //This method returns day
    String getDay(){
        return day;
    }

    //This method returns date
    String getDate(){
        return date;
    }

    //This method returns rideType
    String getRideType(){
        return rideType;
    }

    //This method returns pickupLocation
    String getPickupLocation(){
        return pickupLocation;
    }

    //This method returns dropLocation
    String getDropLocation(){
        return dropLocation;
    }
}
