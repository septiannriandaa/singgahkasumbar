package com.example.wisatasumbar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

public class SplashScreen extends AppCompatActivity {
    private int waktu_loading = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

//        LocationManager lm = (LocationManager)
//                getSystemService(Context. LOCATION_SERVICE ) ;
//        boolean network_enabled = false;
//        try {
//            network_enabled = lm.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
//        } catch (Exception e) {
//            e.printStackTrace() ;
//        }
//        if(!network_enabled) {
//            new AlertDialog.Builder(this)
//                    .setMessage("Silahkan Aktifkan Internet Anda")
//                    .setPositiveButton("Settings", new
//                            DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                                    startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS));
//                                }
//                            })
//                    .setNegativeButton("Cancel", null)
//                    .show();
//        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent dashboard = new Intent(SplashScreen.this,Dashboard.class);
                    startActivity(dashboard);
                    finish();
                }
            },waktu_loading);

    }
}
