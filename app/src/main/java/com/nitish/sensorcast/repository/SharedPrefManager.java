package com.nitish.sensorcast.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.nitish.sensorcast.BuildConfig;

public class SharedPrefManager {

    private static final String SP_NAME = BuildConfig.APPLICATION_ID;
    private static final int ACCESS_MODE = Context.MODE_PRIVATE;

    private static final String SENSOR_DATA_KEY = "SENSOR_DATA_KEY";

    private static final String SENSOR_DATA_DEFAULT = "NO SENSOR LOG FOUND";

    private static SharedPrefManager sharedPrefManager;
    private SharedPreferences sharedPreferences;
    private Context context;

    public static SharedPrefManager getInstance(Context context) {
        if (null == sharedPrefManager) {
            sharedPrefManager = new SharedPrefManager(context);
        }
        return sharedPrefManager;
    }


    public SharedPrefManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(SP_NAME, ACCESS_MODE);
    }

    private void setString(Context context, String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String getString(Context context, String key, String def_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, ACCESS_MODE);
        return sharedPreferences.getString(key, def_value);
    }

    private void setInteger(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, ACCESS_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }


    private int getInteger(Context context, String key, int def_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, ACCESS_MODE);
        return sharedPreferences.getInt(key, def_value);
    }

    private void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, ACCESS_MODE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private boolean getBoolean(Context context, String key, boolean def_value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, ACCESS_MODE);
        return sharedPreferences.getBoolean(key, def_value);
    }


    private void setSensorLog(String data) {
        setString(context, SENSOR_DATA_KEY, data);
    }

    public String updateSensorLog(String data) {
        String oldData = getSensorLog();
        oldData = oldData.equals(SENSOR_DATA_DEFAULT) ? "" : oldData;
        String result = oldData + "\n" + data;
        setSensorLog(result);
        return result;
    }

    public String getSensorLog(){
        return getString(context, SENSOR_DATA_KEY, SENSOR_DATA_DEFAULT);
    }

    public void clearSensorLog(){
        setSensorLog(SENSOR_DATA_DEFAULT);
    }


}
