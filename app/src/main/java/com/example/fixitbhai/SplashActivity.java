package com.example.fixitbhai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        navigateRunnable = () -> {
            if (!isFinishing() && !isDestroyed()) {
                android.content.SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
                boolean introSeen = prefs.getBoolean(IntroActivity.PREF_KEY_INTRO_SEEN, false);

                Intent intent;
                if (!introSeen) {
                    intent = new Intent(SplashActivity.this, IntroActivity.class);
                } else if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        };

        handler.postDelayed(navigateRunnable, SPLASH_DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
    }
}