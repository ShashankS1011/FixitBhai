package com.example.fixitbhai;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    public static final String KEY_CLOUD_SYNC_PREF = "CloudSyncEnabled";
    public static final String KEY_PREF_SET = "CloudSyncPrefSet";
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;

    private ContactAdapter adapter;
    private List<Contact> contactList;
    private DatabaseHelper dbHelper;

    private FirebaseFirestore firestore;

    private String selectedCity = "Prayagraj";

    private ChipGroup chipGroupCategories;
    private Chip chipCitySelect;
    private RecyclerView recyclerViewContacts;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.accent_blue));
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.surface_dark));
            swipeRefreshLayout.setOnRefreshListener(() -> syncWithCloudAndPhonebook(true));
        }

        ImageButton btnAbout = findViewById(R.id.btnAbout);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnCloudSync = findViewById(R.id.btnCloudSync);
        FloatingActionButton btnSearch = findViewById(R.id.btnSearch);

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        contactList = new ArrayList<>();

        loadSavedState();

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
                        adapter.filterBy(s != null ? s.toString() : "", selectedCity);
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
                    p.setColor(Color.parseColor("#DC2626"));

                    RectF background;
                    if (dX > 0) {
                        background = new RectF((float) itemView.getLeft(), (float) itemView.getTop() + 12, dX + 24, (float) itemView.getBottom() - 12);
                    } else {
                        background = new RectF((float) itemView.getRight() + dX - 24, (float) itemView.getTop() + 12, (float) itemView.getRight(), (float) itemView.getBottom() - 12);
                    }
                    c.drawRoundRect(background, 32, 32, p);

                    Drawable icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();

                        if (dX > 0) {
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        } else {
                            int iconRight = itemView.getRight() - iconMargin;
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
                Contact deletedContact = contactList.get(position);

                // Remove from UI
                contactList.remove(position);
                adapter.notifyItemRemoved(position);

                // Remove from local database
                dbHelper.deleteContact(deletedContact.getPhone());
                saveContactsToStorage();

                // Remove from user's personal Firestore Backup dynamically using active UID
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    firestore.collection("users").document(currentUser.getUid())
                            .collection("contacts").document(deletedContact.getPhone().replaceAll("[^0-9+]", "")).delete();
                }

                Toast.makeText(MainActivity.this, deletedContact.getName() + " removed from your device", Toast.LENGTH_SHORT).show();
                checkEmptyState();
                refreshCategoryChips();
            }
        };
        new ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(recyclerViewContacts);

        adapter.filterBy("", selectedCity);
        checkEmptyState();

        // Trigger the Combined Sync Process on app launch
        syncWithCloudAndPhonebook(false);

        if (btnAbout != null) btnAbout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
        if (btnLogout != null) btnLogout.setOnClickListener(v -> logoutUser());
        if (btnCloudSync != null) btnCloudSync.setOnClickListener(v -> showCloudContactPicker());

        // Trigger the Combined Sync Process on manual search click
        if (btnSearch != null) btnSearch.setOnClickListener(v -> syncWithCloudAndPhonebook(true));
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
                    adapter.filterBy("", selectedCity);
                }
                refreshCategoryChips();
            }
        }
        checkEmptyState();
    }

    // --- CLOUD CONTACT PICKER LOGIC ---
    public void showCloudContactPicker() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view cloud contacts.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // Fetch shared public cloud directory for the selected city so ALL users see uploaded contacts
        firestore.collection("cities")
                .document(selectedCity)
                .collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    List<Contact> cloudContacts = new ArrayList<>();
                    Set<String> seenPhones = new HashSet<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
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
                    Collections.sort(cloudContacts, (c1, c2) -> {
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
        String[] contactLabels = new String[cloudContacts.size()];
        boolean[] checkedItems = new boolean[cloudContacts.size()];

        for (int i = 0; i < cloudContacts.size(); i++) {
            Contact c = cloudContacts.get(i);
            String uploader = c.getUploadedBy();
            String uploaderText = (uploader != null && !uploader.isEmpty()) ? "\nVerified by: " + uploader : "";
            contactLabels[i] = c.getName() + " (" + c.getCategory() + " • " + c.getCity() + ")\n" + c.getPhone() + uploaderText;
            checkedItems[i] = !dbHelper.contactExists(c.getPhone());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cloud Contacts for " + selectedCity)
                .setMultiChoiceItems(contactLabels, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked)
                .setPositiveButton("Import Selected", (dialog, which) -> {
                    int importedCount = 0;
                    for (int i = 0; i < cloudContacts.size(); i++) {
                        if (checkedItems[i]) {
                            Contact selectedContact = cloudContacts.get(i);
                            dbHelper.addContact(selectedContact);
                            importedCount++;
                        }
                    }

                    if (importedCount > 0) {
                        contactList.clear();
                        contactList.addAll(dbHelper.getAllContacts());
                        saveContactsToStorage();
                        adapter.updateData(contactList);
                        adapter.filterBy("", selectedCity);
                        refreshCategoryChips();
                        checkEmptyState();
                    }

                    Toast.makeText(MainActivity.this, importedCount + " contact(s) saved to device", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- COMBINED SYNC ORCHESTRATOR ---
    private void syncWithCloudAndPhonebook(boolean showToast) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (recyclerViewContacts != null) recyclerViewContacts.setVisibility(View.GONE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);

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
            if (!cityList.contains(c)) {
                cityList.add(c);
            }
        }
        if (!cityList.contains("Prayagraj")) cityList.add("Prayagraj");
        if (!cityList.contains("Chennai")) cityList.add("Chennai");
        if (!cityList.contains("Lucknow")) cityList.add("Lucknow");
        if (!cityList.contains("Delhi")) cityList.add("Delhi");
        if (!cityList.contains("Mumbai")) cityList.add("Mumbai");
        if (!cityList.contains("Bengaluru")) cityList.add("Bengaluru");

        final String[] cityArray = cityList.toArray(new String[0]);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_city_select, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        ListView listView = dialogView.findViewById(R.id.listViewCities);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, cityArray);
        listView.setAdapter(arrayAdapter);

        for (int i = 0; i < cityArray.length; i++) {
            if (cityArray[i].equalsIgnoreCase(selectedCity)) {
                listView.setItemChecked(i, true);
                break;
            }
        }

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedCity = cityArray[position];
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(KEY_SELECTED_CITY, selectedCity).apply();

            if (chipCitySelect != null) chipCitySelect.setText("City: " + selectedCity);

            String filterCity = selectedCity.equalsIgnoreCase("All Cities") ? "" : selectedCity;
            adapter.filterBy("", filterCity);
            checkEmptyState();

            Toast.makeText(MainActivity.this, "Switched filter to: " + selectedCity, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        ImageButton btnCityHelp = dialogView.findViewById(R.id.btnCityHelp);
        if (btnCityHelp != null) {
            btnCityHelp.setOnClickListener(v -> new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("City Selection")
                    .setMessage("FixitBhai dynamically organizes your service contacts by city (e.g. Chennai, Prayagraj, Lucknow, Delhi). Select 'All Cities' to view contacts across all cities at once!")
                    .setPositiveButton("Got It", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show());
        }
        dialog.show();
    }

    private void checkContactPermissionAndScan(boolean showToastIfNoNew) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            importContactsFromPhone(showToastIfNoNew);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
            refreshCategoryChips();
            adapter.filterBy("", selectedCity);
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
                adapter.filterBy("", selectedCity);
                checkEmptyState();
            }
        }
    }

    private void loadSavedState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedCity = prefs.getString(KEY_SELECTED_CITY, "Prayagraj");

        List<Contact> dbContacts = dbHelper.getAllContacts();
        if (!dbContacts.isEmpty()) {
            contactList.clear();
            contactList.addAll(dbContacts);
        } else {
            String json = prefs.getString(KEY_CONTACT_LIST, null);
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Contact>>() {}.getType();
                List<Contact> savedList = gson.fromJson(json, type);
                if (savedList != null) {
                    contactList.clear();
                    contactList.addAll(savedList);
                }
            }
        }
    }

    private void saveContactsToStorage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        editor.putString(KEY_CONTACT_LIST, gson.toJson(contactList));
        editor.apply();
    }

    private void refreshCategoryChips() {
        if (chipGroupCategories == null) return;

        chipGroupCategories.removeAllViews();
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnClickListener(v -> adapter.filterBy("", selectedCity));
        chipGroupCategories.addView(allChip);

        Chip emergencyChip = new Chip(this);
        emergencyChip.setText("24/7 Emergency");
        emergencyChip.setCheckable(true);
        emergencyChip.setChipIconResource(R.drawable.ic_info);
        emergencyChip.setOnClickListener(v -> adapter.filterBy("24/7 Emergency", selectedCity));
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
            categoryChip.setOnClickListener(v -> adapter.filterBy(categoryName, selectedCity));
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

            Set<String> existingNumbers = new HashSet<>();
            Set<String> existingNames = new HashSet<>();
            for (Contact contact : contactList) {
                if (contact.getPhone() != null) existingNumbers.add(normalizePhoneNumber(contact.getPhone()));
                if (contact.getName() != null) existingNames.add(contact.getName().toLowerCase().trim());
            }

            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

            int importedCount = 0;

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
                    for (String keyword : serviceKeywords) {
                        String regex = "(?i).*\\b" + Pattern.quote(keyword) + "\\b.*";
                        if (lowerName.matches(regex)) {
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

            String[] majorCities = {
                    "Chennai", "Prayagraj", "Lucknow", "Delhi", "New Delhi", "Mumbai",
                    "Bengaluru", "Bangalore", "Hyderabad", "Kolkata", "Pune", "Ahmedabad",
                    "Jaipur", "Kanpur", "Varanasi", "Noida", "Gurgaon", "Gurugram",
                    "Chandigarh", "Bhopal", "Indore", "Patna", "Ranchi", "Surat",
                    "Vadodara", "Coimbatore", "Kochi", "Trivandrum", "Visakhapatnam"
            };

            if (matchedCategory != null) {
                String detectedCity = (selectedCity == null || selectedCity.equalsIgnoreCase("All Cities")) ? "Prayagraj" : selectedCity;
                for (String knownCity : majorCities) {
                    String cityRegex = "(?i).*\\b" + Pattern.quote(knownCity) + "\\b.*";
                    if (lowerName.matches(cityRegex)) {
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
                importedCount++;

                dbHelper.addContact(newContact);
                if (shouldSyncCloud && currentUserId != null) {
                    newContact.setCloud(true);
                    String uploaderEmail = currentUser.getEmail() != null && !currentUser.getEmail().isEmpty() ? currentUser.getEmail() : "Verified Member";
                    newContact.setUploadedBy(uploaderEmail);
                    newContact.setUploaderUid(currentUserId);
                    newContact.setVerified(true);
                    String cleanDocKey = formattedNumber.replaceAll("[^0-9+]", "");
                    firestore.collection("cities").document(detectedCity).collection("contacts").document(cleanDocKey).set(newContact.toMap());
                    firestore.collection("users").document(currentUserId).collection("contacts").document(cleanDocKey).set(newContact.toMap());
                }
            }
                }
                cursor.close();
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
                    adapter.filterBy("", selectedCity);
                    refreshCategoryChips();

                    if (showToast) {
                        String modeMsg = shouldSyncCloud ? " (Local + Backup)" : " (Local Only)";
                        Toast.makeText(MainActivity.this, "Imported " + finalImportedCount + " new contacts" + modeMsg + ".", Toast.LENGTH_SHORT).show();
                    }
                } else if (showToast) {
                    Toast.makeText(MainActivity.this, "Scan Complete! No new local service contacts found.", Toast.LENGTH_SHORT).show();
                }

                adapter.filterBy("", selectedCity);
                checkEmptyState();
            });

            executor.shutdown();
        });
    }

    private String normalizePhoneNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) return "";
        String digitsOnly = rawNumber.replaceAll("[^0-9]", "");
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