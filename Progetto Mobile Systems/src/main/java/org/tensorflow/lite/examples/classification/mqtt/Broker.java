package org.tensorflow.lite.examples.classification.mqtt;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import io.moquette.BrokerConstants;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;

public class Broker extends Thread {

    private static final String TAG = "Broker";

    @Override
    public void run() {

        // lettura di indirizzo e porta del server dai metadati del manifest
        ApplicationInfo ai = null;
        try {
            ai = MainActivity.getAppContext().getPackageManager().getApplicationInfo(MainActivity.getAppContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String brokerAddress = (String) ai.metaData.get("BROKER_ADDRESS");
        int brokerPort = (int) ai.metaData.get("BROKER_PORT");

        // configurazione delle proprietà del broker e avvio del server relativo
        Properties properties = new Properties();
        properties.setProperty(BrokerConstants.HOST_PROPERTY_NAME, brokerAddress);
        properties.setProperty(BrokerConstants.PORT_PROPERTY_NAME, String.valueOf(brokerPort));
        properties.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME);
        MemoryConfig memoryConfig = new MemoryConfig(properties);
        Server mqttBroker = new Server();
        try {
            mqttBroker.startServer(memoryConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Server Started");
    }
}