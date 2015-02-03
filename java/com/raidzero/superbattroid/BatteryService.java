package com.raidzero.superbattroid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * Created by posborn on 2/2/15.
 */
public class BatteryService extends Service {
    private static final String tag = "BatteryService";
    private static final int NUM_TANKS = 15;
    public static boolean isRunning;
    private int currentEnergy;
    private boolean currentlyCharging = false;

    private BroadcastReceiver mBatteryReceiver;

    private String mUpdateWidgetAction = "com.raidzero.superbattroid.UPDATE";
    private Intent mUpdateWidgetIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtility.Log(tag, "onCreate()");

        mUpdateWidgetIntent = new Intent(mUpdateWidgetAction);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtility.Log(tag, "onDestroy()");

        if (mBatteryReceiver != null) {
            unregisterReceiver(mBatteryReceiver);
        }

        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        LogUtility.Log(tag, "onStartCommand()");

        registerReceivers();

        isRunning = true;

        return START_NOT_STICKY;
    }

    private void registerReceivers() {
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                LogUtility.Log(tag, "onReceive: " + action);
                if (isScreenOn()) {
                    int energyLevel = getEnergyLevel(context);
                    boolean charging = false;

                    if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                        charging = true;
                    } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                        charging = false;
                    } else {
                        charging = isCharging(context);
                    }

                    if (energyLevel != currentEnergy || currentlyCharging != charging) {
                        mUpdateWidgetIntent.putExtra("batteryLevel", energyLevel);
                        mUpdateWidgetIntent.putExtra("charging", charging);
                        sendBroadcast(mUpdateWidgetIntent);
                        currentEnergy = energyLevel;
                        currentlyCharging = charging;
                    }
                }
            }
        };

        IntentFilter batteryIntentFilter = new IntentFilter();
        batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        batteryIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);

        registerReceiver(mBatteryReceiver, batteryIntentFilter);
    }


    public static int getEnergyLevel(Context context) {
        Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int energyLevel = (level * 100 / scale) * NUM_TANKS;

        LogUtility.Log(tag, "getEnergyLevel: " + energyLevel);
        return energyLevel;
    }

    public static boolean isCharging(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING;
    }

    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }
}
