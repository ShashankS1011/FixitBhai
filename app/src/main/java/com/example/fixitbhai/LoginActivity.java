package com.example.fixitbhai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnAuthAction, btnToggleMode, btnGoogleSignIn;
    private TextView tvLoginTitle, tvLoginSubtitle;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private boolean isRegistrationMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnAuthAction = findViewById(R.id.btnAuthAction);
        btnToggleMode = findViewById(R.id.btnToggleMode);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvLoginTitle = findViewById(R.id.tvLoginTitle);
        tvLoginSubtitle = findViewById(R.id.tvLoginSubtitle);

        // Configure Google Sign In Options
        String webClientId = getWebClientId();
        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail();

        if (!TextUtils.isEmpty(webClientId) && !webClientId.contains("defaultwebclient")) {
            gsoBuilder.requestIdToken(webClientId);
        }

        mGoogleSignInClient = GoogleSignIn.getClient(this, gsoBuilder.build());

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                String idToken = account.getIdToken();
                                if (idToken != null) {
                                    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                                    signInWithFirebaseCredential(credential);
                                } else {
                                    String email = account.getEmail();
                                    Toast.makeText(this, "Signed in as " + email, Toast.LENGTH_SHORT).show();
                                    navigateToMain();
                                }
                            }
                        } catch (ApiException e) {
                            Log.e(TAG, "Google Sign In Failed", e);
                            Toast.makeText(LoginActivity.this, "Google Sign-In Failed (Code " + e.getStatusCode() + "): " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

        btnAuthAction.setOnClickListener(v -> performEmailAuth());
        btnToggleMode.setOnClickListener(v -> toggleAuthMode());
        btnGoogleSignIn.setOnClickListener(v -> performGoogleSignIn());
    }

    private String getWebClientId() {
        try {
            int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            if (resId != 0) return getString(resId);
        } catch (Exception ignored) {}
        return getString(R.string.default_web_client_id);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMain();
        }
    }

    private void toggleAuthMode() {
        isRegistrationMode = !isRegistrationMode;
        if (isRegistrationMode) {
            tvLoginTitle.setText("Create Account");
            tvLoginSubtitle.setText("Register with your email and password");
            btnAuthAction.setText("Register Account");
            btnToggleMode.setText("Already have an account? Sign In");
        } else {
            tvLoginTitle.setText("Welcome to FixitBhai");
            tvLoginSubtitle.setText("Sign in with your email or Google account");
            btnAuthAction.setText("Sign In");
            btnToggleMode.setText("Don't have an account? Register");
        }
    }

    private void performEmailAuth() {
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (isRegistrationMode) {
            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            syncLocalContactsToCloud();
                            Toast.makeText(LoginActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            String err = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                            Toast.makeText(LoginActivity.this, "Error: " + err, Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            syncLocalContactsToCloud();
                            Toast.makeText(LoginActivity.this, "Sign In Successful!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        } else {
                            String err = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(LoginActivity.this, "Error: " + err, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void performGoogleSignIn() {
        Toast.makeText(this, "Opening Google Account Picker...", Toast.LENGTH_SHORT).show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void signInWithFirebaseCredential(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        syncLocalContactsToCloud();
                        Toast.makeText(LoginActivity.this, "Google Sign In Successful!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Google Sign-In failed";
                        Toast.makeText(LoginActivity.this, "Error: " + err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void syncLocalContactsToCloud() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uploaderEmail = currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty() ? currentUser.getEmail() : "Verified Member";

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Contact> localContacts = dbHelper.getAllContacts();

        if (localContacts.isEmpty()) return;

        WriteBatch batch = firestore.batch();
        for (Contact c : localContacts) {
            String phone = c.getPhone();
            if (phone == null || phone.trim().isEmpty()) continue;
            c.setCloud(true);
            c.setUploadedBy(uploaderEmail);
            c.setUploaderUid(uid);
            c.setVerified(true);

            String docKey = phone.trim().replaceAll("[^0-9+]", "");
            String city = c.getCity() != null && !c.getCity().trim().isEmpty() ? c.getCity().trim() : "Prayagraj";

            batch.set(firestore.collection("cities").document(city).collection("contacts").document(docKey), c.toMap());
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