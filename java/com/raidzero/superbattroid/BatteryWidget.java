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
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DecimalFormat;

/**
 * Created by raidzero on 1/31/15.
 */
public class BatteryWidget extends AppWidgetProvider {
    private static final String tag = "BatteryWidget";
    private static final String ACTION_BATTERY_UPDATE = "com.raidzero.superbattroid.UPDATE";
    private double batteryLevel = 0;

    private static final int NUM_TANKS = 14;

    private Intent battStatsIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
    private PendingIntent pendingBattIntent;

    private AppWidgetManager widgetManager;
    private int[] appWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        this.widgetManager = appWidgetManager;
        this.appWidgetIds = appWidgetIds;

        pendingBattIntent = PendingIntent.getActivity(context, 0, battStatsIntent, 0);

        double currentLevel = calculateBatteryLevel(context);
        if (batteryChanged(currentLevel)) {
            batteryLevel = currentLevel;
        }

        updateViews(context);
    }

    private boolean batteryChanged(double currentLevelLeft) {
        return (batteryLevel != currentLevelLeft);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equals(ACTION_BATTERY_UPDATE)) {
            double currentLevel = calculateBatteryLevel(context);

            if (batteryChanged(currentLevel)) {
                batteryLevel = currentLevel;
                updateViews(context);
            }
        }
    }

    private int getTankDrawable(Context context, int energyLevel) {

        int fullTanks = 0;
        TypedArray tankImages = context.getResources().obtainTypedArray(R.array.energy_tanks);

        fullTanks = energyLevel / 100;

        Log.d(tag, "fullTanks: " + fullTanks);
        return tankImages.getResourceId(fullTanks, -1);
    }

    private int[] getEnergyDisplay(Context context, int energyLevel) {
        int level = energyLevel % 100;

        if (level == 0) {
            level = 99;
        }

        Log.d(tag, "energyDisplayed: " + level);

        TypedArray fontImages = context.getResources().obtainTypedArray(R.array.energy_font);

        int[] images = new int[2];
        images[0] = fontImages.getResourceId(level / 10, -1);
        images[1] = fontImages.getResourceId(level % 10, -1);
        return images;
    }

    private int calculateBatteryLevel(Context context) {

        Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int energyLevel = (level * 100 / scale) * NUM_TANKS;

        Log.d(tag, "percentage: " + energyLevel);
        return energyLevel;
    }

    private void updateViews(Context context) {
        int numWidgets = 0;
        if (appWidgetIds != null) {
            numWidgets = appWidgetIds.length;
            for (int i = 0; i < numWidgets; i++) {
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                views.setOnClickPendingIntent(R.id.widget_container, pendingBattIntent);

                int energyLevel = calculateBatteryLevel(context);
                int displayedTanksId = getTankDrawable(context, energyLevel);
                int[] displayedEnergy = getEnergyDisplay(context, energyLevel);

                views.setImageViewResource(R.id.tank_display, displayedTanksId);
                views.setImageViewResource(R.id.energy_display_tens, displayedEnergy[0]);
                views.setImageViewResource(R.id.energy_display_ones, displayedEnergy[1]);

                widgetManager.updateAppWidget(appWidgetIds[i], views);
            }
        }
    }
}