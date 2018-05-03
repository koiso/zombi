package koti.blescanapi21;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * Created by Proot on 3.5.2018.
 */



public class PahoSubscribe extends AsyncTask<String, String, MqttMessage> implements MqttCallback {

    //private String connAddress = "tcp://tutkasema.mine.nu:1883";
    private String connAddress = "tcp://81.175.134.233:1883";
    //private String connAddress = "tcp://192.168.0.113:1883";
    //private String connAddress = "tcp://test.mosquitto.org:1883";
    //private static String connAddress = "tcp://iot.eclipse.org:1883";

    private MqttAsyncClient sub_client;
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
            //sub_client = new MqttClient(connAddress, "zombihommansub", persistence);
            sub_client = new MqttAsyncClient(connAddress, "zombihommansub", persistence);
            //sub_client = new MqttAsyncClient(connAddress, String.valueOf(i), persistence);
            i++;
            sub_client.connect(mqttConnectOptions).waitForCompletion();

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
    protected MqttMessage doInBackground(String... strings) {
        getData();
        return message;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d("JALAJALA", "SUB_CONNECTION_LOST");
        getData();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //Log.d("JALAJALA", "PAHO_MESSAGE: " + message);

        //send here message back to databasehandler insertSubscribedData(String.valueOf(message))
        //to be parsed and inserted to db if not already there (subscribed from another device).

        String payload = new String(message.getPayload());

        /*testing for map problems with subnodes
        String[] values = payload.split(",");

        String address = values[0].replaceAll("\\s+","");
        String rssi = values[1].replaceAll("\\s+","");
        String nloc = values[2].replaceAll("\\s+","");
        String eloc = values[3].replaceAll("\\s+","");
        String user = values[4].replaceAll("\\s+","");
        String m_id = "1001";

        MapsMarkerActivity.addNewSubNode(m_id, address, rssi, nloc, eloc, user);
        */

        DatabaseHandler db = new DatabaseHandler(context);
        db.insertSubscribedData(String.valueOf(payload));
        this.message = message;
        //sub_client.close();

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    protected void onPostExecute(MqttMessage message){
        Log.d("JALAJALA", "PAHOSUB ON POST");
        super.onPostExecute(message);
        getData();

    }
}
