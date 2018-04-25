package koti.blescanapi21;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by Proot on 5.4.2018.
 */

public class LocationFetch extends Service implements LocationListener {

    private Context context;
    private Activity activity;
    protected LocationManager locationManager;

    Location locationNet;
    Location locationGps;
    double latitude;
    double longitude;

    private String NLOC;
    private String ELOC;

    private boolean gps_on = false;
    private boolean net_on = false;

    private long lastGpsFix;

/*
    public LocationFetch(){

    }

    public LocationFetch(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        //locationManager = (LocationManager) this.context.getSystemService(context.LOCATION_SERVICE);
    }
    */

    public void onCreate() {
        Log.d("JALAJALA", "LOCATIONFETCH_CREATE");
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        //LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        try {
            Log.d("JALAJALA", "OIKEUDET_ON");
            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);

            gps_on = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            net_on = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.d("JALAJALA GPS_ON: ", String.valueOf(gps_on));
            Log.d("JALAJALA NET_ON: ", String.valueOf(net_on));

            if (locationGps == null) {
                Log.d("JALAJALA: ", "Location NULL");

                if (gps_on) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
                    //location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } else {
                    Log.d("JALAJALA: ", "NETWORK PROVIDER REQUEST");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, this);
                }
                if (locationManager != null) {
                    Log.d("JALAJALA: ", "LocationManager NOT NULL");

                    if (gps_on) {
                        locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        lastGpsFix = locationGps.getTime();
                        //location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    } else {
                        locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    //NLOC = String.valueOf(location.getLatitude());
                    //ELOC = String.valueOf(location.getLongitude());
                    //sendResults();

                    //if (locationGps != null) {
                    if (lastGpsFix < 5000) {
                        Log.d("JALAJALA: ", "Location NOT NULL");
                        //latitude = location.getLatitude();
                        //longitude = location.getLongitude();
                        NLOC = String.valueOf(locationGps.getLatitude());
                        ELOC = String.valueOf(locationGps.getLongitude());
                    }

                    //korjaa jos eka location (aiempi if) location == null (eli ei gps lokaatiota
                    //niin käytä network lokaatiota
                    else{
                        Log.d("JALAJALA", "NETLOC");
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, this);
                        locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        NLOC = String.valueOf(locationNet.getLatitude());
                        ELOC = String.valueOf(locationNet.getLongitude());
                    }
                    sendResults();
                }
            }

        } catch (SecurityException e) {
            Log.d("JALAJALA", e.toString());
        }
    }

    //public void onStart(Intent intent, int startID) {
    //    super.onStart(intent, startID);
    //}

    public int onStartCommand(Intent intent, int flags, int startID) {
        Log.d("JALAJALA", "LOCATIONFETCH_ONSTARTCOMMAND");
        super.onStartCommand(intent, flags, startID);
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }


    public double getLocN() {
        if (locationGps != null) {
            latitude = locationGps.getLatitude();
        }
        return latitude;
    }

    public double getLocE() {
        if (locationGps != null) {
            longitude = locationGps.getLongitude();
        }
        return longitude;
    }

    @SuppressLint("MissingPermission")
    public void sendResults() {
        locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (locationGps != null) {
            if (NLOC != null && ELOC != null) {
                if (lastGpsFix < 5000) {
                    Log.d("JALAJALA: ", "GPSLOC");
                    //latitude = location.getLatitude();
                    //longitude = location.getLongitude();
                    NLOC = String.valueOf(locationGps.getLatitude());
                    ELOC = String.valueOf(locationGps.getLongitude());
                }
                else{
                    Log.d("JALAJALA", "NETLOC");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, this);
                    locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    NLOC = String.valueOf(locationNet.getLatitude());
                    ELOC = String.valueOf(locationNet.getLongitude());
                }
            }
        }

        //korjaa jos eka location (aiempi if) location == null (eli ei gps lokaatiota
        //niin käytä network lokaatiota
        else{
            Log.d("JALAJALA", "NETLOC");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, this);
            locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            NLOC = String.valueOf(locationNet.getLatitude());
            ELOC = String.valueOf(locationNet.getLongitude());
        }

        Intent intent = new Intent("JALAEVENTNAME");
        //intent.putExtra("NLOC", String.valueOf(location.getLatitude()));
        //intent.putExtra("ELOC", String.valueOf(location.getLongitude()));
        intent.putExtra("NLOC", NLOC);
        intent.putExtra("ELOC", ELOC);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    public void onLocationChanged(Location location) {
        String message = "N: " + location.getLatitude() + "\nE: " + location.getLongitude();
        //Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
        //Log.d("JALA", message);
        NLOC = String.valueOf(location.getLatitude());
        ELOC = String.valueOf(location.getLongitude());
        sendResults();

        MapsMarkerActivity.updateOwnLocation(NLOC, ELOC);

    }

    public void onProviderDisabled(String provider) {
        //Intent intentti = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //startActivity(intentti);
        Log.d("JALAJALA", "GPS IS TURNED OFF");
        Toast.makeText(this, "GPS IS TURNED OFF, PLEASE TURN IT ON TO USE THIS APP", Toast.LENGTH_SHORT).show();

    }

    public void onProviderEnabled(String provider) {
        Log.d("JALAJALA", "GPS IS ON");
        Toast.makeText(this, "GPS IS ON", Toast.LENGTH_SHORT).show();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Something something
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}