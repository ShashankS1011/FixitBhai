package com.example.fixitbhai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    public static final String PREF_KEY_INTRO_SEEN = "KEY_INTRO_SEEN";

    private ViewPager2 viewPagerIntro;
    private TextView tvStepIndicator;
    private MaterialButton btnNextIntro, btnSkipIntro;

    private static class IntroSlide {
        int iconResId;
        String title;
        String description;

        IntroSlide(int iconResId, String title, String description) {
            this.iconResId = iconResId;
            this.title = title;
            this.description = description;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        viewPagerIntro = findViewById(R.id.viewPagerIntro);
        tvStepIndicator = findViewById(R.id.tvStepIndicator);
        btnNextIntro = findViewById(R.id.btnNextIntro);
        btnSkipIntro = findViewById(R.id.btnSkipIntro);

        List<IntroSlide> slides = new ArrayList<>();
        slides.add(new IntroSlide(
                R.drawable.ic_fixitbhai_logo,
                "Smart Local Phonebook Scanner",
                "FixitBhai automatically discovers plumbers, electricians, AC repair experts, and technicians from your device phonebook while keeping your personal contacts 100% private."
        ));
        slides.add(new IntroSlide(
                R.drawable.ic_search,
                "City Directories & Service Chips",
                "Filter technicians by target cities (e.g. Noida, Gurgaon, Chennai) or tap service trade chips to find AC Repair, RO Technicians, or Electricians instantly."
        ));
        slides.add(new IntroSlide(
                R.drawable.ic_cloud_download,
                "Verified Cloud Experts & Moderation",
                "Discover community-verified repair experts from the cloud. Request cloud publication for local experts, backed by secure admin moderation."
        ));
        slides.add(new IntroSlide(
                R.drawable.ic_help,
                "One-Tap Actions & Safety Flags",
                "Call, WhatsApp, share contacts, or scan QR codes instantly. Mark bad service providers with 'Do Not Call' warnings to avoid re-hiring."
        ));

        IntroAdapter adapter = new IntroAdapter(slides);
        viewPagerIntro.setAdapter(adapter);

        viewPagerIntro.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int total = slides.size();
                if (tvStepIndicator != null) {
                    tvStepIndicator.setText((position + 1) + " of " + total);
                }
                if (btnNextIntro != null) {
                    btnNextIntro.setText(position == total - 1 ? "Get Started 🚀" : "Next ➔");
                }
            }
        });

        if (btnNextIntro != null) {
            btnNextIntro.setOnClickListener(v -> {
                int current = viewPagerIntro.getCurrentItem();
                if (current < slides.size() - 1) {
                    viewPagerIntro.setCurrentItem(current + 1, true);
                } else {
                    finishIntroAndNavigate();
                }
            });
        }

        if (btnSkipIntro != null) {
            btnSkipIntro.setOnClickListener(v -> finishIntroAndNavigate());
        }
    }

    private void finishIntroAndNavigate() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_KEY_INTRO_SEEN, true).apply();

        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(IntroActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
        }
        finish();
    }

    private static class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.SlideViewHolder> {
        private final List<IntroSlide> slides;

        IntroAdapter(List<IntroSlide> slides) {
            this.slides = slides;
        }

        @NonNull
        @Override
        public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intro_slide, parent, false);
            return new SlideViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
            IntroSlide slide = slides.get(position);
            holder.imgIcon.setImageResource(slide.iconResId);
            holder.tvTitle.setText(slide.title);
            holder.tvDescription.setText(slide.description);
        }

        @Override
        public int getItemCount() {
            return slides.size();
        }

        static class SlideViewHolder extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            TextView tvTitle, tvDescription;

            SlideViewHolder(@NonNull View itemView) {
                super(itemView);
                imgIcon = itemView.findViewById(R.id.imgIntroIcon);
                tvTitle = itemView.findViewById(R.id.tvIntroTitle);
                tvDescription = itemView.findViewById(R.id.tvIntroDescription);
            }
        }
    }
}