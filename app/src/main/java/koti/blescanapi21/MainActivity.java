package koti.blescanapi21;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager locationManager;

    private Handler mHandler = new Handler();
    private boolean mScanning = true;

    private final static long SCAN_PERIOD = 10000;
    private final static int REQUEST_ENABLE_BT = 1;

    String deviceAddress;
    TextView teksti;
    int rssi;

    LocationFetch locationFetch;
    private String locN;
    private String locE;

    final boolean enable = true;

    //receiver for LocationFetch location broadcasts
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("JALAJALA: ", "RECEIVER CHECK");
            locN = intent.getStringExtra("NLOC");
            locE = intent.getStringExtra("ELOC");
        }
    };

    //replies from scan
    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //Log.d("JALAJALA: ", result.toString());
            Log.d("JALAJALA: ", "ONSCANRESULT");
            BluetoothDevice bluetoothDevice = result.getDevice();
            ScanRecord sr = result.getScanRecord();
            rssi = result.getRssi();

            deviceAddress = bluetoothDevice.getAddress();
            String deviceName = sr.getDeviceName();


            if (deviceName != null) {
                Log.d("JALAJALA", deviceName);
            }
            if (rssi != 0) {
                Log.d("JALAJALA", String.valueOf(rssi));
            }
                if (deviceAddress != null) {
                asetaTeksti(deviceAddress, rssi);
            }
            //Log.d("JALAJALA", deviceName);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d("JALAJALA: ", "ONBATCHRESULTS");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("JALAJALA: ", "ONSSCANFAILED");
        }
    };

    //method to set MAC and location to textview
    public void asetaTeksti(String deviceAddress, int rssi) {
        Log.d("JALAJALA ", deviceAddress);
        teksti = (TextView)findViewById(R.id.textView);

        if (teksti == null) {
            teksti.setText("MAC: " + deviceAddress + "\t" + rssi + "\nN: " + locN + "\nE: " + locE + "\n\n");
        }
        else {
            teksti.setText(teksti.getText(), TextView.BufferType.EDITABLE);
            ((Editable) teksti.getText()).insert(0, "MAC: " + deviceAddress + "\t\t RSSI: " + rssi + "\nN: " + locN + "\nE: " + locE + "\n\n");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        //basic
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Broadcastlisterner
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("JALAEVENTNAME"));

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //if bt not enabled, ask to enable it
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtintent, REQUEST_ENABLE_BT);
        }

        //If there is no rights for location:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding-
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.'

            Log.d("JALAJALA: ","LOC OIKEUS_CHECK");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
        else {
            Log.d("JALAJALA: ", "LOC OIKEUDET_ON");
            startService(new Intent(this, LocationFetch.class));


            //scanLeDevice(enable);
        }

        Button cameraButton = (Button) findViewById(R.id.button);
        cameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    scanLeDevice(enable);

            }
        });
    }

    //scanner for BLE nodes, takes boolen true to start scanning. SCAN_PERIOD is cycle for one scan
    private void scanLeDevice(final boolean enable) {

        final BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;

                    bluetoothLeScanner.stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothLeScanner.startScan(mLeScanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(mLeScanCallback);
        }
    }




}