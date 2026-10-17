package com.example.fixitbhai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.card.MaterialCardView;

public class AboutActivity extends AppCompatActivity {

    private static final String GITHUB_REPO_URL = "https://github.com/ShashankS1011/FixitBhai";
    private static final String ISSUES_URL = "https://github.com/ShashankS1011/FixitBhai/issues/new";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About FixitBhai");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialCardView cardDeveloper = findViewById(R.id.cardDeveloper);
        if (cardDeveloper != null) {
            cardDeveloper.setOnClickListener(v -> openUrl(GITHUB_REPO_URL));
        }

        MaterialCardView cardFeedback = findViewById(R.id.cardFeedback);
        if (cardFeedback != null) {
            cardFeedback.setOnClickListener(v -> openUrl(ISSUES_URL));
        }

        com.google.android.material.button.MaterialButton btnCheckUpdates = findViewById(R.id.btnCheckUpdates);
        if (btnCheckUpdates != null) {
            btnCheckUpdates.setOnClickListener(v -> UpdateChecker.checkForUpdates(this, true, null));
        }
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}