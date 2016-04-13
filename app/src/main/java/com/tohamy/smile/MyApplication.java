package com.tohamy.smile;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by tohamy on 3/31/16.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
