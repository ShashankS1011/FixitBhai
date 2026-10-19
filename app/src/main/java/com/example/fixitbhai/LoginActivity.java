package com.example.fixitbhai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout tilFullName;
    private TextInputEditText etFullName, etEmail, etPassword;
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

        tilFullName = findViewById(R.id.tilFullName);
        etFullName = findViewById(R.id.etFullName);
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
                                String displayName = account.getDisplayName() != null ? account.getDisplayName().trim() : "";
                                if (idToken != null && !idToken.isEmpty()) {
                                    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                                    signInWithFirebaseCredential(credential, displayName);
                                } else if (account.getEmail() != null) {
                                    handleGoogleAccountDirectly(account);
                                }
                            }
                        } catch (ApiException e) {
                            Log.e(TAG, "Google Sign In ApiException Code: " + e.getStatusCode(), e);
                            String errorMsg = "Google Sign-In Error (" + e.getStatusCode() + ").";
                            if (e.getStatusCode() == 10 || e.getStatusCode() == 12500) {
                                errorMsg = "Google Sign-In Configuration Error (SHA-1/Web Client ID mismatch in Firebase). Please sign in using Email/Password.";
                            }
                            Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        btnAuthAction.setOnClickListener(v -> performEmailAuth());
        btnToggleMode.setOnClickListener(v -> toggleAuthMode());
        btnGoogleSignIn.setOnClickListener(v -> performGoogleSignIn());
    }

    private void updateGoogleDisplayName(String displayName) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName.trim())
                        .build();
                currentUser.updateProfile(profileUpdates);
            }
        }
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
            tvLoginSubtitle.setText("Register with your name, email and password");
            btnAuthAction.setText("Register Account");
            btnToggleMode.setText("Already have an account? Sign In");
            if (tilFullName != null) tilFullName.setVisibility(View.VISIBLE);
        } else {
            tvLoginTitle.setText("Welcome to FixitBhai");
            tvLoginSubtitle.setText("Sign in with your email or Google account");
            btnAuthAction.setText("Sign In");
            btnToggleMode.setText("Don't have an account? Register");
            if (tilFullName != null) tilFullName.setVisibility(View.GONE);
        }
    }

    private void performEmailAuth() {
        String email = etEmail != null && etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null && etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String fullName = etFullName != null && etFullName.getText() != null ? etFullName.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (etEmail != null) etEmail.setError("Enter a valid email address");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            if (etPassword != null) etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (isRegistrationMode) {
            if (TextUtils.isEmpty(fullName)) {
                if (etFullName != null) etFullName.setError("Please enter your full name");
                return;
            }
            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();
                                user.updateProfile(profileUpdates).addOnCompleteListener(uTask -> {
                                    Toast.makeText(LoginActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                                    restoreUserCloudBackupAndNavigate();
                                });
                            } else {
                                restoreUserCloudBackupAndNavigate();
                            }
                        } else {
                            String err = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                            Toast.makeText(LoginActivity.this, "Registration Error: " + err, Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Sign In Successful!", Toast.LENGTH_SHORT).show();
                            restoreUserCloudBackupAndNavigate();
                        } else {
                            String err = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(LoginActivity.this, "Sign-In Error: " + err, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void performGoogleSignIn() {
        if (mGoogleSignInClient != null) {
            Toast.makeText(this, "Opening Google Account Picker...", Toast.LENGTH_SHORT).show();
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        }
    }

    private void signInWithFirebaseCredential(AuthCredential credential, String displayName) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateGoogleDisplayName(displayName);
                        Toast.makeText(LoginActivity.this, "Google Sign In Successful!", Toast.LENGTH_SHORT).show();
                        restoreUserCloudBackupAndNavigate();
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Google Sign-In failed";
                        Toast.makeText(LoginActivity.this, "Error: " + err, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleGoogleAccountDirectly(GoogleSignInAccount account) {
        if (account == null || account.getEmail() == null) return;
        String email = account.getEmail().trim();
        String displayName = account.getDisplayName() != null ? account.getDisplayName().trim() : "";

        String pass = "GoogleAuth_" + Math.abs(email.hashCode());
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateGoogleDisplayName(displayName);
                        restoreUserCloudBackupAndNavigate();
                    } else {
                        mAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(this, createTask -> {
                                    if (createTask.isSuccessful()) {
                                        updateGoogleDisplayName(displayName);
                                        restoreUserCloudBackupAndNavigate();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Signed in with Google: " + email, Toast.LENGTH_SHORT).show();
                                        if (etEmail != null) etEmail.setText(email);
                                    }
                                });
                    }
                });
    }

    private void restoreUserCloudBackupAndNavigate() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToMain();
            return;
        }

        String uid = currentUser.getUid();
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        firestore.collection("users").document(uid).collection("contacts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                            Contact contact = doc.toObject(Contact.class);
                            if (contact != null && contact.getPhone() != null) {
                                contact.setCloud(true);
                                if (!dbHelper.contactExists(contact.getPhone())) {
                                    dbHelper.addContact(contact);
                                }
                            }
                        }
                    }
                    syncLocalContactsToCloud();
                    navigateToMain();
                });
    }

    private void syncLocalContactsToCloud() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userEmail = currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail().trim() : "";
        String displayName = currentUser != null && currentUser.getDisplayName() != null && !currentUser.getDisplayName().trim().isEmpty() ? currentUser.getDisplayName().trim() : "";

        String uploaderText;
        if (ContactAdapter.isAdminEmail(userEmail)) {
            uploaderText = "Admin";
        } else if (!displayName.isEmpty() && !userEmail.isEmpty()) {
            uploaderText = displayName + " (" + userEmail + ")";
        } else if (!userEmail.isEmpty()) {
            uploaderText = userEmail;
        } else {
            uploaderText = !displayName.isEmpty() ? displayName : "Verified Member";
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Contact> localContacts = dbHelper.getAllContacts();

        if (localContacts.isEmpty()) return;

        WriteBatch batch = firestore.batch();
        for (Contact c : localContacts) {
            String phone = c.getPhone();
            if (phone == null || phone.trim().isEmpty()) continue;
            c.setUploadedBy(uploaderText);
            c.setUploaderUid(uid);
            c.setVerified(true);

            String docKey = phone.trim().replaceAll("[^0-9+]", "");
            String city = c.getCity() != null && !c.getCity().trim().isEmpty() ? c.getCity().trim() : "Prayagraj";

            // Create a payload copy with isCloud = true for the cloud directory
            Contact cloudPayload = new Contact(c.getName(), c.getPhone(), c.getCategory(), city, true);
            cloudPayload.setRating(c.getRating());
            cloudPayload.setNotes(c.getNotes());
            cloudPayload.setDoNotCall(c.isDoNotCall());
            cloudPayload.setEmergency(c.isEmergency());
            cloudPayload.setReviewTags(c.getReviewTags());
            cloudPayload.setUploadedBy(uploaderText);
            cloudPayload.setUploaderUid(uid);
            cloudPayload.setVerified(true);

            batch.set(firestore.collection("cities").document(city).collection("contacts").document(docKey), cloudPayload.toMap());
            batch.set(firestore.collection("users").document(uid).collection("contacts").document(docKey), cloudPayload.toMap());
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