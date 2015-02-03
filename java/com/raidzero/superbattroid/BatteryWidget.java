package com.raidzero.superbattroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by raidzero on 1/31/15.
 */
public class BatteryWidget extends AppWidgetProvider {
    private static final String tag = "BatteryWidget";
    private static final String ACTION_BATTERY_UPDATE = "com.raidzero.superbattroid.UPDATE";
    private static final String ACTION_BATTERY_REQUEST_UPDATE = "com.raidzero.superbattroid.REQUEST_UPDATE";

    private int energyLevel =0 ;
    private boolean mCharging = false;

    private Intent battStatsIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);

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

        energyLevel = BatteryService.getEnergyLevel(context);
        mCharging = BatteryService.isCharging(context);

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

    private int getTankDrawable(Context context, int energyLevel) {

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

    private int[] getEnergyDisplay(Context context, int energyLevel) {
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

    private void updateViews(Context context) {
        LogUtility.Log(tag, "updateViews");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        PendingIntent pendingBattIntent = PendingIntent.getActivity(context, 0, battStatsIntent, 0);

        views.setOnClickPendingIntent(R.id.widget_container, pendingBattIntent);

        int displayedTanksId = getTankDrawable(context, energyLevel);
        int[] displayedEnergy = getEnergyDisplay(context, energyLevel);

        views.setImageViewResource(R.id.tank_display, displayedTanksId);
        views.setImageViewResource(R.id.energy_display_tens, displayedEnergy[0]);
        views.setImageViewResource(R.id.energy_display_ones, displayedEnergy[1]);

        if (mCharging) {
            views.setViewVisibility(R.id.charging, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.charging, View.GONE);
        }

        ComponentName componentName = new ComponentName(context, BatteryWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(componentName, views);
    }

    private void enableAlarm(Context context, boolean enabled) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_BATTERY_REQUEST_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        if (enabled) {
            // every 2 minutes
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, 120 * 1000, pendingIntent);
            LogUtility.Log(tag, "Alarm set");
        } else {
            alarmManager.cancel(pendingIntent);
            LogUtility.Log(tag, "Alarm canceled");
        }
    }
}