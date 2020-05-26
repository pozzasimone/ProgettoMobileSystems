package org.tensorflow.lite.examples.classification.mqtt;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

public class Subscriber extends Thread implements MqttCallback {
    private static final String TAG = "Subscriber";
    private String topic;
    private MqttAndroidClient mqttAndroidClient;
    private String imagesFolder;
    private Handler mainThreadHandler;

    Subscriber(Handler handler) {
        this.mainThreadHandler = handler;
        // lettura di indirizzo e porta del server dai metadati del manifest
        ApplicationInfo ai = null;
        try {
            ai = MainActivity.getAppContext().getPackageManager().getApplicationInfo(MainActivity.getAppContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String brokerAddress = (String) ai.metaData.get("BROKER_ADDRESS");
        int brokerPort = (int) ai.metaData.get("BROKER_PORT");
        String brokerURI = "tcp://" + brokerAddress + ":" + brokerPort;

        this.mqttAndroidClient = new MqttAndroidClient(MainActivity.getAppContext(), brokerURI, TAG, new MemoryPersistence());
        this.pickNewRandomTopic();
        this.imagesFolder = Environment.getExternalStorageDirectory().toString() + File.separator + "MobileSystemsM";
    }

    @Override
    public void run() {
        try {
            mqttAndroidClient.setCallback(this);
            // connessione al broker
            do {
                SystemClock.sleep(300);
                mqttAndroidClient.connect();
            }
            while (!mqttAndroidClient.isConnected() && Log.i(TAG, "Falied to connect to MQTT Broker - attempting reconnection") != 0);
            Log.i(TAG, "Connected successfully  to MQTT Broker");

            this.subscribeToTopic();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(this.topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    Log.i(TAG, "Subscribed to topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    Log.i(TAG, "Failed to subscribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Message message = Message.obtain(this.mainThreadHandler, 99, this.topic);
        mainThreadHandler.sendMessage(message);
    }

    private void pickNewRandomTopic() {
        String[] demoTopics = {"mouse", "computer keyboard", "monitor"};
        int index;
        do {
            index = new Random().nextInt(demoTopics.length); // restituisce un intero fra 0 e length-1
        }
        while(demoTopics[index].equals(this.topic));
        this.topic = demoTopics[index];
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.i(TAG, "Connection lost.");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG, "Recevied image for topic " + this.topic);
        mqttAndroidClient.unsubscribe(this.topic);

        //conversione byte[]>bitamp
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(message.getPayload(), 0, message.getPayload().length, options);

        // salvataggio immagine su disco
        String imageName = topic + ".png";
        File imageFile = new File(imagesFolder, imageName);
        OutputStream outputStream = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
        outputStream.close();
        Log.i(TAG, "Saved image " + imageName);

        // nuova sottoscrizione
        this.pickNewRandomTopic();
        this.subscribeToTopic();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "Delivery complete");
    }
}
