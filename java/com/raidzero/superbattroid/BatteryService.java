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
    private static int mCurrentEnergy;
    private static boolean mCurrentlyCharging = false;

    private BroadcastReceiver mBatteryReceiver;

    private String mUpdateWidgetAction = "com.raidzero.superbattroid.UPDATE";
    private static final String ACTION_BATTERY_REQUEST_UPDATE = "com.raidzero.superbattroid.REQUEST_UPDATE";
    private Intent mUpdateWidgetIntent;

    private IntentFilter mBatteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

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

        return START_STICKY;
    }

    private void registerReceivers() {
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                LogUtility.Log(tag, "BatteryService onReceive: " + action);

                Intent batteryStatus = context.registerReceiver(null, mBatteryIntentFilter);
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                boolean doBroadcast = false;
                boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING;
                if (mCurrentlyCharging != charging) {
                    mCurrentlyCharging = charging;
                    doBroadcast = true;
                }

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                int energy = (level * 100 / scale) * NUM_TANKS;

                LogUtility.Log(tag, "got info from battery: " + energy + ", " + level + ", " + scale);
                if (mCurrentEnergy != energy) {
                    mCurrentEnergy = energy;
                    doBroadcast = true;
                }

                if (doBroadcast) {
                    LogUtility.Log(tag, "sending new battery info");
                    mUpdateWidgetIntent.putExtra("charging", charging);
                    mUpdateWidgetIntent.putExtra("batteryLevel", energy);

                    sendBroadcast(mUpdateWidgetIntent);
                }
            }

        };

        // fire this when plugged/unplugged or an update is requested from alarm
        IntentFilter batteryIntentFilter = new IntentFilter();
        batteryIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        batteryIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        batteryIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        batteryIntentFilter.addAction(ACTION_BATTERY_REQUEST_UPDATE);

        registerReceiver(mBatteryReceiver, batteryIntentFilter);
    }


    public static int getEnergyLevel() {
        return mCurrentEnergy;
    }

    public static boolean isCharging() {
        return mCurrentlyCharging;
    }
}
