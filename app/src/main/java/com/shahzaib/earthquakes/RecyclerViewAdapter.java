package com.shahzaib.earthquakes;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shahzaib.earthquakes.Data.Earthquake;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {


    ArrayList<Earthquake> earthquakes;
    private  Context context;

    public RecyclerViewAdapter(Context context)
    {
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_recyclerview,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(earthquakes == null) return;

        //******** Bind Data
        holder.magnitude.setText(String.valueOf(earthquakes.get(position).getMagnitude()));
        Drawable magnitude_circle = holder.magnitude.getBackground();
        magnitude_circle.setColorFilter(getMagnitudeColor(earthquakes.get(position).getMagnitude()), PorterDuff.Mode.SRC_IN);
        holder.location.setText(earthquakes.get(position).getLocation());
        holder.date.setText(getDate(earthquakes.get(position).getTimeInMilli()));
        holder.time.setText(getTime(earthquakes.get(position).getTimeInMilli()));


    }


    @Override
    public int getItemCount() {
        if(earthquakes!=null)
        {
            return earthquakes.size();
        }
        else
        {
            return 0;
        }
    }



    public void setEarthquakes(ArrayList<Earthquake> earthquakes)
    {
        this.earthquakes = earthquakes;
    }

    private int getMagnitudeColor(float magnitude)
    {
        int color = -1;
        switch ((int)Math.floor(magnitude))
        {

            case 1:
                color = ContextCompat.getColor(context,R.color.magnitude1);
                break;

            case 2:
                color = ContextCompat.getColor(context,R.color.magnitude2);
                break;

            case 3:
                color = ContextCompat.getColor(context,R.color.magnitude3);
                break;

            case 4:
                color = ContextCompat.getColor(context,R.color.magnitude4);
                break;

            case 5:
                color = ContextCompat.getColor(context,R.color.magnitude5);
                break;

            case 6:
                color = ContextCompat.getColor(context,R.color.magnitude6);
                break;

            case 7:
                color = ContextCompat.getColor(context,R.color.magnitude7);
                break;

            case 8:
                color = ContextCompat.getColor(context,R.color.magnitude8);
                break;

            case 9:
                color = ContextCompat.getColor(context,R.color.magnitude9);
                break;

            case 10:
                color = ContextCompat.getColor(context,R.color.magnitude10);
                break;

        }

        if(((int)Math.floor(magnitude))>10)
        {
            color = ContextCompat.getColor(context,R.color.magnitude10);
        }

        return color;
    }


    private String getDate(long timeInMilli) {
        /**
         * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
         */
        Date date = new Date(timeInMilli);
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    private String getTime(long timeInMilli) {
        /**
         * Return the formatted date string (i.e. "4:30 PM") from a Date object.
         */
        Date date = new Date(timeInMilli);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a",Locale.getDefault());
        return timeFormat.format(date);
    }




    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView magnitude,location,date,time;

        public ViewHolder(View itemView) {
            super(itemView);
            magnitude = itemView.findViewById(R.id.magnitude);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
        }
    }
}
