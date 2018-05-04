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

import static koti.blescanapi21.MapsMarkerActivity.context;


/**
 * Created by Proot on 3.5.2018.
 */



//public class PahoSubscribe extends AsyncTask<String, String, MqttMessage> implements MqttCallback {
public class PahoSubscribe extends Service implements MqttCallback {

    //private String connAddress = "tcp://tutkasema.mine.nu:1883";
    private String connAddress = "tcp://81.175.134.233:1883";
    //private String connAddress = "tcp://192.168.0.113:1883";
    //private String connAddress = "tcp://test.mosquitto.org:1883";
    //private static String connAddress = "tcp://iot.eclipse.org:1883";

    //private MqttAsyncClient sub_client;
    private MqttClient sub_client;
    private Context context = MapsMarkerActivity.context;
    private MqttMessage message;
    private int i = 0;

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
            i++;
            //sub_client.connect(mqttConnectOptions).waitForCompletion();
            sub_client.connect(mqttConnectOptions);

            //subscribe
            Log.d("JALAJALA", "PAHO_SUBSCRIBE");
            int subQos = 1;
            sub_client.subscribe("zombihomman/testicase", subQos);
            sub_client.setCallback(this);
        }
        catch (MqttException e){
            e.printStackTrace();
            Log.d("JALAJALA", String.valueOf(e.getCause()));
            Log.d("JALAJALA", String.valueOf(e.getMessage()));
        }
    }

    @Override
    public void onCreate(){
        getData();
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

        //Log.d("JALAJALA", "PAHO_MESSAGE: " + message);

        //send here message back to databasehandler insertSubscribedData(String.valueOf(message))
        //to be parsed and inserted to db if not already there (subscribed from another device).

        String payload = new String(message.getPayload());

/*      testing for map problems with subnodes
        String[] values = payload.split(",");

        String address = values[0].replaceAll("\\s+","");
        String rssi = values[1].replaceAll("\\s+","");
        String nloc = values[2].replaceAll("\\s+","");
        String eloc = values[3].replaceAll("\\s+","");
        String user = values[4].replaceAll("\\s+","");
        String id = "1001";

        //MapsMarkerActivity.addNewSubNode(id, address, rssi, nloc, eloc, user);

        //MOAR TESTING
        Intent intent = new Intent("PAHOMESSAGE");
        //intent.putExtra("NLOC", String.valueOf(location.getLatitude()));
        //intent.putExtra("ELOC", String.valueOf(location.getLongitude()));
        intent.putExtra("ID", id);
        intent.putExtra("ADDRESS", address);
        intent.putExtra("RSSI", rssi);
        intent.putExtra("NLOC",nloc );
        intent.putExtra("ELOC", eloc);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
*/
        //DatabaseHandler db = new DatabaseHandler(this);
        //db.insertSubscribedData(String.valueOf(payload));
        //this.message = message;
        //sub_client.close();

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
