package com.shahzaib.earthquakes.Data;

import android.content.Context;

public class Earthquake {
    //magnitude,location_offset,primary_location,date,time;
    String location;
    long timeInMilli;
    float magnitude;

    public Earthquake(float magnitude, String location, long timeInMilli)
    {
        this.magnitude = magnitude;
        this.location = location;
        this.timeInMilli = timeInMilli;
    }



    public float getMagnitude() {
        return magnitude;
    }

    public String getLocation() {
        return location;
    }

    public long getTimeInMilli(){
        return timeInMilli;
    }
}
