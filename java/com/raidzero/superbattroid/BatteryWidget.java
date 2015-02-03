package com.raidzero.superbattroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.BatteryManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by raidzero on 1/31/15.
 */
public class BatteryWidget extends AppWidgetProvider {
    private static final String tag = "BatteryWidget";
    private static final String ACTION_BATTERY_UPDATE = "com.raidzero.superbattroid.UPDATE";
    private int energyLevel;

    private boolean mCharging = false;
    private Intent battStatsIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);

    @Override
    public void onEnabled(Context context) {
        LogUtility.Log(tag, "onEnabled()");

        context.startService(new Intent(context, BatteryService.class));
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        LogUtility.Log(tag, "onDisabled()");

        if (BatteryService.isRunning) {
            context.stopService(new Intent(context, BatteryService.class));
        }
        super.onDisabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        LogUtility.Log(tag, "onUpdate()");

        if (!BatteryService.isRunning) {
            context.startService(new Intent(context, BatteryService.class));
        }
        energyLevel = BatteryService.getEnergyLevel(context);

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
}