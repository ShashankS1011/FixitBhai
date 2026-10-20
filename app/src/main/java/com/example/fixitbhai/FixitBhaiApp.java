package com.example.fixitbhai;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class FixitBhaiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enforce high-contrast dark theme globally for gorgeous UI aesthetics
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}