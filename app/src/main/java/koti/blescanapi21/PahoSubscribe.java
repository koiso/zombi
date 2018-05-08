package koti.blescanapi21;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static koti.blescanapi21.MapsMarkerActivity.context;


/**
 * Created by Proot on 3.5.2018.
 */



//public class PahoSubscribe extends AsyncTask<String, String, MqttMessage> implements MqttCallback {
public class PahoSubscribe extends Service implements MqttCallback {

    //private String connAddress;
    //private String connAddress = "tcp://tutkasema.mine.nu:1883";
    private String connAddress = "tcp://81.175.134.233:1883";
    //private String connAddress = "tcp://192.168.0.113:1883";
    //private String connAddress = "tcp://test.mosquitto.org:1883";
    //private static String connAddress = "tcp://iot.eclipse.org:1883";

    //private MqttAsyncClient sub_client;
    private MqttClient sub_client;
    private Context context = MapsMarkerActivity.context;
    private MqttMessage message;


    @Override
    public void onCreate(){
        getData();
    }

    public void getData(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setKeepAliveInterval(60*5);
        //or false
        mqttConnectOptions.setCleanSession(true);

        MemoryPersistence persistence = new MemoryPersistence();
        try {
            Log.d("JALAJALA", "SUB_GET_DATA");
            sub_client = new MqttClient(connAddress, "zombihommansub", persistence);
            //sub_client = new MqttAsyncClient(connAddress, "zombihommansub", persistence);
            //sub_client = new MqttAsyncClient(connAddress, String.valueOf(i), persistence);
            //sub_client.connect(mqttConnectOptions).waitForCompletion();
            sub_client.connect(mqttConnectOptions);

            //subscribe
            Log.d("JALAJALA", "PAHO_SUBSCRIBE");
            int subQos = 1;
            sub_client.subscribe("zombihomman/testicase", subQos);
            sub_client.setCallback(this);
        }
        catch (MqttException e){
            Log.d("JALAJALA", String.valueOf(e.getCause()));
            Log.d("JALAJALA", String.valueOf(e.getMessage()));

        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        super.onStartCommand(intent, flags, startID);
        return START_STICKY;
    }


    /*
    @Override
    protected MqttMessage doInBackground(String... strings) {
        getData();
        return message;
    }
*/
    @Override
    public void connectionLost(Throwable cause) {
        Log.d("JALAJALA", "SUB_CONNECTION_LOST" + String.valueOf(cause));
        getData();
    }

    @Override
    public void messageArrived(String topic, final MqttMessage message) throws Exception {
           Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {

                    //Toast.makeText(context, "MQTT Message:\n" +new String(message.getPayload()), Toast.LENGTH_SHORT).show();
                    String payload = new String(message.getPayload());
                    DatabaseHandler db = new DatabaseHandler(context);
                    db.insertSubscribedData(String.valueOf(payload));
                }
            });
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    /*
    @Override
    protected void onPostExecute(MqttMessage message){
        Log.d("JALAJALA", "PAHOSUB ON POST");
        super.onPostExecute(message);
        getData();

    }
*/

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
