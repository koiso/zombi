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
    public static Context context;
    private static List<String[]> nodes;
    private static String[] node;
    private static int koko;
    private static int markersize = 0;
    private static List<Marker> markers = new ArrayList<>();

    //paho testing
    //PahoClient paho;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Intent intententti = getIntent();
        //locN = intententti.getStringExtra("locN");
        //locE = intententti.getStringExtra("locE");
        MapsMarkerActivity.context = getApplicationContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        //KOSKA JUSSI KÄSKI
        Intent checkPermissions = new Intent(this, Permissions.class);
        startActivity(checkPermissions);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //for MQTT messages in
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("PAHOMESSAGE"));

        startService(new Intent(this, LocationFetch.class));
        startService(new Intent(this, BleScanner.class));
        startService(new Intent(this, PahoClient.class));
        startService(new Intent(this, PahoSubscribe.class));


        //testing for paho in asynctask
        //PahoSubscribe pahoSubscribe = new PahoSubscribe();
        //pahoSubscribe.execute();

    }

    //mikä tämä on ja miksi, tarvitaanko vielä
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String r_id = intent.getStringExtra("ID");
            String r_address = intent.getStringExtra("ADDRESS");
            String r_nloc = intent.getStringExtra("NLOC");
            String r_eloc = intent.getStringExtra("ELOC");
            Log.d("PAHO_MESSAGE_MAP: ", r_id + r_address + r_nloc + r_eloc);
        }
    };

    protected void onPause(){
        super.onPause();
    }
    protected void onStop(){
        super.onStop();
    }
    protected void onDestroy(){
        super.onDestroy();
    }
    protected void onResume() {
        super.onResume();
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
        map.animateCamera(CameraUpdateFactory.zoomTo(16));
    }


    public static void addNewNode(String m_id, String address, String rssi, String nloc, String eloc, String user){
        Log.d("JALAJALA", "MAP ADD NEW NODE:" + m_id + ", " + address + ", " + rssi + ", " + nloc + ", " + eloc + ", " + user);
        Toast.makeText(context, "ADDED NEW NODE: " + address, Toast.LENGTH_SHORT).show();
        locNN = Double.parseDouble(nloc);
        locEE = Double.parseDouble(eloc);

        String title = m_id;
        //create marker from values and add to map
        LatLng node1 = new LatLng(locNN, locEE);
        Marker marker;
        marker = map.addMarker(new MarkerOptions()
                .position(node1)
                .snippet(address)
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(title));

        //add marker to arraylist markers so it can be deleted later
        markers.add(marker);
    }


    //add new node fetched from MQTT broker
    public static void addNewSubNode(String m_id, String address, String rssi, String nloc, String eloc, String user){
        Log.d("JALAJALA", "MAP_ADD_NEW_SUBNODE:" + m_id + ", " + address + ", " + rssi + ", " + nloc + ", " + eloc + ", " + user);
        Toast.makeText(context, "ADDED NEW SUBNODE: " + address, Toast.LENGTH_SHORT).show();

        locNN = Double.parseDouble(nloc);
        locEE = Double.parseDouble(eloc);

        //testataan eikö vanhan markin vuoksi voi piirtä subnodea kun tulee samalta laitteela (luulatavasti turhaa)
        for (Marker mark : new ArrayList<Marker>(markers)) {
            //Log.d("JALAJALA", "MARK_TITLE / id = " + mark.getTitle() + " : " + id);
            String title = mark.getTitle();
            if (title.equals(m_id)) {
                mark.remove();
                markers.remove(mark);
                Log.d("JALAJALA", "MARK REMOVED FOR SUB");
            } else {
            }
        }

        String title = m_id;
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
            } else {
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
    }


    //update node location fetched from MQTT broker
    public static void updateSubLocation(String id, String nloc, String eloc, String address) {
        Log.d("JALAJALA", "UPDATE_SUB_LOCATION_ON_MAP");

        //for (Marker mark : markers) {
        for (Marker mark : new ArrayList<Marker>(markers)) {
            //Log.d("JALAJALA", "MARK_TITLE / id = " + mark.getTitle() + " : " + id);
            String title = mark.getTitle();
            if (title.equals(id)) {
                mark.remove();
                markers.remove(mark);
                Log.d("JALAJALA", "MARK REMOVED");
            } else {
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(title));

        //add marker to arraylist markers so it can be deleted later
        markers.add(marker);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}