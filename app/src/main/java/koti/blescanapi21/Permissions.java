package koti.blescanapi21;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.widget.Toast;



public class Permissions extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Context context = MapsMarkerActivity.context;

    private static final int REQUEST_BLUETOOTH = 0;
    private static final int REQUEST_FINE_LOCATION = 1;
    private static final int REQUEST_INTERNET = 2;
    private static final int REQUEST_WRITE = 3;

    private boolean locationPermissionGranted = false;
    private boolean bluetoothPermissionGranted = false;
    private boolean internetPermissionGranted = false;

    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Marshmallow
            Toast.makeText(this , "checking runtime permissions..", Toast.LENGTH_SHORT).show();
            LocationPermission();
            WritePermission();
            BluetoothPermission();
            //InternetPermission();

        }

        else{
            locationPermissionGranted = true;
            bluetoothPermissionGranted = true;
            internetPermissionGranted = true;
            Toast.makeText(this , "permissions ok", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void WritePermission() {
        Toast.makeText(this , "checking write permissions..", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
            }
        }

        else{
            Toast.makeText(this , "write enabled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void LocationPermission(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent enableGpsintent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(enableGpsintent);
        }

        Toast.makeText(this , "checking location permissions..", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            }

        }

        else{
            Toast.makeText(this , "location enabled", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void BluetoothPermission(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtintent, REQUEST_BLUETOOTH);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH);
            }

            else{
                Toast.makeText(this , "BT enabled", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void InternetPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.INTERNET}, REQUEST_INTERNET);
            }

            else{
                Toast.makeText(this , "Internet enabled", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){

            case REQUEST_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    locationPermissionGranted = true;
                    finish();
                    break;
                }
                else{
                    locationPermissionGranted = false;
                    Toast.makeText(this , "service doesn't work without location", Toast.LENGTH_SHORT).show();
                    break;
                }

            case REQUEST_BLUETOOTH:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    bluetoothPermissionGranted = true;
                }
                else{
                    bluetoothPermissionGranted = false;
                    Toast.makeText(this , "service doesn't work without BT", Toast.LENGTH_SHORT).show();
                    finishAndRemoveTask();
                }
                break;

            case REQUEST_INTERNET:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    internetPermissionGranted = true;
                }
                else{
                    internetPermissionGranted = false;
                    Toast.makeText(this , "service doesn't work without internet access", Toast.LENGTH_SHORT).show();
                    finishAndRemoveTask();
                }
                break;
        }
    }


    protected void onStop() {
        super.onStop();
    }
}
