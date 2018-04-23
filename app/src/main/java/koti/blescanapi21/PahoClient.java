package koti.blescanapi21;

/**
 * Created by Proot on 22.4.2018.
 */

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;


public class PahoClient extends Service implements MqttCallback{

    private Context context = MapsMarkerActivity.context;
    MqttClient client;
    MqttClient sub_client;
    String nodeData = "testi," + "data," + "paketti," + "kannasta";

    //private String connAddress = "tcp://tutkasema.mine.nu:1883";
    private String connAddress = "tcp://81.175.134.233:1883";
    //private String connAddress = "tcp://192.168.0.113:1883";
    //private String connAddress = "tcp://test.mosquitto.org:1883";

    public PahoClient(){

    }

    public int onStartCommand(Intent intent, int flags, int startID){
        super.onStartCommand(intent, flags, startID);

        return START_STICKY;
    }

    public void onCreate() {
        Log.d("JALAJALA", "PAHO_ON_CREATE");
/*
        try {
            Log.d("JALAJALA", "PAHO_CREATE_CLIENT");
            MemoryPersistence persistence = new MemoryPersistence();
            //client = new MqttClient("tcp://iot.eclipse.org:1883", "zombihommanid", persistence);
            client = new MqttClient(connAddress, "zombihommanid2", persistence);
            client.connect();

            //client.disconnect();
        }
        catch (MqttException e) {
            e.printStackTrace();
            Log.d("JALAJALA", String.valueOf(e.getCause()));
            Log.d("JALAJALA", String.valueOf(e.getMessage()));
        }*/
        getData();
        sendData(nodeData);
    }

    public void onDestroy(){
        try {
            client.close();
        }
        catch (MqttException e) {
            e.printStackTrace();
            Log.d("JALAJALA", String.valueOf(e.getCause()));
            Log.d("JALAJALA", String.valueOf(e.getMessage()));
        }
    }

    public void getData(){
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            sub_client = new MqttClient(connAddress, "zombihommansub", persistence);
            sub_client.connect();

            //subscribe
            Log.d("JALAJALA", "PAHO_SUBSCRIBE");
            sub_client.setCallback(this);
            sub_client.subscribe("zombihomman/testicase");

        }
        catch (MqttException e){
            e.printStackTrace();
            Log.d("JALAJALA", String.valueOf(e.getCause()));
            Log.d("JALAJALA", String.valueOf(e.getMessage()));
        }
    }

    public void sendData(String testi) {
        try {
            //Log.d("JALAJALA", "PAHO_CREATE_CLIENT");
            MemoryPersistence persistence = new MemoryPersistence();
            //client = new MqttClient("tcp://iot.eclipse.org:1883", "zombihommanid", persistence);
            client = new MqttClient(connAddress, "zombihommanpub", persistence);
            client.connect();

            //subscribe
            //Log.d("JALAJALA", "PAHO_SUBSCRIBE");
            //client.setCallback(this);
            //client.subscribe("zombihomman/testicase");

             //publish
            Log.d("JALAJALA", "PAHO_PUBLISH");
            MqttMessage message = new MqttMessage();
            //message.setPayload("A single message".getBytes());
            //message.setPayload(nodeData.getBytes());
            message.setPayload(testi.getBytes());

            client.publish("zombihomman/testicase", message);

            //client.disconnect();

        }
        catch (MqttException e) {
            e.printStackTrace();
            Log.d("JALAJALA", String.valueOf(e.getCause()));
            Log.d("JALAJALA", String.valueOf(e.getMessage()));
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        //Toast.makeText(context, String.valueOf(message), Toast.LENGTH_SHORT).show();
        Log.d("JALAJALA", "PAHO_MESSAGE: " + message);

        //send here message back to databasehandler insertSubscribedData(String.valueOf(message))
        //to be parsed and inserted to db if not already there (subscribed from another device).
        //DatabaseHandler db = new DatabaseHandler(this);
        //db.insertSubscribedData(String.valueOf(message));

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }
}

