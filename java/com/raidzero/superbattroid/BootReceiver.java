package com.raidzero.superbattroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by posborn on 2/3/15.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtility.Log("SuperBattroid", "Received boot complete event. Starting battery service.");
        context.startService(new Intent(context, BatteryService.class));
    }
}
