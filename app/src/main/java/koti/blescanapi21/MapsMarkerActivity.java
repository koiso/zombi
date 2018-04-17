package koti.blescanapi21;

/**
 * Created by Proot on 8.4.2018.
 */

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.R.layout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    String locN;
    String locE;
    private static double locNN;
    private static double locEE;

    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager locationManager;

    private final static int REQUEST_ENABLE_BT = 1;
    final static int BLUETOOTH_PERMISSION_REQUEST_CODE = 0;

    private static GoogleMap map;
    private static Context context;
    private static List<String[]> nodes;
    private static String[] node;
    private static int koko;
    private static int markersize = 0;
    private static List<Marker> markers = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Intent intententti = getIntent();
        //locN = intententti.getStringExtra("locN");
        //locE = intententti.getStringExtra("locE");
        MapsMarkerActivity.context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //create crap for permissions
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //if bt not enabled, ask to enable it - and so on...
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtintent, REQUEST_ENABLE_BT);

            if (Build.VERSION.SDK_INT >= 23) { // Marshmallow
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
            }

        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent enableGpsintent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(enableGpsintent);
        }

        //If there is no rights for location:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("JALAJALA: ", "LOC OIKEUS_CHECK");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            startService(new Intent(this, LocationFetch.class));
            startService(new Intent(this, BleScanner.class));
        } else {
            //start services for location and ble info.
            //location passes loc parameters to ble which passes them to db.
            Log.d("JALAJALA: ", "LOC OIKEUDET_ON");
            startService(new Intent(this, LocationFetch.class));
            startService(new Intent(this, BleScanner.class));

        }
    }

    protected void onResume() {
        super.onResume();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtintent, REQUEST_ENABLE_BT);

            if (Build.VERSION.SDK_INT >= 23) { // Marshmallow
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
            }

        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent enableGpsintent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(enableGpsintent);
        }

        //If there is no rights for location:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d("JALAJALA: ", "LOC OIKEUS_CHECK");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Log.d("JALAJALA: ", "LOC OIKEUDET_ON");
            //startService(new Intent(this, LocationFetch.class));
            //startService(new Intent(this, BleScanner.class));
        }
    }


    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googlemap) {
        Log.d("JALAJALA", "ONMAPREADY");
        map = googlemap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) { // Marshmallow
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
            }
            return;
        }

        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(this);

        final DatabaseHandler db = new DatabaseHandler(this);
        nodes = db.getData();

        if (nodes == null) {
        }
        else {
            for (String[] node : nodes) {

                Log.d("JALAJALA", Arrays.toString(node));

                //get values from list
                String title = node[0];
                locNN = Double.parseDouble(node[1]);
                locEE = Double.parseDouble(node[2]);
                String address = node[3];

                //create marker from values and add to map
                LatLng node1 = new LatLng(locNN, locEE);
                Marker marker;
                marker = map.addMarker(new MarkerOptions()
                        .position(node1)
                        .snippet(address)
                        .title(title));

                //add marker to arraylist markers so it can be deleted later
                markers.add(marker);

                /*LatLng node1 = new LatLng(locNN, locEE);
                map.addMarker(new MarkerOptions()
                        .position(node1)
                        .snippet(address)
                        .title(title));
                map.moveCamera(CameraUpdateFactory.newLatLng(node1));*/
            }
        }
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String title = marker.getTitle();
                marker.remove();
                db.removeNode(title);
                nodes = db.getData();
                Toast.makeText(context, "Node removed from map and from DB", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        db.close();
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    public static void updateOwnLocation(String latitude, String longitude){
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        LatLng latLng = new LatLng(lat, lon);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
    //Metodin idea on lukea db.getData():lla tuoreimmat arvot ja verrata aiempiin onMapReadyssä() luettuihin
    //vertaa arvojen määrä nodes listoissa, jos isompi tässä, lue viimeiset ja paiskaa / korvaa (jos update) kartalle, päivitä nodes seuraavaan lukuun
    //tällä hetkellä metodia kutsutaan locationfetchin onlocationchangessa, eli aina ku gps arvot muuttuu
    //siksi, että samalla voidaan passata gps koordinaatit tänne jotta kartta seuraa
    //vois tieten olla viisaampaa tehdä databasehandlerissa...
    public static void addNewNode(){
        DatabaseHandler db = new DatabaseHandler(MapsMarkerActivity.context);
        List<String[]> nodes2;
        nodes2 = db.getData();
        int koko;
        //jossei haettu ole tyhjä
        if (nodes2 != null) {

            //hax jos vanha taulu on tyhja --> oletuskoko
            if (nodes == null) {
                koko = 0;
                Log.d("JALAJALA", "NODES NULL --> 0");
            }
            else {
                koko = nodes.size();
                Log.d("JALAJALA", "NODES : NODES2 SIZE: " + koko + " : " + nodes2.size());
            }
            //jos tauluun lisätty nodeja, päivitetään map (vain näillä nodeilla)
            if(nodes2.size() > koko) {

                //aloitetaan uuden taulun kahlaus indeksistä vanhan taulun koko ja jatketaan taulun loppuun
                for (int apu = koko; apu < nodes2.size(); apu++) {
                    Log.d("JALAJALA", "ADD_NEW_NODE, DB ROW: " + apu);

                    String[] node2 = nodes2.get(apu);
                    String title = node2[0];
                    locNN = Double.parseDouble(node2[1]);
                    locEE = Double.parseDouble(node2[2]);
                    String address = node2[3];

                    Log.d("JALAJALA", String.valueOf(node2[3]));

                    //create marker from values and add to map
                    LatLng node1 = new LatLng(locNN, locEE);
                    Marker marker;
                    marker = map.addMarker(new MarkerOptions()
                            .position(node1)
                            .snippet(address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title(title));

                    //add marker to arraylist markers so it can be deleted later
                    markers.add(marker);
                }
             }
            nodes = nodes2;
        }
    }

    //Create list of map marker objects and add object to list every time marker is created.
    //scan through list to remove specific node instead of clearing all of them.

    //UUSI YRITYS: LUETAAN LISÄTYT MARKERIT ARRAYLSITASTA markers
    //poistetaan yksittäinen markkeri kartasta (jonka sijaintia on "parannettu") ja lisätään uusi markkeri
    public static void updateLocation(String id, String nloc, String eloc, String address) {
        Log.d("JALAJALA", "UPDATE_NODE_LOCATION_ON_MAP");

        //for (Marker mark : markers) {
        for (Marker mark : new ArrayList<Marker>(markers)) {
            //Log.d("JALAJALA", "MARK_TITLE / id = " + mark.getTitle() + " : " + id);
            String title = mark.getTitle();
            if (title.equals(id)) {
                mark.remove();
                markers.remove(mark);
                Log.d("JALAJALA", "MARK REMOVED");
            }
            else{
            }
        }
        String title = id;
        locNN = Double.parseDouble(nloc);
        locEE = Double.parseDouble(eloc);

        //create marker from values and add to map
        LatLng node1 = new LatLng(locNN, locEE);
        Marker marker;
        marker = map.addMarker(new MarkerOptions()
                .position(node1)
                .snippet(address)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(title));

        //add marker to arraylist markers so it can be deleted later
        markers.add(marker);


    //taulussa sama määrä nodeja, tarkastesetaan onko sijaintia korjattu ja päivitetään map
        //public static void updateLocation() {
        /*Log.d("JALAJALA", "UPDATE_NODE_LOCATION_ON_MAP");
        map.clear();
        DatabaseHandler db = new DatabaseHandler(MapsMarkerActivity.context);
        List<String[]> nodes3;
        nodes3 = db.getData();
        String[] node3;

        for (int i=0; i<nodes3.size(); i++){
        //for (String[] node3 : nodes3) {
            //for (String[] node : nodes) {
            if (nodes3.get(i) != nodes.get(i)) {
                node3 = nodes3.get((i));
                String title = node3[0];
                locNN = Double.parseDouble(node3[1]);
                locEE = Double.parseDouble(node3[2]);
                String address = node3[3];

                LatLng merkki3 = new LatLng(locNN, locEE);
                map.addMarker(new MarkerOptions()
                        .position(merkki3)
                        .snippet(address)
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                //map.moveCamera(CameraUpdateFactory.newLatLng(merkki3));
            }
        }
        nodes = nodes3;*/
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}