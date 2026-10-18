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

        MaterialCardView cardPrivacyPolicy = findViewById(R.id.cardPrivacyPolicy);
        if (cardPrivacyPolicy != null) {
            cardPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());
        }

        android.widget.TextView tvVersion = findViewById(R.id.tvVersion);
        if (tvVersion != null) {
            tvVersion.setText("Version " + BuildConfig.VERSION_NAME);
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

    private void showPrivacyPolicyDialog() {
        android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        scrollView.setPadding(40, 30, 40, 20);

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);

        android.widget.TextView tvTitle = new android.widget.TextView(this);
        tvTitle.setText("Privacy Policy & Data Security");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(android.graphics.Color.parseColor("#F8FAFC"));

        android.widget.TextView tvSubtitle = new android.widget.TextView(this);
        tvSubtitle.setText("Your personal contacts remain 100% private and secure.");
        tvSubtitle.setTextSize(13);
        tvSubtitle.setTextColor(android.graphics.Color.parseColor("#38BDF8"));
        tvSubtitle.setPadding(0, 6, 0, 16);

        android.widget.TextView tvBody = new android.widget.TextView(this);
        tvBody.setLineSpacing(6f, 1f);
        tvBody.setTextSize(13);
        tvBody.setTextColor(android.graphics.Color.parseColor("#CBD5E1"));
        tvBody.setText("At FixitBhai, we respect your personal privacy above everything else.\n\n" +
                "1. 100% Offline Local Processing\n" +
                "FixitBhai scans your device's phonebook STRICTLY LOCALLY on your phone to organize home repair experts (AC Repair, Plumbers, Electricians, RO Technicians). Unuploaded contacts remain stored exclusively in your device's private offline database.\n\n" +
                "2. Zero Unsanctioned Cloud Access\n" +
                "We NEVER read, store, transmit, or view any of your personal, family, or private contacts. Only technicians you EXPLICITLY choose to publish via long-press or Cloud Backup are uploaded to the shared directory.\n\n" +
                "3. Complete Control & System Deletion\n" +
                "When you delete a contact in FixitBhai, it is permanently deleted from your app database, your personal cloud backup, and your device's system phonebook/SIM card.\n\n" +
                "4. No Third-Party Sharing\n" +
                "Your contact data is never sold, shared with advertisers, or analyzed for marketing.");

        container.addView(tvTitle);
        container.addView(tvSubtitle);
        container.addView(tvBody);
        scrollView.addView(container);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setView(scrollView)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}