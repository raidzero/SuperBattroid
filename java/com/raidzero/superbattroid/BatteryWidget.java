package com.raidzero.superbattroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by raidzero on 1/31/15.
 */
public abstract class BatteryWidget extends AppWidgetProvider {
    private static final String tag = "BatteryWidget";
    private static final String ACTION_BATTERY_UPDATE = "com.raidzero.superbattroid.UPDATE";
    private static final String ACTION_BATTERY_REQUEST_UPDATE = "com.raidzero.superbattroid.REQUEST_UPDATE";

    protected int energyLevel =0 ;
    protected boolean mCharging = false;

    protected Intent battStatsIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);

    @Override
    public void onEnabled(Context context) {
        LogUtility.Log(tag, "onEnabled()");

        context.startService(new Intent(context, BatteryService.class));

        enableAlarm(context, true);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        LogUtility.Log(tag, "onDisabled()");

        if (BatteryService.isRunning) {
            context.stopService(new Intent(context, BatteryService.class));
        }

        enableAlarm(context, false);
        super.onDisabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        LogUtility.Log(tag, "onUpdate()");

        if (!BatteryService.isRunning) {
            context.startService(new Intent(context, BatteryService.class));
        }

        // ask service for update right away
        context.sendBroadcast(new Intent(ACTION_BATTERY_REQUEST_UPDATE));

        energyLevel = BatteryService.getEnergyLevel();
        mCharging = BatteryService.isCharging();

        updateViews(context);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        LogUtility.Log(tag, "onReceive: " + intent.getAction());

        // update widget
        if (intent.getAction().equals(ACTION_BATTERY_UPDATE)) {
            energyLevel = intent.getIntExtra("batteryLevel", 0);
            mCharging = intent.getBooleanExtra("charging", false);

            LogUtility.Log(tag, "Got energy level: " + energyLevel);
            updateViews(context);
        }
    }

    protected int getTankDrawable(Context context, int energyLevel) {

        int fullTanks = 0;
        TypedArray tankImages = context.getResources().obtainTypedArray(R.array.energy_tanks);

        LogUtility.Log(tag, "getTankDrawable(" + energyLevel + ")");

        fullTanks = energyLevel / 100;

        if (fullTanks > 14) {
            fullTanks = 14;
        }

        LogUtility.Log(tag, "fullTanks: " + fullTanks);
        return tankImages.getResourceId(fullTanks, -1);
    }

    protected int[] getEnergyDisplay(Context context, int energyLevel) {
        int level = energyLevel % 100;

        if (level == 0) {
            level = 99;
        }

        if (energyLevel == 0) {
            level = 0;
        }

        LogUtility.Log(tag, "energyDisplayed: " + level);

        TypedArray fontImages = context.getResources().obtainTypedArray(R.array.energy_font);

        int[] images = new int[2];
        images[0] = fontImages.getResourceId(level / 10, -1);
        images[1] = fontImages.getResourceId(level % 10, -1);
        return images;
    }

    protected abstract void updateViews(Context context);

    private void enableAlarm(Context context, boolean enabled) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_BATTERY_REQUEST_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        if (enabled) {
            // send request after 1 sec, then every 2 mins
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, 1000 * 60 * 2, pendingIntent);
            LogUtility.Log(tag, "Alarm set");
        } else {
            alarmManager.cancel(pendingIntent);
            LogUtility.Log(tag, "Alarm canceled");
        }
    }
}