package com.raidzero.superbattroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by raidzero on 2/3/15.
 */
public class BatteryWidget_2x1 extends BatteryWidget {
    private static final String tag = "BatteryWidget_2x1";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    protected void updateViews(Context context) {
        LogUtility.Log(tag, "updateViews");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_2x1);
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

        ComponentName componentName = new ComponentName(context, BatteryWidget_2x1.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(componentName, views);
    }
}
