package com.aware.plugin.device_id_manager;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {

    public static final String TAG = "AWARE::DeviceIDManager";

    @Override
    public void onCreate() {
        super.onCreate();

        // Define the broadcasts that this plugin will send
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                // Not needed for this plugin
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (PERMISSIONS_OK) {
            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            // Initialize plugin
            Aware.setSetting(this, Settings.STATUS_PLUGIN_DEVICE_ID_MANAGER, true);

            if (Aware.DEBUG) Log.d(TAG, "Device ID Manager plugin started");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Aware.setSetting(this, Settings.STATUS_PLUGIN_DEVICE_ID_MANAGER, false);

        if (Aware.DEBUG) Log.d(TAG, "Device ID Manager plugin terminated");
    }
}