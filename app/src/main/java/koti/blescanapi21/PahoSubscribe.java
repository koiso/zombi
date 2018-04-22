package koti.blescanapi21;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by mikes on 22.4.2018.
 */

public class PahoSubscribe extends Service implements MqttCallback {

    private Context context = MapsMarkerActivity.context;
    MqttClient client;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate(){
        Log.d("JALAJALA", "PahoSubscribe onCreate");
        try {
            Log.d("JALAJALA", "PAHO_SUBSCRIBE");
            MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient("tcp://iot.eclipse.org:1883", "pahomqttpublish1", persistence);
            client.connect();

            //subscribe
            client.setCallback(this);
            client.subscribe("pahodemo/test");
        }
        catch (MqttException e) {
            e.printStackTrace();
            Log.d("JALAJALA", String.valueOf(e.getCause()));
            Log.d("JALAJALA", String.valueOf(e.getMessage()));
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("JALAJALA", "PahoSubscribe onStartCommand");

        return START_STICKY;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d("JALAJALA", "connectionLost" );
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d("JALAJALA", "PAHO_MESSAGE: " + message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d("JALAJALA", "deliveryComplete" );
    }
}
