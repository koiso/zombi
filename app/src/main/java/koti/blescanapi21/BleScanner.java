package koti.blescanapi21;

import android.Manifest;
import android.app.Service;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class BleScanner extends Service {

    private Handler mHandler = new Handler();
    private boolean mScanning = true;

    private String deviceAddress;
    private TextView teksti;
    private int rssi;

    private String locN;
    private String locE;

    final boolean enable = true;
    private final static long SCAN_PERIOD = 1000000;
    private BluetoothAdapter mBluetoothAdapter;

    public BleScanner() {
    }

    public int onStartCommand(Intent intent, int flags, int startID) {
        Log.d("JALAJALA", "BLESCAN_ONSTARTCOMMAND");
        //Broadcastlisterner
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("JALAEVENTNAME"));
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        super.onStartCommand(intent, flags, startID);
        scanLeDevice(enable);
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    //receiver for LocationFetch location broadcasts
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("JALAJALA: ", "RECEIVER CHECK");

            locN = intent.getStringExtra("NLOC");
            locE = intent.getStringExtra("ELOC");

            Log.d("JALAJALA: ", locN);
            Log.d("JALAJALA: ", locE);
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

            //additional data from nodes for something
            String deviceName = sr.getDeviceName();

            /*
            SparseArray<byte[]> manu = sr.getManufacturerSpecificData();

            if (manu != null) {
                int size = manu.size();
                for (int i = 0; i < size; i++) {

                    int key = manu.keyAt(i);
                    byte[] value = manu.valueAt(i);

                    String content = "";
                    try {
                        content = new String(value, "ISO-8859-15");
                    }
                    catch(UnsupportedEncodingException e){
                    }
                    Log.i("TAG", "key: " + key + " value: " + content);
                }
            }
            */
            
            if (deviceName != null) {
                Log.d("JALAJALA", deviceName);
            }
            if (rssi != 0) {
                Log.d("JALAJALA", String.valueOf(rssi));
            }
            if (deviceAddress != null) {
                Log.d("JALAJALA", String.valueOf(deviceAddress));
                insertToDb(deviceAddress, rssi);
            }
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
    public void insertToDb(String deviceAddress, int rssi) {

        //insert to database
        DatabaseHandler db = new DatabaseHandler(this);
        if (deviceAddress != null && locN != null && locE != null) {
            db.addNode(deviceAddress, String.valueOf(rssi), String.valueOf(locN), String.valueOf(locE), "user");
        }
        // db.updateNode("address", "rssi2", "nloc2", "user2");
        //Log.d("JALAJALA ", deviceAddress);

        /*OLD MAIN PRINTS
        teksti = (TextView)findViewById(R.id.textView);

        if (teksti == null) {
            teksti.setText("MAC: " + deviceAddress + "\t" + rssi + "\nN: " + locN + "\nE: " + locE + "\n\n");
        }
        else {
            teksti.setText(teksti.getText(), TextView.BufferType.EDITABLE);
            ((Editable) teksti.getText()).insert(0, "MAC: " + deviceAddress + "\t\t RSSI: " + rssi + "\nN: " + locN + "\nE: " + locE + "\n\n");
        }*/
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
/*
    public void startMap(View view) {
        Intent intententti = new Intent(this, MapsMarkerActivity.class);
        //Log.d("JALAJALACALL", String.valueOf(locN));
        //Log.d("JALAJALACALL", String.valueOf(locE));
        //intententti.putExtra("locN", locN);
        //intententti.putExtra("locE", locE);
        startActivity(intententti);
    }
*/

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }
}
