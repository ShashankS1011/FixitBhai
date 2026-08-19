package com.example.fixitbhai;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ContactDetailActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvCategory, tvWarningBadge;
    private RatingBar ratingBar;
    private CheckBox cbDoNotCall;
    private EditText etNotes;
    private Button btnShareText, btnShareQR, btnSave;

    private String name = "";
    private String phone = "";
    private String category = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        tvName = findViewById(R.id.tvContactName);
        tvPhone = findViewById(R.id.tvContactPhone);
        tvCategory = findViewById(R.id.tvContactCategory);
        tvWarningBadge = findViewById(R.id.tvWarningBadge);
        ratingBar = findViewById(R.id.ratingBar);
        cbDoNotCall = findViewById(R.id.cbDoNotCall);
        etNotes = findViewById(R.id.etNotes);
        btnShareText = findViewById(R.id.btnShareText);
        btnShareQR = findViewById(R.id.btnShareQR);
        btnSave = findViewById(R.id.btnSave);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EXTRA_CONTACT")) {
            Contact contact = (Contact) intent.getSerializableExtra("EXTRA_CONTACT");
            if (contact != null) {
                name = contact.getName();
                phone = contact.getPhone();
                category = contact.getCategory();

                tvName.setText(name);
                tvPhone.setText(phone);
                tvCategory.setText(category);
            }
        }

        cbDoNotCall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvWarningBadge.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnShareText.setOnClickListener(v -> shareContactAsText());
        btnShareQR.setOnClickListener(v -> showQRCodeDialog());

        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Contact details updated", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void shareContactAsText() {
        float rating = ratingBar.getRating();
        String notes = etNotes.getText().toString().trim();
        boolean isDoNotCall = cbDoNotCall.isChecked();

        StringBuilder shareBody = new StringBuilder();
        shareBody.append("FixitBhai Service Contact\n\n");
        shareBody.append("Name: ").append(name).append("\n");
        shareBody.append("Service: ").append(category).append("\n");
        shareBody.append("Phone: ").append(phone).append("\n");

        if (rating > 0) {
            shareBody.append("Rating: ").append((int) rating).append("/5 Stars\n");
        }

        if (isDoNotCall) {
            shareBody.append("\nNOTE: Marked as Do Not Call Again in FixitBhai\n");
        } else if (!notes.isEmpty()) {
            shareBody.append("\nNotes/Rates: ").append(notes).append("\n");
        }

        shareBody.append("\nShared via FixitBhai App");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareBody.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Contact via:");
        startActivity(shareIntent);
    }

    private void showQRCodeDialog() {
        try {
            String vCard = "BEGIN:VCARD\n" +
                    "VERSION:3.0\n" +
                    "N:" + name + ";;;\n" +
                    "FN:" + name + "\n" +
                    "TEL;TYPE=CELL:" + phone + "\n" +
                    "NOTE:FixitBhai Service - " + category + "\n" +
                    "END:VCARD";

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(vCard, BarcodeFormat.QR_CODE, 600, 600);

            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(bitmap);
            imageView.setPadding(32, 32, 32, 32);

            new AlertDialog.Builder(this)
                    .setTitle("Scan to Save " + name)
                    .setMessage("Scan this QR code with a phone camera to save the contact directly.")
                    .setView(imageView)
                    .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                    .show();

        } catch (Exception e) {
            Toast.makeText(this, "Could not generate QR code.", Toast.LENGTH_SHORT).show();
        }
    }
}