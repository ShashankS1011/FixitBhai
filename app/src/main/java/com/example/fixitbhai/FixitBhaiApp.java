package com.example.fixitbhai;

import android.app.Application;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatDelegate;

public class FixitBhaiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enforce high-contrast dark theme globally for gorgeous UI aesthetics
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Enable StrictMode in debug builds to catch thread policy violations early
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }
    }
}