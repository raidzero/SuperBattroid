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
                if (isScreenOn()) {
                    int energyLevel = getEnergyLevel(context);
                    if (energyLevel != currentEnergy) {
                        mUpdateWidgetIntent.putExtra("batteryLevel", energyLevel);
                        sendBroadcast(mUpdateWidgetIntent);
                        currentEnergy = energyLevel;
                    }
                }
            }
        };

        IntentFilter batteryIntentFilter = new IntentFilter();
        batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

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

    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }
}
