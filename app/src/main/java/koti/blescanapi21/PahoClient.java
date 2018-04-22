package koti.blescanapi21;

/**
 * Created by Proot on 22.4.2018.
 */

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class PahoClient extends Service implements MqttCallback{

    MqttClient client;


    public PahoClient() {

    }

    public void onCreate() {
        Log.d("JALAJALA", "PAHO_ON_CREATE");
        doDemo();
    }

    public void doDemo() {
        try {
            Log.d("JALAJALA", "PAHO_DO_DEMO");
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient("tcp://iot.eclipse.org:1883", "pahomqttpublish1", persistence);
            client.connect();

            //subscribe
            client.setCallback(this);
            client.subscribe("pahodemo/test");

            //publish
            MqttMessage message = new MqttMessage();
            message.setPayload("A single message".getBytes());
            client.publish("pahodemo/test", message);

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
        Log.d("JALAJALA", "PAHO_MESSAGE: " + message);
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

