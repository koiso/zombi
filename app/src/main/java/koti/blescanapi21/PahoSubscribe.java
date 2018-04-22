package koti.blescanapi21;

/**
 * Created by Proot on 22.4.2018.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PahoSubscribe extends Service implements MqttCallback{

    private Context context = MapsMarkerActivity.context;
    MqttClient client;

    public void onCreate(){
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

    public int onStartCommand(Intent intent, int flags, int startID){
        super.onStartCommand(intent, flags, startID);
        return START_STICKY;
    }

    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message)
            throws Exception {
        //Toast.makeText(this, String.valueOf(message), Toast.LENGTH_SHORT).show();
        Log.d("JALAJALA", "PAHO_MESSAGE: " + message);

        Intent intent = new Intent("PAHO_MESSAGE");
        //intent.putExtra("NLOC", String.valueOf(location.getLatitude()));
        //intent.putExtra("ELOC", String.valueOf(location.getLongitude()));
        intent.putExtra("MESSAGE", String.valueOf(message));
        
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
