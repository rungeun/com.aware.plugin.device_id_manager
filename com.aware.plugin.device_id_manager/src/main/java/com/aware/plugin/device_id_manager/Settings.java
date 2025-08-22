package com.aware.plugin.device_id_manager;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.providers.Aware_Provider;
import java.util.UUID;
import java.util.regex.Pattern;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String STATUS_PLUGIN_DEVICE_ID_MANAGER = "status_plugin_device_id_manager";
    public static final String CUSTOM_DEVICE_ID = "custom_device_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_device_id_manager);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        // Set current device ID as hint
        final EditTextPreference deviceIdPref = (EditTextPreference) findPreference(CUSTOM_DEVICE_ID);
        String currentDeviceId = Aware.getSetting(this, Aware_Preferences.DEVICE_ID);
        deviceIdPref.setSummary("Current: " + currentDeviceId);
        deviceIdPref.setDefaultValue(currentDeviceId);

        // Add validation
        deviceIdPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newId = newValue.toString().trim();

                if (newId.isEmpty()) {
                    Toast.makeText(Settings.this, "Device ID cannot be empty", Toast.LENGTH_SHORT).show();
                    return false;
                }

                // Any non-empty string is allowed
                return true;
            }
        });

        // Handle generate UUID button
        Preference generateUuid = findPreference("generate_uuid");
        generateUuid.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String newUuid = UUID.randomUUID().toString();
                deviceIdPref.setText(newUuid);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(CUSTOM_DEVICE_ID, newUuid);
                editor.apply();
                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(CUSTOM_DEVICE_ID)) {
            String newDeviceId = sharedPreferences.getString(key, "");
            if (!newDeviceId.isEmpty()) {
                updateDeviceId(this, newDeviceId);
            }
        }
    }

    private void updateDeviceId(Context context, String uuid) {
        String key = Aware_Preferences.DEVICE_ID;
        String value = uuid;

        ContentValues setting = new ContentValues();
        setting.put(Aware_Provider.Aware_Settings.SETTING_KEY, key);
        setting.put(Aware_Provider.Aware_Settings.SETTING_VALUE, value);
        setting.put(Aware_Provider.Aware_Settings.SETTING_PACKAGE_NAME, "com.aware.phone");

        Cursor qry = context.getContentResolver().query(
                Aware_Provider.Aware_Settings.CONTENT_URI,
                null,
                Aware_Provider.Aware_Settings.SETTING_KEY + " LIKE '" + key + "' AND " +
                        Aware_Provider.Aware_Settings.SETTING_PACKAGE_NAME + " LIKE 'com.aware.phone'",
                null,
                null
        );

        try {
            // Update existing setting
            if (qry != null && qry.moveToFirst()) {
                if (!qry.getString(qry.getColumnIndex(Aware_Provider.Aware_Settings.SETTING_VALUE)).equals(value)) {
                    context.getContentResolver().update(
                            Aware_Provider.Aware_Settings.CONTENT_URI,
                            setting,
                            Aware_Provider.Aware_Settings.SETTING_ID + "=" +
                                    qry.getInt(qry.getColumnIndex(Aware_Provider.Aware_Settings.SETTING_ID)),
                            null
                    );

                    if (Aware.DEBUG) {
                        Log.d(Plugin.TAG, "Updated: " + key + "=" + value);
                    }

                    // Log the change if in study
                    if (Aware.isStudy(context)) {
                        ContentValues log = new ContentValues();
                        log.put(Aware_Provider.Aware_Log.LOG_TIMESTAMP, System.currentTimeMillis());
                        log.put(Aware_Provider.Aware_Log.LOG_DEVICE_ID, value);
                        log.put(Aware_Provider.Aware_Log.LOG_MESSAGE, "Device ID changed to: " + value);
                        context.getContentResolver().insert(Aware_Provider.Aware_Log.CONTENT_URI, log);
                    }

                    Toast.makeText(context, "Device ID updated successfully", Toast.LENGTH_SHORT).show();

                    // Update summary
                    EditTextPreference deviceIdPref = (EditTextPreference) findPreference(CUSTOM_DEVICE_ID);
                    deviceIdPref.setSummary("Current: " + value);
                }
            } else {
                // Insert new setting
                context.getContentResolver().insert(Aware_Provider.Aware_Settings.CONTENT_URI, setting);

                if (Aware.DEBUG) {
                    Log.d(Plugin.TAG, "Added: " + key + "=" + value);
                }

                Toast.makeText(context, "Device ID set successfully", Toast.LENGTH_SHORT).show();

                // Update summary
                EditTextPreference deviceIdPref = (EditTextPreference) findPreference(CUSTOM_DEVICE_ID);
                deviceIdPref.setSummary("Current: " + value);
            }
        } catch (SQLiteException e) {
            if (Aware.DEBUG) Log.d(Plugin.TAG, e.getMessage());
            Toast.makeText(context, "Error updating Device ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            if (Aware.DEBUG) Log.d(Plugin.TAG, e.getMessage());
            Toast.makeText(context, "Error updating Device ID: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (qry != null && !qry.isClosed()) {
                qry.close();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}