package com.example.wisatasumbar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

public class MyLocationService extends BroadcastReceiver {
    public final static String ACTION_PROCESS_UPDATE = "wisataSumbar.UPDATE_LOCATION";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            final String action = intent.getAction();
            if(ACTION_PROCESS_UPDATE.equals(action)){
                LocationResult result = LocationResult.extractResult(intent);
                if(result != null){
                    Location location = result.getLastLocation();
                }
            }
        }
    }
}
