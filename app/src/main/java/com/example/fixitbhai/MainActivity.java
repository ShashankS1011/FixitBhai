package com.example.fixitbhai;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "FixitBhaiPrefs";
    private static final String KEY_CONTACT_LIST = "SavedContacts";
    private static final String KEY_SELECTED_CITY = "SelectedCity";
    private static final String ISSUES_URL = "https://github.com/ShashankS1011/FixitBhai/issues/new";
    private static final Gson GSON_INSTANCE = new Gson();
    private static final Pattern NORMALIZE_PHONE_PATTERN = Pattern.compile("[^0-9]");
    private static final android.content.res.ColorStateList CHIP_BG_COLOR = android.content.res.ColorStateList.valueOf(Color.parseColor("#1E293B"));
    private static final android.content.res.ColorStateList CHIP_EMERGENCY_BG_COLOR = android.content.res.ColorStateList.valueOf(Color.parseColor("#312E81"));
    public static final String KEY_CLOUD_SYNC_PREF = "CloudSyncEnabled";
    public static final String KEY_PREF_SET = "CloudSyncPrefSet";
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;

    private ContactAdapter adapter;
    private List<Contact> contactList;
    private DatabaseHelper dbHelper;

    private FirebaseFirestore firestore;

    private String selectedCity = "All Cities";

    private ChipGroup chipGroupCategories;
    private Chip chipCitySelect;
    private RecyclerView recyclerViewContacts;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton btnAdminPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        firestore = FirebaseFirestore.getInstance();

        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        chipCitySelect = findViewById(R.id.chipCitySelect);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        btnAdminPending = findViewById(R.id.btnAdminPending);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.accent_blue));
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.surface_dark));
            swipeRefreshLayout.setOnRefreshListener(() -> syncWithCloudAndPhonebook(true));
        }

        ImageButton btnAbout = findViewById(R.id.btnAbout);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnCloudSync = findViewById(R.id.btnCloudSync);
        ImageButton btnHelp = findViewById(R.id.btnHelp);
        FloatingActionButton btnFeedback = findViewById(R.id.btnFeedback);
        FloatingActionButton btnFabCrashLogs = findViewById(R.id.btnFabCrashLogs);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isAdmin = currentUser != null && currentUser.getEmail() != null && ContactAdapter.isAdminEmail(currentUser.getEmail());
        if (isAdmin && btnFabCrashLogs != null) {
            btnFabCrashLogs.setVisibility(View.VISIBLE);
            btnFabCrashLogs.setOnClickListener(v -> showAdminCrashLogsDialog());
        }

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        contactList = new ArrayList<>();

        boolean privacyAccepted = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getBoolean("KEY_PRIVACY_POLICY_ACCEPTED", false);
        if (privacyAccepted) {
            loadSavedState();
        }

        if (chipCitySelect != null) {
            chipCitySelect.setText("City: " + selectedCity);
            chipCitySelect.setOnClickListener(v -> showCitySelectionDialog());
        }

        adapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(adapter);

        TextInputEditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (adapter != null) {
                        adapter.filterBy(s != null ? s.toString() : "", getFilterCity());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // --- SWIPE TO DELETE LOGIC WITH VISUAL RED BACKGROUND & TRASH ICON ---
        ItemTouchHelper.SimpleCallback swipeToDeleteCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                if (dX != 0) {
                    Paint p = new Paint();
                    p.setAntiAlias(true);
                    p.setColor(Color.parseColor("#DC2626")); // Warning Red

                    RectF background;
                    if (dX > 0) {
                        // Swiping Right
                        background = new RectF((float) itemView.getLeft() + 12, (float) itemView.getTop() + 10, (float) itemView.getLeft() + dX, (float) itemView.getBottom() - 10);
                    } else {
                        // Swiping Left
                        background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop() + 10, (float) itemView.getRight() - 12, (float) itemView.getBottom() - 10);
                    }
                    c.drawRoundRect(background, 28, 28, p);

                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    if (icon != null && Math.abs(dX) > 60) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();

                        if (dX > 0) {
                            // Swiping Right -> Icon positioned on Left
                            int iconLeft = itemView.getLeft() + 32;
                            int iconRight = iconLeft + icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        } else {
                            // Swiping Left -> Icon positioned on Right
                            int iconRight = itemView.getRight() - 32;
                            int iconLeft = iconRight - icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        }
                        icon.draw(c);
                    }
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                Contact deletedContact = adapter.getContactAt(position);
                if (deletedContact == null) return;

                // Remove from UI and adapter list
                adapter.removeContact(position);
                contactList.remove(deletedContact);

                // Remove from local database
                dbHelper.deleteContact(deletedContact.getPhone());
                saveContactsToStorage();

                // Remove from system phonebook / SIM card
                deleteContactFromPhonebook(MainActivity.this, deletedContact.getPhone());

                // Remove from user's personal Firestore Backup dynamically using active UID
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    firestore.collection("users").document(currentUser.getUid())
                            .collection("contacts").document(deletedContact.getPhone().replaceAll("[^0-9+]", "")).delete();
                }

                Toast.makeText(MainActivity.this, deletedContact.getName() + " deleted from app and phonebook", Toast.LENGTH_SHORT).show();
                checkEmptyState();
                refreshCategoryChips();
            }
        };
        new ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(recyclerViewContacts);

        adapter.filterBy("", getFilterCity());
        checkEmptyState();

        // Trigger the Combined Sync Process on app launch
        syncWithCloudAndPhonebook(false);

        // Silent background update check against GitHub Releases API
        UpdateChecker.checkForUpdates(this, false, null);

        // Check and display post-update confirmation banner
        checkPostUpdateBanner();

        // Check for pending cloud contact moderation requests (Admin Only)
        checkPendingModerationRequests();

        if (btnAbout != null) btnAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
        if (btnLogout != null) btnLogout.setOnClickListener(v -> logoutUser());
        if (btnCloudSync != null) btnCloudSync.setOnClickListener(v -> showCloudContactPicker());
        if (btnHelp != null) btnHelp.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpActivity.class)));
        if (btnFeedback != null) {
            btnFeedback.setOnClickListener(v -> showReportIssueConfirmationDialog());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null) {
            List<Contact> updatedList = dbHelper.getAllContacts();
            if (!updatedList.isEmpty()) {
                contactList.clear();
                contactList.addAll(updatedList);
                if (adapter != null) {
                    adapter.updateData(contactList);
                    adapter.filterBy("", getFilterCity());
                }
                refreshCategoryChips();
            }
        }
        checkEmptyState();
    }

    private void checkPostUpdateBanner() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastSeenVersion = prefs.getString("KEY_LAST_SEEN_VERSION", "");
        String currentVersion = BuildConfig.VERSION_NAME;

        if (!lastSeenVersion.isEmpty() && !lastSeenVersion.equals(currentVersion)) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userAccount = (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty())
                    ? currentUser.getEmail()
                    : "your account";

            new MaterialAlertDialogBuilder(this)
                    .setTitle("App Updated Successfully! 🎉")
                    .setMessage("FixitBhai has been updated to v" + currentVersion + ".\n\nYou are automatically logged in as " + userAccount + ".")
                    .setPositiveButton("Awesome!", (dialog, which) -> dialog.dismiss())
                    .show();
        }
        prefs.edit().putString("KEY_LAST_SEEN_VERSION", currentVersion).apply();
    }

    private void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showReportIssueConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Report Issue / Submit Feedback")
                .setMessage("Do you want to report an issue or submit feedback for FixitBhai?\n\nYou will now be redirected to our GitHub repository issue page in your web browser.")
                .setPositiveButton("Yes, Take Me to GitHub", (dialog, which) -> {
                    Toast.makeText(MainActivity.this, "Opening GitHub in browser...", Toast.LENGTH_SHORT).show();
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ISSUES_URL));
                        startActivity(intent);
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showAdminCrashLogsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastCrash = prefs.getString("KEY_LAST_CRASH_LOG", "No recent crashes recorded by Crashlytics.");

        new MaterialAlertDialogBuilder(this)
                .setTitle("Admin Crash & Error Analytics")
                .setMessage("Firebase Crashlytics Status: Active (Monitoring ANRs & Crashes)\n\nLast Logged Event / Exception:\n" + lastCrash + "\n\nWould you like to trigger a test crash to verify Crashlytics reporting?")
                .setPositiveButton("Force Test Crash", (dialog, which) -> {
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().log("Admin triggered test crash from FixitBhai panel");
                    throw new RuntimeException("Test Crash from Admin Panel");
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void checkPendingModerationRequests() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null || !ContactAdapter.isAdminEmail(user.getEmail())) {
            if (btnAdminPending != null) btnAdminPending.setVisibility(View.GONE);
            return;
        }

        firestore.collection("moderation_requests")
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        if (btnAdminPending != null) {
                            btnAdminPending.setVisibility(View.VISIBLE);
                            btnAdminPending.setOnClickListener(v -> showAdminModerationDialog(snapshots.getDocuments()));
                        }
                    } else {
                        if (btnAdminPending != null) btnAdminPending.setVisibility(View.GONE);
                    }
                });
    }

    private void showAdminModerationDialog(List<DocumentSnapshot> pendingDocs) {
        List<DocumentSnapshot> addContactRequests = new ArrayList<>();
        List<DocumentSnapshot> deleteContactRequests = new ArrayList<>();
        List<DocumentSnapshot> addCityRequests = new ArrayList<>();

        if (pendingDocs != null) {
            for (DocumentSnapshot doc : pendingDocs) {
                String action = doc.getString("action");
                if (action != null && action.equalsIgnoreCase("DELETE")) {
                    deleteContactRequests.add(doc);
                } else if (action != null && action.equalsIgnoreCase("ADD_CITY")) {
                    addCityRequests.add(doc);
                } else {
                    addContactRequests.add(doc);
                }
            }
        }

        CharSequence[] tabOptions = {
                "Add Contact Requests (" + addContactRequests.size() + ")",
                "Delete Contact Requests (" + deleteContactRequests.size() + ")",
                "Add City Requests (" + addCityRequests.size() + ")",
                "Clear Public Cloud Directory (Admin)"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Admin Moderation Panel")
                .setItems(tabOptions, (dialog, which) -> {
                    if (which == 0) {
                        if (addContactRequests.isEmpty()) {
                            Toast.makeText(MainActivity.this, "No pending add contact requests.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showRequestListDialog(addContactRequests);
                    } else if (which == 1) {
                        if (deleteContactRequests.isEmpty()) {
                            Toast.makeText(MainActivity.this, "No pending delete contact requests.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showRequestListDialog(deleteContactRequests);
                    } else if (which == 2) {
                        if (addCityRequests.isEmpty()) {
                            Toast.makeText(MainActivity.this, "No pending city creation requests.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showRequestListDialog(addCityRequests);
                    } else if (which == 3) {
                        new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("Clear Public Cloud Directory")
                                .setMessage("Are you sure you want to permanently delete all public shared contacts from the cloud directory?")
                                .setPositiveButton("Clear All Cloud Contacts", (d, w) -> clearPublicCloudDirectory())
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void clearPublicCloudDirectory() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        firestore.collectionGroup("contacts").get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null && !snapshots.isEmpty()) {
                        com.google.firebase.firestore.WriteBatch batch = firestore.batch();
                        int count = 0;
                        for (DocumentSnapshot doc : snapshots) {
                            if (!doc.getReference().getPath().contains("/users/")) {
                                batch.delete(doc.getReference());
                                count++;
                            }
                        }
                        int finalCount = count;
                        batch.commit().addOnSuccessListener(aVoid -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Successfully cleared " + finalCount + " public contacts from cloud!", Toast.LENGTH_LONG).show();
                            contactList.clear();
                            contactList.addAll(dbHelper.getAllContacts());
                            if (adapter != null) {
                                adapter.updateData(contactList);
                                adapter.filterBy("", getFilterCity());
                            }
                            checkEmptyState();
                            checkPendingModerationRequests();
                        }).addOnFailureListener(e -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Failed to clear cloud: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Cloud directory is already empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("Firestore Rules Notice")
                            .setMessage("Collection group queries require rules enabling recursive reads in Firebase Console:\n\nmatch /{path=**}/contacts/{contactId} {\n  allow read: if true;\n}")
                            .setPositiveButton("Got it", null)
                            .show();
                });
    }

    private void showRequestListDialog(List<DocumentSnapshot> requestDocs) {
        String[] items = new String[requestDocs.size()];
        for (int i = 0; i < requestDocs.size(); i++) {
            DocumentSnapshot doc = requestDocs.get(i);
            String name = doc.getString("name");
            String city = doc.getString("city");
            String email = doc.getString("uploaderEmail");
            String action = doc.getString("action");
            if (action == null) action = "ADD";
            items[i] = "[" + action + "] " + (name != null ? name : "Contact") + " (" + (city != null ? city : "City") + ")\nUser: " + (email != null ? email : "Unknown");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Request to Review")
                .setItems(items, (dialog, which) -> {
                    DocumentSnapshot doc = requestDocs.get(which);
                    String reqId = doc.getId();
                    String action = doc.getString("action");
                    if (action == null) action = "ADD";
                    final String finalAction = action;
                    String name = doc.getString("name");
                    String city = doc.getString("city");
                    String email = doc.getString("uploaderEmail");
                    String phone = doc.getString("phone");
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> payloadMap = (java.util.Map<String, Object>) doc.get("contactPayload");

                    String cleanPhone = phone != null ? phone.replaceAll("[^0-9+]", "") : "";

                    new MaterialAlertDialogBuilder(MainActivity.this)
                            .setTitle("Review " + finalAction + " Request")
                            .setMessage(finalAction.equalsIgnoreCase("ADD_CITY") ? "Requested City Name: " + city + "\n\nRequested By User Email:\n" + (email != null ? email : "Unknown") : "Contact Name: " + name + "\nPhone: " + phone + "\nCity: " + city + "\n\nRequested By User Email:\n" + (email != null ? email : "Unknown"))
                            .setPositiveButton("Approve & Execute", (d, w) -> {
                                if (finalAction.equalsIgnoreCase("ADD_CITY")) {
                                    if (city != null && !city.trim().isEmpty()) {
                                        java.util.Map<String, Object> cityMap = new java.util.HashMap<>();
                                        cityMap.put("name", city.trim());
                                        cityMap.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                                        firestore.collection("public_cities").document(city.trim()).set(cityMap)
                                                .addOnSuccessListener(aVoid -> {
                                                    firestore.collection("moderation_requests").document(reqId).update("status", "APPROVED");
                                                    Toast.makeText(MainActivity.this, "City " + city + " approved & published for all users!", Toast.LENGTH_SHORT).show();
                                                    checkPendingModerationRequests();
                                                });
                                    }
                                } else if (finalAction.equalsIgnoreCase("DELETE")) {
                                    if (!cleanPhone.isEmpty() && city != null) {
                                        firestore.collection("cities").document(city).collection("contacts").document(cleanPhone).delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    firestore.collection("moderation_requests").document(reqId).update("status", "APPROVED");
                                                    Toast.makeText(MainActivity.this, "Deletion request approved & executed!", Toast.LENGTH_SHORT).show();
                                                    checkPendingModerationRequests();
                                                });
                                    }
                                } else {
                                    if (payloadMap != null && !cleanPhone.isEmpty() && city != null) {
                                        firestore.collection("cities").document(city).collection("contacts").document(cleanPhone).set(payloadMap)
                                                .addOnSuccessListener(aVoid -> {
                                                    firestore.collection("moderation_requests").document(reqId).update("status", "APPROVED");
                                                    Toast.makeText(MainActivity.this, "Addition request approved & published!", Toast.LENGTH_SHORT).show();
                                                    checkPendingModerationRequests();
                                                });
                                    }
                                }
                            })
                            .setNegativeButton("Reject", (d, w) -> {
                                firestore.collection("moderation_requests").document(reqId).update("status", "REJECTED");
                                Toast.makeText(MainActivity.this, "Request rejected.", Toast.LENGTH_SHORT).show();
                                checkPendingModerationRequests();
                            })
                            .setNeutralButton("Back", (d, w) -> checkPendingModerationRequests())
                            .show();
                })
                .setNegativeButton("Back", (d, w) -> checkPendingModerationRequests())
                .show();
    }

    private String getFilterCity() {
        if (selectedCity == null || selectedCity.equalsIgnoreCase("All Cities") || selectedCity.equalsIgnoreCase("All")) {
            return "";
        }
        return selectedCity;
    }

    // --- CLOUD CONTACT PICKER LOGIC ---
    public void showCloudContactPicker() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view cloud contacts.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> queryTask;
        if (getFilterCity().isEmpty()) {
            queryTask = firestore.collectionGroup("contacts").get();
        } else {
            queryTask = firestore.collection("cities").document(selectedCity).collection("contacts").get();
        }

        queryTask.addOnSuccessListener(queryDocumentSnapshots -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            List<Contact> cloudContacts = new ArrayList<>();
            Set<String> seenPhones = new HashSet<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                // Exclude private user backups from public cloud picker
                if (doc.getReference().getPath().contains("/users/") || doc.getReference().getPath().startsWith("users/")) {
                    continue;
                }

                Contact contact = doc.toObject(Contact.class);
                if (contact != null && contact.getPhone() != null) {
                    String cleanPhone = contact.getPhone().replaceAll("[^0-9]", "");
                    if (!seenPhones.contains(cleanPhone)) {
                        seenPhones.add(cleanPhone);
                        contact.setCloud(true);
                        cloudContacts.add(contact);
                    }
                }
            }

            // Sort cloud contacts alphabetically by name
            cloudContacts.sort((c1, c2) -> {
                String n1 = c1.getName() != null ? c1.getName() : "";
                String n2 = c2.getName() != null ? c2.getName() : "";
                return n1.compareToIgnoreCase(n2);
            });

            if (cloudContacts.isEmpty()) {
                Toast.makeText(this, "No public cloud contacts found for " + selectedCity + ".", Toast.LENGTH_SHORT).show();
                return;
            }

            displaySelectionDialog(cloudContacts);
        })
        .addOnFailureListener(e -> {
            Log.w("FixitBhai", "Shared city cloud query failed: " + e.getMessage() + ". Trying personal cloud backup...");
            fetchUserPersonalCloudContacts(currentUser.getUid());
        });
    }

    private void fetchUserPersonalCloudContacts(String uid) {
        firestore.collection("users")
                .document(uid)
                .collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    List<Contact> cloudContacts = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Contact contact = doc.toObject(Contact.class);
                        if (contact != null) {
                            contact.setCloud(true);
                            cloudContacts.add(contact);
                        }
                    }

                    if (cloudContacts.isEmpty()) {
                        Toast.makeText(this, "No cloud contacts found in your account.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    displaySelectionDialog(cloudContacts);
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Permission Denied: Please enable read access in Firebase Console Rules.", Toast.LENGTH_LONG).show();
                });
    }

    private void displaySelectionDialog(List<Contact> cloudContacts) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_cloud_import, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvCloudSubtitle = dialogView.findViewById(R.id.tvCloudSubtitle);
        if (tvCloudSubtitle != null) {
            tvCloudSubtitle.setText(cloudContacts.size() + " Verified Experts available in " + selectedCity);
        }

        Set<String> existingLocalPhones = new HashSet<>();
        List<Contact> localContacts = dbHelper.getAllContacts();
        for (Contact c : localContacts) {
            if (c.getPhone() != null) existingLocalPhones.add(c.getPhone());
        }

        RecyclerView rvCloudContacts = dialogView.findViewById(R.id.rvCloudContacts);
        MaterialButton btnImportSelected = dialogView.findViewById(R.id.btnImportSelected);
        MaterialButton btnCancelCloud = dialogView.findViewById(R.id.btnCancelCloud);
        MaterialButton btnSelectAll = dialogView.findViewById(R.id.btnSelectAll);
        TextInputEditText etCloudSearch = dialogView.findViewById(R.id.etCloudSearch);
        ChipGroup chipGroupCloudCategory = dialogView.findViewById(R.id.chipGroupCloudCategory);

        if (rvCloudContacts != null) {
            rvCloudContacts.setLayoutManager(new LinearLayoutManager(this));
        }

        CloudContactAdapter cloudAdapter = new CloudContactAdapter(cloudContacts, existingLocalPhones, count -> {
            if (btnImportSelected != null) {
                btnImportSelected.setText("Import Selected (" + count + ")");
            }
        });
        if (rvCloudContacts != null) {
            rvCloudContacts.setAdapter(cloudAdapter);
        }

        if (etCloudSearch != null) {
            etCloudSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    cloudAdapter.getFilter().filter(s != null ? s.toString() : "");
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSelectAll != null) {
            btnSelectAll.setOnClickListener(v -> {
                boolean select = !cloudAdapter.isAllSelected();
                cloudAdapter.selectAll(select);
                btnSelectAll.setText(select ? "Deselect All" : "Select All");
            });
        }

        if (chipGroupCloudCategory != null) {
            chipGroupCloudCategory.removeAllViews();
            Chip allChip = new Chip(this);
            allChip.setText("All");
            allChip.setCheckable(true);
            allChip.setChecked(true);
            allChip.setOnClickListener(v -> cloudAdapter.filterByCategory(""));
            chipGroupCloudCategory.addView(allChip);

            Set<String> categories = new LinkedHashSet<>();
            for (Contact c : cloudContacts) {
                if (c.getCategory() != null && !c.getCategory().trim().isEmpty()) {
                    categories.add(c.getCategory().trim());
                }
            }

            for (String categoryName : categories) {
                Chip categoryChip = new Chip(this);
                categoryChip.setText(categoryName);
                categoryChip.setCheckable(true);
                categoryChip.setOnClickListener(v -> cloudAdapter.filterByCategory(categoryName));
                chipGroupCloudCategory.addView(categoryChip);
            }
        }

        if (btnCancelCloud != null) {
            btnCancelCloud.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnImportSelected != null) {
            btnImportSelected.setOnClickListener(v -> {
                List<Contact> selectedContacts = cloudAdapter.getSelectedContacts();
                int importedCount = 0;
                for (Contact c : selectedContacts) {
                    if (!dbHelper.contactExists(c.getPhone())) {
                        dbHelper.addContact(c);
                        importedCount++;
                    }
                }

                if (importedCount > 0) {
                    contactList.clear();
                    contactList.addAll(dbHelper.getAllContacts());
                    saveContactsToStorage();
                    adapter.updateData(contactList);
                    adapter.filterBy("", selectedCity.equalsIgnoreCase("All Cities") ? "" : selectedCity);
                    refreshCategoryChips();
                    checkEmptyState();
                }

                Toast.makeText(MainActivity.this, importedCount + " cloud contact(s) imported to your device!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    // --- COMBINED SYNC ORCHESTRATOR ---
    private void syncWithCloudAndPhonebook(boolean showToast) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerViewContacts != null) recyclerViewContacts.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);

        if (showToast) {
            fetchPublicCloudContactsForMainList(true);
        }

        // Scan local phonebook contacts (cloud contacts are imported explicitly via the cloud button)
        checkContactPermissionAndScan(showToast);
    }

    private void checkEmptyState() {
        if (contactList.isEmpty()) {
            if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.VISIBLE);
            if (recyclerViewContacts != null) recyclerViewContacts.setVisibility(View.GONE);
        } else {
            if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
            if (recyclerViewContacts != null) recyclerViewContacts.setVisibility(View.VISIBLE);
        }
    }

    public void logoutUser() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    dbHelper.deleteAllContacts();
                    contactList.clear();
                    saveContactsToStorage();
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().putBoolean("KEY_PRIVACY_POLICY_ACCEPTED", false).apply();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void showCitySelectionDialog() {
        List<String> distinctCities = dbHelper.getDistinctCities();
        List<String> cityList = new ArrayList<>();
        cityList.add("All Cities");
        for (String c : distinctCities) {
            if (!cityList.contains(c) && !c.equalsIgnoreCase("Unassigned")) {
                cityList.add(c);
            }
        }

        String[] presetCities = {
                "Gurgaon", "Noida", "Delhi", "Mathura", "Ghaziabad", "Faridabad",
                "Prayagraj", "Chennai", "Lucknow", "Mumbai", "Bengaluru", "Hyderabad",
                "Kolkata", "Pune", "Jaipur", "Kanpur", "Varanasi", "Indore", "Surat"
        };
        for (String preset : presetCities) {
            if (!cityList.contains(preset)) {
                cityList.add(preset);
            }
        }

        firestore.collection("public_cities").get().addOnSuccessListener(snapshots -> {
            if (snapshots != null && !snapshots.isEmpty()) {
                for (DocumentSnapshot doc : snapshots) {
                    String pubCity = doc.getString("name");
                    if (pubCity != null && !pubCity.trim().isEmpty() && !cityList.contains(pubCity.trim())) {
                        cityList.add(pubCity.trim());
                    }
                }
            }
            cityList.add("➕ Add Custom City...");
            renderCityDialogList(cityList);
        }).addOnFailureListener(e -> {
            cityList.add("➕ Add Custom City...");
            renderCityDialogList(cityList);
        });
    }

    private void renderCityDialogList(List<String> cityList) {
        final String[] cityArray = cityList.toArray(new String[0]);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_city_select, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ListView listView = dialogView.findViewById(R.id.listViewCities);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.item_city_choice, cityArray);
        listView.setAdapter(arrayAdapter);

        for (int i = 0; i < cityArray.length; i++) {
            if (cityArray[i].equalsIgnoreCase(selectedCity)) {
                listView.setItemChecked(i, true);
                break;
            }
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String choice = cityArray[position];
            if (choice.contains("Add Custom City")) {
                dialog.dismiss();
                showAddCustomCityDialog();
            } else {
                selectedCity = choice;
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putString(KEY_SELECTED_CITY, selectedCity).apply();

                if (chipCitySelect != null) chipCitySelect.setText("City: " + selectedCity);

                if (dbHelper != null) {
                    List<Contact> allLocal = dbHelper.getAllContacts();
                    contactList.clear();
                    contactList.addAll(allLocal);
                    if (adapter != null) {
                        adapter.updateData(contactList);
                        adapter.filterBy("", getFilterCity());
                    }
                    refreshCategoryChips();
                }

                checkEmptyState();

                Toast.makeText(MainActivity.this, "Switched filter to: " + selectedCity, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        ImageButton btnCityHelp = dialogView.findViewById(R.id.btnCityHelp);
        if (btnCityHelp != null) {
            btnCityHelp.setOnClickListener(v -> new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("City Selection")
                    .setMessage("FixitBhai organizes your contacts by city (e.g. Gurgaon, Noida, Delhi, Mathura, Prayagraj, Chennai). Select 'All Cities' to view contacts everywhere or tap 'Add Custom City' to type a new location!")
                    .setPositiveButton("Got It", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show());
        }
        dialog.show();
    }

    private void showAddCustomCityDialog() {
        com.google.android.material.textfield.TextInputLayout tilCustomCity = new com.google.android.material.textfield.TextInputLayout(this);
        tilCustomCity.setHint("Enter City Name (e.g. Mathura, Agra)");
        tilCustomCity.setPadding(32, 16, 32, 16);

        TextInputEditText etCustomCity = new TextInputEditText(this);
        etCustomCity.setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        tilCustomCity.addView(etCustomCity);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Custom City")
                .setView(tilCustomCity)
                .setPositiveButton("Submit City", (dialog, which) -> {
                    String input = etCustomCity.getText() != null ? etCustomCity.getText().toString().trim() : "";
                    if (!input.isEmpty()) {
                        final String newCity = capitalizeWords(input);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String userEmail = user != null && user.getEmail() != null ? user.getEmail().trim() : "";
                        boolean isAdmin = ContactAdapter.isAdminEmail(userEmail);

                        if (isAdmin) {
                            java.util.Map<String, Object> cityMap = new java.util.HashMap<>();
                            cityMap.put("name", newCity);
                            cityMap.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                            firestore.collection("public_cities").document(newCity).set(cityMap);

                            selectedCity = newCity;
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_SELECTED_CITY, selectedCity).apply();
                            if (chipCitySelect != null) chipCitySelect.setText("City: " + selectedCity);
                            Toast.makeText(MainActivity.this, "City " + newCity + " published for all users!", Toast.LENGTH_SHORT).show();
                        } else if (user != null) {
                            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

                            firestore.collection("public_cities").get().addOnSuccessListener(publicSnapshots -> {
                                boolean existsInPublic = false;
                                if (publicSnapshots != null && !publicSnapshots.isEmpty()) {
                                    for (DocumentSnapshot doc : publicSnapshots) {
                                        String name = doc.getString("name");
                                        if (name != null && name.equalsIgnoreCase(newCity)) {
                                            existsInPublic = true;
                                            break;
                                        }
                                    }
                                }

                                if (existsInPublic) {
                                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                                    new MaterialAlertDialogBuilder(MainActivity.this)
                                            .setTitle("City Already Exists! 🏙️")
                                            .setMessage("City '" + newCity + "' is already published in the public directory and available in the selection list.")
                                            .setPositiveButton("Got it", null)
                                            .show();
                                    return;
                                }

                                firestore.collection("moderation_requests")
                                        .whereEqualTo("action", "ADD_CITY")
                                        .whereEqualTo("status", "PENDING")
                                        .get()
                                        .addOnSuccessListener(reqSnapshots -> {
                                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                                            boolean pendingExists = false;
                                            if (reqSnapshots != null && !reqSnapshots.isEmpty()) {
                                                for (DocumentSnapshot doc : reqSnapshots) {
                                                    String reqCity = doc.getString("city");
                                                    if (reqCity != null && reqCity.equalsIgnoreCase(newCity)) {
                                                        pendingExists = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            if (pendingExists) {
                                                new MaterialAlertDialogBuilder(MainActivity.this)
                                                        .setTitle("Request Already Sent! ⏳")
                                                        .setMessage("A request to add city '" + newCity + "' has already been sent to the admin and is currently pending review.")
                                                        .setPositiveButton("Got it", null)
                                                        .show();
                                            } else {
                                                java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
                                                requestMap.put("action", "ADD_CITY");
                                                requestMap.put("city", newCity);
                                                requestMap.put("name", newCity);
                                                requestMap.put("uploaderEmail", userEmail);
                                                requestMap.put("uploaderUid", user.getUid());
                                                requestMap.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                                                requestMap.put("status", "PENDING");

                                                firestore.collection("moderation_requests").add(requestMap)
                                                        .addOnSuccessListener(docRef -> {
                                                            new MaterialAlertDialogBuilder(MainActivity.this)
                                                                    .setTitle("City Request Submitted! 🎉")
                                                                    .setMessage("Your request to add city '" + newCity + "' has been submitted successfully.\n\nIt is now waiting for admin approval before showing globally.")
                                                                    .setPositiveButton("Got it", null)
                                                                    .show();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            selectedCity = newCity;
                                                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_SELECTED_CITY, selectedCity).apply();
                                                            if (chipCitySelect != null) chipCitySelect.setText("City: " + selectedCity);
                                                            Toast.makeText(MainActivity.this, "City set locally to " + newCity, Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                                            Toast.makeText(MainActivity.this, "Failed checking pending requests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }).addOnFailureListener(e -> {
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "Failed checking public cities: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            selectedCity = newCity;
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(KEY_SELECTED_CITY, selectedCity).apply();
                            if (chipCitySelect != null) chipCitySelect.setText("City: " + selectedCity);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void deleteContactFromPhonebook(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) return;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("FixitBhai", "WRITE_CONTACTS permission not granted. Skipping system phonebook deletion.");
            return;
        }

        try {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            try (Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.LOOKUP_KEY}, null, null, null)) {
                if (cursor != null) {
                    int deletedCount = 0;
                    int lookupKeyIdx = cursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY);
                    while (cursor.moveToNext()) {
                        if (lookupKeyIdx != -1 && !cursor.isNull(lookupKeyIdx)) {
                            String lookupKey = cursor.getString(lookupKeyIdx);
                            Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                            deletedCount += cr.delete(lookupUri, null, null);
                        }
                    }
                    Log.i("FixitBhai", "Deleted " + deletedCount + " entry/entries from system phonebook for " + phoneNumber);
                }
            }
        } catch (Exception e) {
            Log.e("FixitBhai", "Error deleting contact from system phonebook: " + e.getMessage(), e);
        }
    }

    public void showPrivacyPolicyAgreementDialog(boolean showToast) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setPadding(40, 30, 40, 20);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Privacy Policy & Data Security");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#F8FAFC"));

        TextView tvSubtitle = new TextView(this);
        tvSubtitle.setText("Your personal contacts remain 100% private and secure.");
        tvSubtitle.setTextSize(13);
        tvSubtitle.setTextColor(Color.parseColor("#38BDF8"));
        tvSubtitle.setPadding(0, 6, 0, 16);

        TextView tvBody = new TextView(this);
        tvBody.setLineSpacing(6f, 1f);
        tvBody.setTextSize(13);
        tvBody.setTextColor(Color.parseColor("#CBD5E1"));
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

        new MaterialAlertDialogBuilder(this)
                .setView(scrollView)
                .setCancelable(false)
                .setPositiveButton("I Agree & Accept", (dialog, which) -> {
                    prefs.edit().putBoolean("KEY_PRIVACY_POLICY_ACCEPTED", true).apply();
                    dialog.dismiss();
                    loadSavedState();
                    checkContactPermissionAndScan(showToast);
                })
                .setNegativeButton("Decline & Exit", (dialog, which) -> finishAffinity())
                .show();
    }

    private void restoreUserCloudBackup() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        firestore.collection("users").document(currentUser.getUid()).collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean restoredAny = false;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Contact contact = doc.toObject(Contact.class);
                        if (contact != null && contact.getPhone() != null) {
                            if (!dbHelper.contactExists(contact.getPhone())) {
                                dbHelper.addContact(contact);
                                restoredAny = true;
                            }
                        }
                    }

                    if (restoredAny) {
                        contactList.clear();
                        contactList.addAll(dbHelper.getAllContacts());
                        saveContactsToStorage();
                        if (adapter != null) {
                            adapter.updateData(contactList);
                            adapter.filterBy("", getFilterCity());
                        }
                        refreshCategoryChips();
                        checkEmptyState();
                    }
                });
    }

    private void fetchPublicCloudContactsForMainList(boolean showToast) {
        firestore.collectionGroup("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) return;

                    Set<String> existingPhones = new HashSet<>();
                    for (Contact c : contactList) {
                        if (c.getPhone() != null) {
                            existingPhones.add(c.getPhone().replaceAll("[^0-9]", ""));
                        }
                    }

                    boolean addedNewCloudContacts = false;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Exclude private user backups from public cloud list
                        if (doc.getReference().getPath().contains("/users/") || doc.getReference().getPath().startsWith("users/")) {
                            continue;
                        }

                        Contact cloudContact = doc.toObject(Contact.class);
                        if (cloudContact != null && cloudContact.getPhone() != null) {
                            String cleanPhone = cloudContact.getPhone().replaceAll("[^0-9]", "");
                            if (!existingPhones.contains(cleanPhone)) {
                                existingPhones.add(cleanPhone);
                                cloudContact.setCloud(true);
                                contactList.add(cloudContact);
                                addedNewCloudContacts = true;
                            }
                        }
                    }

                    if (addedNewCloudContacts) {
                        Collections.sort(contactList, (c1, c2) -> {
                            String name1 = c1.getName() != null ? c1.getName() : "";
                            String name2 = c2.getName() != null ? c2.getName() : "";
                            return name1.compareToIgnoreCase(name2);
                        });

                        if (adapter != null) {
                            adapter.updateData(contactList);
                            adapter.filterBy("", getFilterCity());
                        }
                        refreshCategoryChips();
                        checkEmptyState();

                        if (showToast) {
                            Toast.makeText(MainActivity.this, "Loaded shared public cloud directory!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("FixitBhai", "Failed fetching public cloud contacts: " + e.getMessage()));
    }

    private void checkContactPermissionAndScan(boolean showToastIfNoNew) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean privacyAccepted = prefs.getBoolean("KEY_PRIVACY_POLICY_ACCEPTED", false);
        if (!privacyAccepted) {
            showPrivacyPolicyAgreementDialog(showToastIfNoNew);
            return;
        }

        restoreUserCloudBackup();

        if (!prefs.getBoolean(KEY_PREF_SET, false)) {
            showCloudBackupPreferenceDialog(showToastIfNoNew);
            return;
        }
        proceedWithScan(showToastIfNoNew);
    }

    private void showCloudBackupPreferenceDialog(boolean showToastIfNoNew) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cloud Backup Option")
                .setMessage("Would you like to sync discovered service contacts to your personal cloud for cross-device backup, or keep them locally on your device?")
                .setPositiveButton("Backup to Cloud", (dialog, which) -> {
                    saveCloudPreference(true);
                    proceedWithScan(showToastIfNoNew);
                })
                .setNegativeButton("Local Only", (dialog, which) -> {
                    saveCloudPreference(false);
                    proceedWithScan(showToastIfNoNew);
                })
                .setCancelable(false)
                .show();
    }

    private void saveCloudPreference(boolean syncToCloud) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_CLOUD_SYNC_PREF, syncToCloud).putBoolean(KEY_PREF_SET, true).apply();
    }

    private void proceedWithScan(boolean showToastIfNoNew) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            importContactsFromPhone(showToastIfNoNew);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
            }, PERMISSION_REQUEST_READ_CONTACTS);
            refreshCategoryChips();
            adapter.filterBy("", getFilterCity());
            checkEmptyState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted. Scanning contacts...", Toast.LENGTH_SHORT).show();
                importContactsFromPhone(true);
            } else {
                Toast.makeText(this, "Permission denied. Local contacts skipped.", Toast.LENGTH_SHORT).show();
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                refreshCategoryChips();
                adapter.filterBy("", getFilterCity());
                checkEmptyState();
            }
        }
    }

    private void loadSavedState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedCity = prefs.getString(KEY_SELECTED_CITY, "All Cities");

        try {
            List<Contact> dbContacts = dbHelper.getAllContacts();
            if (dbContacts != null && !dbContacts.isEmpty()) {
                contactList.clear();
                contactList.addAll(dbContacts);
            } else {
                String json = prefs.getString(KEY_CONTACT_LIST, null);
                if (json != null) {
                    Type type = new TypeToken<ArrayList<Contact>>() {}.getType();
                    List<Contact> savedList = GSON_INSTANCE.fromJson(json, type);
                    if (savedList != null) {
                        contactList.clear();
                        contactList.addAll(savedList);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading saved state", e);
        }
    }

    private void saveContactsToStorage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_CONTACT_LIST, GSON_INSTANCE.toJson(contactList));
        editor.apply();
    }

    private void refreshCategoryChips() {
        if (chipGroupCategories == null) return;

        chipGroupCategories.setSingleSelection(true);
        chipGroupCategories.setSelectionRequired(false);
        chipGroupCategories.removeAllViews();

        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setChipBackgroundColor(CHIP_BG_COLOR);
        allChip.setTextColor(Color.parseColor("#F8FAFC"));
        allChip.setOnClickListener(v -> {
            hideSoftKeyboard();
            adapter.filterBy("", getFilterCity());
        });
        chipGroupCategories.addView(allChip);

        Chip emergencyChip = new Chip(this);
        emergencyChip.setText("24/7 Emergency");
        emergencyChip.setCheckable(true);
        emergencyChip.setChipIconResource(R.drawable.ic_info);
        emergencyChip.setChipBackgroundColor(CHIP_EMERGENCY_BG_COLOR);
        emergencyChip.setTextColor(Color.parseColor("#818CF8"));
        emergencyChip.setOnClickListener(v -> {
            hideSoftKeyboard();
            adapter.filterBy("24/7 Emergency", getFilterCity());
        });
        chipGroupCategories.addView(emergencyChip);

        Set<String> categories = new LinkedHashSet<>();
        for (Contact contact : contactList) {
            if (contact.getCategory() != null && !contact.getCategory().trim().isEmpty()) {
                categories.add(contact.getCategory().trim());
            }
        }

        for (String categoryName : categories) {
            Chip categoryChip = new Chip(this);
            categoryChip.setText(categoryName);
            categoryChip.setCheckable(true);
            categoryChip.setChipBackgroundColor(CHIP_BG_COLOR);
            categoryChip.setTextColor(Color.parseColor("#CBD5E1"));
            categoryChip.setOnClickListener(v -> {
                hideSoftKeyboard();
                adapter.filterBy(categoryName, getFilterCity());
            });
            chipGroupCategories.addView(categoryChip);
        }
    }

    private void importContactsFromPhone(boolean showToast) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerViewContacts != null) recyclerViewContacts.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean shouldSyncCloud = prefs.getBoolean(KEY_CLOUD_SYNC_PREF, false);
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserId = currentUser != null ? currentUser.getUid() : null;

            String[] serviceKeywords = {
                    "ac repair", "ac service", "ac", "aircon", "air conditioner", "hvac", "chiller",
                    "fridge", "refrigerator", "deep freezer", "cooler", "air cooler", "fan",
                    "ro repair", "ro service", "ro", "water purifier", "aquaguard", "kent ro",
                    "plumber", "plumbing", "borewell", "submersible", "pump", "sump", "water tank",
                    "geyser", "water heater", "solar water heater", "washing machine", "washer", "dryer", "dishwasher", "microwave", "oven",
                    "chimney", "hob", "gas stove", "gas repair", "gas cylinder", "indane", "hp gas", "bharat gas",
                    "mixer", "grinder", "juicer", "induction", "appliance",
                    "electrician", "electrical", "wireman", "inverter", "battery", "ups", "generator",
                    "tv repair", "tv", "television", "led tv", "lcd tv", "dth", "dish tv", "tata play",
                    "set top box", "cable tv", "cable wala", "wifi", "broadband", "internet", "cctv", "camera",
                    "carpenter", "carpentry", "painter", "painting", "mistri", "mistry", "karigar",
                    "mason", "welder", "welding", "glazier", "glass work", "fabricator", "fabrication",
                    "roofer", "roofing", "tiler", "tiles", "marble", "granite", "polish", "pop",
                    "plaster", "contractor", "hardware", "builder", "waterproofing", "interior",
                    "mechanic", "garage", "puncture", "puncture wala", "tyre", "tire", "alignment",
                    "auto", "cab", "driver", "taxi", "crane", "towing", "denter", "dent repair",
                    "painter auto", "wheel alignment", "car service", "bike service", "car wash", "valet",
                    "excavator", "jcb", "loader", "tractor", "cleaner", "cleaning", "housekeeper", "pest control", "disinfection", "sanitize",
                    "septic", "septic tank", "tank cleaner", "sofa cleaning", "carpet cleaning",
                    "maid", "cook", "chef", "gardener", "mali", "laundry", "dhobi", "press wala", "ironing",
                    "milkman", "doodhwala", "paperwala", "newspaper", "waste", "kabra", "scrap", "kadiwala",
                    "security", "guard", "watchman", "technician", "installation", "maintenance",
                    "fitting", "repair", "service", "fix", "helper", "vendor", "supplier", "center", "agency"
            };

            String[] relativeBlacklist = {
                    "chacha", "chachaji", "chachi", "mama", "mamaji", "mami", "bua", "buaji", "fufa", "fufaji", "phoopha",
                    "tau", "tauji", "tai", "taiji", "masi", "masiji", "maasi", "mausa", "mausaji",
                    "bhaiya", "bhai", "bhaya", "didi", "didiji", "di", "dadi", "dadiji", "dada", "dadaji",
                    "nani", "naniji", "nana", "nanaji", "kaka", "kakaji", "kaki", "bhabhi", "bhabhiji",
                    "jija", "jijaji", "saas", "sasur", "sasurji", "devar", "devrani", "nanad", "jeth", "jethani",
                    "sala", "salaji", "sali", "saali", "beta", "beti", "pota", "poti", "natina", "nati",
                    "bhanja", "bhanji", "bhatija", "bhatiji", "samdhi", "samdhan", "uncle", "aunt", "aunty", "cousin", "bro", "brother", "sis", "sister",
                    "mom", "mummy", "mummyji", "dad", "papa", "papaji", "pop", "son", "daughter",
                    "grandma", "grandpa", "mother", "father", "husband", "wife", "hubby", "wifey",
                    "niece", "nephew", "stepdad", "stepmom", "amma", "appa", "baba", "anna", "chechi", "chettan", "akka", "thambi", "macha", "machan"
            };

            String[] majorCities = {
                    "Gurgaon", "Gurugram", "Noida", "Greater Noida", "Ghaziabad", "Faridabad",
                    "Mathura", "Delhi", "New Delhi", "Prayagraj", "Allahabad", "Chennai",
                    "Lucknow", "Mumbai", "Bengaluru", "Bangalore", "Hyderabad", "Kolkata",
                    "Pune", "Ahmedabad", "Jaipur", "Kanpur", "Varanasi", "Chandigarh",
                    "Indore", "Bhopal", "Patna", "Ranchi", "Surat", "Vadodara",
                    "Coimbatore", "Kochi", "Trivandrum", "Visakhapatnam", "Agra", "Meerut"
            };

            // Pre-compile Regex Patterns once to optimize phonebook scanning speed
            Map<String, Pattern> compiledKeywords = new LinkedHashMap<>();
            for (String keyword : serviceKeywords) {
                compiledKeywords.put(keyword, Pattern.compile("(?i).*\\b" + Pattern.quote(keyword) + "\\b.*"));
            }

            Map<String, Pattern> compiledCities = new LinkedHashMap<>();
            for (String knownCity : majorCities) {
                compiledCities.put(knownCity, Pattern.compile("(?i).*\\b" + Pattern.quote(knownCity) + "\\b.*"));
            }

            Set<String> existingNumbers = new HashSet<>();
            Set<String> existingNames = new HashSet<>();
            for (Contact contact : contactList) {
                if (contact.getPhone() != null) existingNumbers.add(normalizePhoneNumber(contact.getPhone()));
                if (contact.getName() != null) existingNames.add(contact.getName().toLowerCase().trim());
            }

            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            int importedCount = 0;
            List<Contact> scannedBatch = new ArrayList<>();

            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    String number = cursor.getString(numberIndex);

                    if (name == null || number == null) continue;
                    String lowerName = name.toLowerCase().trim();
                    String formattedNumber = normalizePhoneNumber(number);

                    if (existingNumbers.contains(formattedNumber) || existingNames.contains(lowerName)) continue;

                    boolean isRelative = false;
                    for (String relative : relativeBlacklist) {
                        if (lowerName.contains(relative)) {
                            isRelative = true;
                            break;
                        }
                    }
                    if (isRelative) continue;

                    String matchedCategory = null;
                    for (Map.Entry<String, Pattern> entry : compiledKeywords.entrySet()) {
                        if (entry.getValue().matcher(lowerName).matches()) {
                            String keyword = entry.getKey();
                            if (keyword.equalsIgnoreCase("ac") || keyword.equalsIgnoreCase("ac repair") || keyword.equalsIgnoreCase("ac service")) {
                                matchedCategory = "AC Repair";
                            } else if (keyword.equalsIgnoreCase("ro") || keyword.equalsIgnoreCase("ro repair") || keyword.equalsIgnoreCase("ro service")) {
                                matchedCategory = "RO Repair";
                            } else if (keyword.equalsIgnoreCase("tv") || keyword.equalsIgnoreCase("tv repair")) {
                                matchedCategory = "TV Repair";
                            } else {
                                matchedCategory = capitalizeWords(keyword);
                            }
                            break;
                        }
                    }

                    if (matchedCategory != null) {
                        String detectedCity = "Unassigned";
                        for (Map.Entry<String, Pattern> entry : compiledCities.entrySet()) {
                            if (entry.getValue().matcher(lowerName).matches()) {
                                String knownCity = entry.getKey();
                                if (knownCity.equalsIgnoreCase("Bangalore")) {
                                    detectedCity = "Bengaluru";
                                } else if (knownCity.equalsIgnoreCase("New Delhi")) {
                                    detectedCity = "Delhi";
                                } else if (knownCity.equalsIgnoreCase("Gurugram")) {
                                    detectedCity = "Gurgaon";
                                } else {
                                    detectedCity = capitalizeWords(knownCity);
                                }
                                break;
                            }
                        }

                        Contact newContact = new Contact(name, formattedNumber, matchedCategory, detectedCity, false);
                        existingNumbers.add(formattedNumber);
                        existingNames.add(lowerName);
                        scannedBatch.add(newContact);
                        importedCount++;

                        if (shouldSyncCloud && currentUserId != null) {
                            String uploaderEmail = currentUser.getEmail() != null && !currentUser.getEmail().isEmpty() ? currentUser.getEmail() : "Verified Member";
                            Contact backupContact = new Contact(name, formattedNumber, matchedCategory, detectedCity, false);
                            backupContact.setUploadedBy(uploaderEmail);
                            backupContact.setUploaderUid(currentUserId);
                            backupContact.setVerified(true);
                            String cleanDocKey = formattedNumber.replaceAll("[^0-9+]", "");
                            firestore.collection("users").document(currentUserId).collection("contacts").document(cleanDocKey).set(backupContact.toMap());
                        }
                    }
                }
                cursor.close();
            }

            if (!scannedBatch.isEmpty()) {
                dbHelper.addContactsBatch(scannedBatch);
            }

            final int finalImportedCount = importedCount;

            handler.post(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                if (finalImportedCount > 0) {
                    contactList.clear();
                    contactList.addAll(dbHelper.getAllContacts());
                    saveContactsToStorage();

                    adapter.updateData(contactList);
                    adapter.filterBy("", getFilterCity());
                    refreshCategoryChips();

                    if (showToast) {
                        String modeMsg = shouldSyncCloud ? " (Local + Backup)" : " (Local Only)";
                        Toast.makeText(MainActivity.this, "Imported " + finalImportedCount + " new contacts" + modeMsg + ".", Toast.LENGTH_SHORT).show();
                    }
                } else if (showToast) {
                    Toast.makeText(MainActivity.this, "Scan Complete! No new local service contacts found.", Toast.LENGTH_SHORT).show();
                }

                adapter.filterBy("", getFilterCity());
                checkEmptyState();
            });

            executor.shutdown();
        });
    }

    private String normalizePhoneNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) return "";
        String digitsOnly = NORMALIZE_PHONE_PATTERN.matcher(rawNumber).replaceAll("");
        if (digitsOnly.length() > 10 && digitsOnly.startsWith("91")) {
            digitsOnly = digitsOnly.substring(digitsOnly.length() - 10);
        }
        if (digitsOnly.length() == 10) {
            return "+91 " + digitsOnly.substring(0, 5) + " " + digitsOnly.substring(5);
        }
        return rawNumber;
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}