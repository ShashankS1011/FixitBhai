package com.example.fixitbhai;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etPhone, etOtp;
    private TextInputLayout tilOtp;
    private MaterialButton btnSendOtp, btnResendCode;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private boolean isOtpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);
        tilOtp = findViewById(R.id.tilOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnResendCode = findViewById(R.id.btnResendCode);

        // ALLOW PHONE NUMBER INPUT UP TO 16 CHARS FOR COUNTRY CODE / SPACES
        etPhone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});

        btnSendOtp.setOnClickListener(v -> {
            if (!isOtpSent) {
                sendOTP(null);
            } else {
                verifyOTP();
            }
        });

        btnResendCode.setOnClickListener(v -> sendOTP(resendToken));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }

    private void sendOTP(PhoneAuthProvider.ForceResendingToken token) {
        String digits = Objects.requireNonNull(etPhone.getText()).toString().trim().replaceAll("[^0-9]", "");

        String rawPhone = digits;
        if (digits.length() == 12 && digits.startsWith("91")) {
            rawPhone = digits.substring(2);
        } else if (digits.length() == 11 && digits.startsWith("0")) {
            rawPhone = digits.substring(1);
        } else if (digits.length() > 10) {
            rawPhone = digits.substring(digits.length() - 10);
        }

        if (rawPhone.length() != 10) {
            etPhone.setError("Enter a valid 10-digit phone number");
            return;
        }

        // Pass E.164 formatted number to Firebase Auth
        String formattedPhone = "+91" + rawPhone;

        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks);

        if (token != null) {
            builder.setForceResendingToken(token);
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build());
        Toast.makeText(this, "Sending OTP to " + formattedPhone + "...", Toast.LENGTH_SHORT).show();
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    signInWithCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.toLowerCase().contains("billing") || msg.toLowerCase().contains("quota") || msg.toLowerCase().contains("disabled")) {
                        Toast.makeText(LoginActivity.this,
                                "Firebase SMS requires Billing (Blaze Plan) or Test Numbers in Firebase Console.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(s, token);
                    verificationId = s;
                    resendToken = token;
                    isOtpSent = true;

                    etPhone.setEnabled(false);
                    tilOtp.setVisibility(View.VISIBLE);
                    btnResendCode.setVisibility(View.VISIBLE);
                    btnSendOtp.setText("Verify & Sign In");

                    Toast.makeText(LoginActivity.this, "OTP Sent!", Toast.LENGTH_SHORT).show();
                }
            };

    private void verifyOTP() {
        String code = Objects.requireNonNull(etOtp.getText()).toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            etOtp.setError("Enter valid 6-digit OTP");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        syncLocalContactsToCloud();
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String errMsg = task.getException() != null ? task.getException().getMessage() : "Verification failed";
                        Toast.makeText(LoginActivity.this, "Verification failed: " + errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void syncLocalContactsToCloud() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Contact> localContacts = dbHelper.getAllContacts();

        if (localContacts.isEmpty()) return;

        WriteBatch batch = firestore.batch();
        for (Contact c : localContacts) {
            String phone = c.getPhone();
            if (phone == null || phone.trim().isEmpty()) continue;
            String docKey = phone.trim().replaceAll("[/\\s]", "_");
            batch.set(firestore.collection("users").document(uid).collection("contacts").document(docKey), c.toMap());
        }
        batch.commit();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}