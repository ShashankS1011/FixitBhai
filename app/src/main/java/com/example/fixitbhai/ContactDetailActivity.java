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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ContactDetailActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvCategory, tvWarningBadge;
    private RatingBar ratingBar;
    private CheckBox cbDoNotCall;
    private EditText etNotes;
    private Button btnShareText, btnShareQR, btnSave, btnDelete;

    private String name = "";
    private String phone = "";
    private String category = "";
    private String city = "Lucknow";
    private boolean isCloud = false; // Added to track cloud status

    private DatabaseHelper dbHelper;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        dbHelper = new DatabaseHelper(this);
        firestore = FirebaseFirestore.getInstance();

        initViews();
        extractIntentData();
        setupListeners();
    }

    private void initViews() {
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
        btnDelete = findViewById(R.id.btnDelete); // FIXED: Added this so delete button works
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EXTRA_CONTACT")) {
            Contact contact;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                contact = intent.getSerializableExtra("EXTRA_CONTACT", Contact.class);
            } else {
                contact = (Contact) intent.getSerializableExtra("EXTRA_CONTACT");
            }
            if (contact != null) {
                name = contact.getName() != null ? contact.getName() : "Unknown";
                phone = contact.getPhone() != null ? contact.getPhone() : "N/A";
                category = contact.getCategory() != null ? contact.getCategory() : "General";
                city = contact.getCity() != null ? contact.getCity() : "Lucknow";
                isCloud = contact.isCloud(); // Extract cloud status

                if (tvName != null) tvName.setText(name);
                if (tvPhone != null) tvPhone.setText(phone);
                if (tvCategory != null) tvCategory.setText(category + " • " + city);

                if (ratingBar != null) ratingBar.setRating(contact.getRating());
                if (etNotes != null) etNotes.setText(contact.getNotes());
                if (cbDoNotCall != null) {
                    cbDoNotCall.setChecked(contact.isDoNotCall());
                    if (tvWarningBadge != null) {
                        tvWarningBadge.setVisibility(contact.isDoNotCall() ? View.VISIBLE : View.GONE);
                    }
                }
            }
        }
    }

    private void setupListeners() {
        if (cbDoNotCall != null) {
            cbDoNotCall.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (tvWarningBadge != null) {
                    tvWarningBadge.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            });
        }

        if (btnShareText != null) btnShareText.setOnClickListener(v -> shareContactAsText());
        if (btnShareQR != null) btnShareQR.setOnClickListener(v -> showQRCodeDialog());
        if (btnSave != null) btnSave.setOnClickListener(v -> saveContactChanges());
        if (btnDelete != null) btnDelete.setOnClickListener(v -> confirmAndDeleteContact());
    }

    private void saveContactChanges() {
        float rating = ratingBar != null ? ratingBar.getRating() : 0.0f;
        String notes = etNotes != null ? etNotes.getText().toString().trim() : "";
        boolean isDoNotCall = cbDoNotCall != null && cbDoNotCall.isChecked();

        // 1. Update SQLite Local Storage
        dbHelper.updateContactDetails(phone, rating, notes, isDoNotCall);

        // 2. Sync to Firebase Cloud Firestore
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            // FIXED: Using the 5-parameter constructor to match your updated Contact class
            Contact updatedContact = new Contact(name, phone, category, city, isCloud);
            updatedContact.setRating(rating);
            updatedContact.setNotes(notes);
            updatedContact.setDoNotCall(isDoNotCall);

            firestore.collection("users")
                    .document(userId)
                    .collection("contacts")
                    .document(phone)
                    .set(updatedContact.toMap());
        }

        Toast.makeText(this, "Contact details updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmAndDeleteContact() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete " + name + " from local storage and cloud backup?")
                .setPositiveButton("Delete", (dialog, which) -> deleteContact())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteContact() {
        // 1. Delete from SQLite local database
        dbHelper.deleteContact(phone);

        // 2. Delete from Firestore if authenticated
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            firestore.collection("users")
                    .document(userId)
                    .collection("contacts")
                    .document(phone)
                    .delete();
        }

        Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void shareContactAsText() {
        float rating = ratingBar != null ? ratingBar.getRating() : 0;
        String notes = etNotes != null ? etNotes.getText().toString().trim() : "";
        boolean isDoNotCall = cbDoNotCall != null && cbDoNotCall.isChecked();

        StringBuilder shareBody = new StringBuilder();
        shareBody.append("FixitBhai Service Contact\n\n");
        shareBody.append("Name: ").append(name).append("\n");
        shareBody.append("Service: ").append(category).append("\n");
        shareBody.append("City: ").append(city).append("\n");
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
                    "ADR;TYPE=HOME:;;" + city + ";;;;\n" +
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