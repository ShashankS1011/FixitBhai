package com.example.fixitbhai;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

public class AboutActivity extends AppCompatActivity {

    // Update with your actual GitHub repository URL
    private static final String GITHUB_REPO_URL = "https://github.com/ShashankS1011/FixitBhai";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enforce Dark Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About FixitBhai");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bind Developer Card and set Click Listener to open GitHub
        CardView cardDeveloper = findViewById(R.id.cardDeveloper);
        if (cardDeveloper != null) {
            cardDeveloper.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL));
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}