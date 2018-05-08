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
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;
import java.util.Date;


public class PahoClient extends Service implements MqttCallback{

    private Context context = MapsMarkerActivity.context;
    private static MqttClient client;
    //private MqttClient sub_client;
    private MqttAsyncClient sub_client;
    private String nodeData = "testi," + "data," + "paketti," + "kannasta";

    //private String connAddress = "tcp://tutkasema.mine.nu:1883";
    private static String connAddress = "tcp://81.175.134.233:1883";
    //private String connAddress = "tcp://192.168.0.113:1883";
    //private String connAddress = "tcp://test.mosquitto.org:1883";
    //private static String connAddress = "tcp://iot.eclipse.org:1883";

    int clientid = 0;

    public PahoClient(){

    }

    public int onStartCommand(Intent intent, int flags, int startID){
        super.onStartCommand(intent, flags, startID);

        return START_STICKY;
    }

    public void onCreate() {

        Log.d("JALAJALA", "PAHO_ON_CREATE");
        //getData();
        //sendData(nodeData);
    }

    public void onDestroy(){
        try {
            client.close();
            //sub_client.close();
        }
        catch (MqttException e) {
            e.printStackTrace();
            Log.d("JALAJALA", String.valueOf(e.getCause()));
            Log.d("JALAJALA", String.valueOf(e.getMessage()));
        }
        super.onDestroy();
    }


    public static void sendData(String testi) {
        try {
            MemoryPersistence persistence = new MemoryPersistence();

            //maybe need to upgrade the clientid for new publishes
            //client = new MqttClient(connAddress, "zombihommanpub", persistence);
            client = new MqttClient(connAddress, MqttClient.generateClientId(), persistence);
            client.connect();


             //publish
            Log.d("JALAJALA", "PAHO_PUBLISH: " + testi );
            MqttMessage message = new MqttMessage();
            message.setPayload(testi.getBytes());

            client.publish("zombihomman/testicase", message);
            client.disconnect();

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
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        //Log.d("JALAJALA", "PAHO_MESSAGE: " + message);

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

