package com.shahzaib.earthquakes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shahzaib.earthquakes.Data.Earthquake;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Earthquake>> {

    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    TextView emptyListView, noInternetConnection;
    ProgressBar loading_spinner;


    // register for listening Networking Connectivity state
    IntentFilter intentFilter = new IntentFilter();
    NetworkConnectivityListener networkConnectivityListener = new NetworkConnectivityListener();







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emptyListView = findViewById(R.id.empty_list);
        noInternetConnection = findViewById(R.id.no_internet_connection);
        loading_spinner = findViewById(R.id.loading_spinner);
        // register for network connectivity change
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");



        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new RecyclerViewAdapter(this);


        loading_spinner.setVisibility(View.VISIBLE);

        // first check the network connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();


        if(isConnected) onNetworkConnected();
        else onNetworkDisconnected();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkConnectivityListener,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkConnectivityListener);
    }





    /** Helper Methods */
    private void onNetworkConnected()
    {
        loading_spinner.setVisibility(View.VISIBLE);
        // start Loader; that download and parse data and after all, give data to adapter to display on recyclerView
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    private void onNetworkDisconnected()
    {
        loading_spinner.setVisibility(View.GONE);
        emptyListView.setVisibility(View.GONE);
        noInternetConnection.setVisibility(View.VISIBLE);
        adapter.setEarthquakes(new ArrayList<Earthquake>()); // also empty the recycler View
        recyclerView.setAdapter(adapter);
    }









    /**Loader Callbacks*/
    @NonNull
    @Override
    public android.support.v4.content.Loader<ArrayList<Earthquake>> onCreateLoader(int id, @Nullable Bundle args) {
        Log.i("123456", "Loader is Created");
        // loader AsynckTask k doInBackground() method ko automatically run kr dy ga aur result onLoadFinished mathod ko dy dy ga.
        return new DownloadEarthquakeData(MainActivity.this);
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<ArrayList<Earthquake>> loader, ArrayList<Earthquake> earthquakes) {
        Log.i("123456", "OnLoadFinished Called");
        loading_spinner.setVisibility(View.GONE);
        // when Download and parsing data is completed then, get the data and pass it to adapter
        adapter.setEarthquakes(earthquakes);
        recyclerView.setAdapter(adapter);
        if (earthquakes.size() <= 0) {
            emptyListView.setVisibility(View.VISIBLE);
            noInternetConnection.setVisibility(View.GONE);
        } else {
            emptyListView.setVisibility(View.GONE);
            noInternetConnection.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<ArrayList<Earthquake>> loader) {
        // when activity is destroyed, loaderManager destroyed the loader, so empty the list data
        adapter.setEarthquakes(new ArrayList<Earthquake>());
        recyclerView.setAdapter(adapter);
    }
























    /** Network connectivity listener*/
    class NetworkConnectivityListener extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("android.net.conn.CONNECTIVITY_CHANGE"))
            { // network connectivity is changed
                Log.i("123456","Connectivity change");
                // first check the network connectivity (is it on OR off)
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();


                if(isConnected) onNetworkConnected();
                else onNetworkDisconnected();
            }
        }
    }




/**
 * This Class will parse the JSON data to extract the Earth quake information and store into ArrayList
 */

    /**
     * Apni zuban main:
     * Ye class earthquakes data ko background main download kry gi aur downloaded json string main sy data ko
     * extract kr k earthquakes arraylist main add kry gi aur vo array list return kr dy gi
     */
    static class DownloadEarthquakeData extends AsyncTaskLoader<ArrayList<Earthquake>> {

        private final String EARTHQUAKE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2017-01-01&endtime=2017-12-31&minmag=6";


        DownloadEarthquakeData(@NonNull Context context) {
            super(context);
        }

        @Nullable
        @Override
        public ArrayList<Earthquake> loadInBackground() {

            try {
                //***creating a connection & downloading the data
                URL url = new URL(EARTHQUAKE_URL);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setReadTimeout(10000);
                httpsURLConnection.setConnectTimeout(15000);
                httpsURLConnection.connect();
                InputStream inputStream = httpsURLConnection.getInputStream(); // downloading the data
                String JSONString = readFromStream(inputStream); // convert the downloaded byte stream into appropriate form
                // parsing the data and return ArrayList
                if (JSONString != null) {
                    return parseData(JSONString);
                } else {
                    Log.i("123456", "Error While Parsing the String");
                    throw new JSONException("Error While Parsing the JSONString");
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new ArrayList<>(); // if data parse failed return empty list
        }

        private String readFromStream(InputStream inputStream) {
            StringBuilder stringBuilder = new StringBuilder();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            try {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = reader.readLine();
                }
                // after getting all the JSON sting in stringBuilder, build the string return


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStreamReader.close();
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (stringBuilder.length() > 0) {
                return stringBuilder.toString();
            } else {
                return null;
            }
        }

        @NonNull
        private ArrayList<Earthquake> parseData(@NonNull String JSONString) { // this function will extract the json data and put in earthquake arrayList and return the list
            ArrayList<Earthquake> earthquakes = new ArrayList<>();
            String location;
            long timeInMilli;
            float magnitude;

            try {
                JSONObject jsonObject = new JSONObject(JSONString);
                JSONArray jsonArray = jsonObject.getJSONArray("features");
                int objectCount = jsonArray.length();
                for (int i = 0; i < objectCount; i++) {
                    JSONObject properties = jsonArray.getJSONObject(i).getJSONObject("properties");
                    magnitude = (float) properties.getDouble("mag");
                    location = properties.getString("place");
                    timeInMilli = properties.getLong("time");
                    earthquakes.add(new Earthquake(magnitude, location, timeInMilli));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("123456", "JSONException, Invalid String");
            }
            return earthquakes;
        }

    }

}
