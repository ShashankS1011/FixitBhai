package com.example.fixitbhai;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FixitBhaiPrefs";
    private static final String KEY_CONTACT_LIST = "SavedContacts";
    // Unique identifier for the contact permission request
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;

    private ContactAdapter adapter;
    private List<Contact> contactList;
    private ChipGroup categoryChipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI Views
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageButton btnAbout = findViewById(R.id.btnAbout);
        FloatingActionButton fabImport = findViewById(R.id.fabImport);

        // Initialize contact list
        contactList = new ArrayList<>();

        // 1. Load saved contacts from storage
        loadContactsFromStorage();

        // 2. Setup RecyclerView Adapter
        adapter = new ContactAdapter(contactList);
        recyclerView.setAdapter(adapter);

        // 3. New: Check and request permission before autoscan
        checkContactPermissionAndScan(false);

        // 4. Click Listeners
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            });
        }

        if (fabImport != null) {
            // Trigger permission check and scan when FAB is clicked (showToast=true)
            fabImport.setOnClickListener(v -> checkContactPermissionAndScan(true));
        }
    }

    // --- Permission Handling ---

    /**
     * Checks if READ_CONTACTS permission is granted.
     * If yes, proceeds to scan. If no, requests it from the user.
     */
    private void checkContactPermissionAndScan(boolean showToastIfNoNew) {
        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, we can safely scan
            importContactsFromPhone(showToastIfNoNew);
        } else {
            // Permission is NOT granted, request it from the user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        }
    }

    /**
     * Callback received when the user responds to the permission request dialog.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            // Check if the user granted the permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted! Start the scan. (Use showToast=true here as it's a direct result of user action)
                Toast.makeText(this, "Permission granted. Scanning contacts...", Toast.LENGTH_SHORT).show();
                importContactsFromPhone(true);
            } else {
                // Permission denied. Explain to the user why the feature won't work.
                Toast.makeText(this, "Permission denied. Fixit Bhai cannot import technician contacts without this permission.", Toast.LENGTH_LONG).show();

                // If permission is denied, ensure chips are refreshed (showing only "All" and saved contacts)
                refreshCategoryChips();
            }
        }
    }

    // --- Core Logic ---

    // Loads saved contacts from local storage (SharedPreferences)
    private void loadContactsFromStorage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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

    // Saves contacts permanently to SharedPreferences
    private void saveContactsToStorage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(contactList);

        editor.putString(KEY_CONTACT_LIST, json);
        editor.apply();
    }

    // Rebuilds dynamic category chips from the current contact list
    private void refreshCategoryChips() {
        categoryChipGroup.removeAllViews();

        // "All" Category Chip
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnClickListener(v -> adapter.getFilter().filter((CharSequence) ""));
        categoryChipGroup.addView(allChip);

        // Extract unique service categories
        Set<String> categories = new LinkedHashSet<>();
        for (Contact contact : contactList) {
            if (contact.getCategory() != null && !contact.getCategory().trim().isEmpty()) {
                categories.add(contact.getCategory().trim());
            }
        }

        // Add dynamic category chips
        for (String categoryName : categories) {
            Chip categoryChip = new Chip(this);
            categoryChip.setText(categoryName);
            categoryChip.setCheckable(true);
            categoryChip.setOnClickListener(v -> adapter.getFilter().filter((CharSequence) categoryName));
            categoryChipGroup.addView(categoryChip);
        }
    }

    // Auto-scans contacts and shows them directly on the main screen
    // Note: The priority order in serviceKeywords has been updated. Generic fallbacks are last.
    private void importContactsFromPhone(boolean showToast) {
        String[] serviceKeywords = {
                // HIGH PRIORITY / Specific Services First (Matches "sujeet ac service" to AC, not Service)
                "ac repair", "ac service", "ac", "aircon", "air conditioner",
                "ro repair", "ro service", "ro", "water purifier",
                "tv repair", "tv", "television", "led tv",

                // Basic Trades & Household Technicians
                "plumber", "electrician", "carpenter", "painter", "mistri", "mistry",
                "mason", "welder", "glazier", "fabricator", "roofer", "tiler", "pop",
                "plaster", "contractor", "hardware", "builder",

                // Appliances & Electronics
                "washing machine", "washer", "dryer", "fridge", "refrigerator",
                "appliance", "microwave", "oven", "chimney", "geyser", "water heater",
                "inverter", "battery", "generator", "cooler", "air cooler", "fan", "dishwasher",

                // Transport & Vehicles
                "mechanic", "garage", "puncture", "puncture wala", "tyre", "tire",
                "auto", "cab", "driver", "taxi", "crane", "towing", "denter",
                "painter auto", "wheel alignment", "car service", "bike service",

                // Cleaning, Pest & Sanitation
                "cleaner", "housekeeper", "pest control", "disinfection", "septic",
                "tank cleaner", "sofa cleaning", "carpet cleaning", "maid", "cook",

                // Outdoor & Heavy Work
                "gardener", "mali", "borewell", "pump", "excavator",

                // LOW PRIORITY / Generic Fallbacks (Keep at the very bottom!)
                "technician", "installation", "maintenance", "fitting", "repair", "service", "fix", "helper", "vendor", "supplier"
        };

        String[] relativeBlacklist = {
                // Immediate & Extended Family (Hindi / Regional Terms)
                "chacha", "chachi", "mama", "mami", "bua", "fufa", "tau", "tai",
                "masi", "mausa", "bhaiya", "bhai", "didi", "di", "dadi", "dada",
                "nani", "nana", "kaka", "kaki", "bhabhi", "jija", "jijaji", "saas",
                "sasur", "devar", "nanad", "sala", "sali", "beta", "beti", "pota",
                "poti", "natina", "nati", "bhanja", "bhanji", "bhatija", "bhatiji",

                // English Family Terms
                "uncle", "aunt", "aunty", "cousin", "bro", "brother", "sis",
                "sister", "mom", "mummy", "dad", "papa", "pop", "son", "daughter",
                "grandma", "grandpa", "mother", "father", "husband", "wife", "niece", "nephew"
        };

        // Set up deduplication sets
        Set<String> existingNumbers = new HashSet<>();
        Set<String> existingNames = new HashSet<>();

        for (Contact contact : contactList) {
            if (contact.getPhone() != null) {
                existingNumbers.add(normalizePhoneNumber(contact.getPhone()));
            }
            if (contact.getName() != null) {
                existingNames.add(contact.getName().toLowerCase().trim());
            }
        }

        ContentResolver contentResolver = getContentResolver();

        // Final sanity check before querying
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // This should not happen if called correctly, but prevent crash just in case
            refreshCategoryChips();
            return;
        }

        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null
        );

        int importedCount = 0;

        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);

                if (name == null || number == null) continue;

                String lowerName = name.toLowerCase().trim();
                String cleanNumber = normalizePhoneNumber(number);

                // Skip existing contacts
                if (existingNumbers.contains(cleanNumber) || existingNames.contains(lowerName)) {
                    continue;
                }

                // Skip family relatives
                boolean isRelative = false;
                for (String relative : relativeBlacklist) {
                    if (lowerName.contains(relative)) {
                        isRelative = true;
                        break;
                    }
                }
                if (isRelative) continue;

                // Match service keyword using whole word boundaries (\b)
                String matchedCategory = null;
                for (String keyword : serviceKeywords) {
                    String regex = "(?i).*\\b" + Pattern.quote(keyword) + "\\b.*";
                    if (lowerName.matches(regex)) {
                        // Map short abbreviations to clean category titles
                        if (keyword.equalsIgnoreCase("ac") || keyword.equalsIgnoreCase("ac repair") || keyword.equalsIgnoreCase("ac service")) {
                            matchedCategory = "AC Repair";
                        } else if (keyword.equalsIgnoreCase("ro") || keyword.equalsIgnoreCase("ro repair") || keyword.equalsIgnoreCase("ro service")) {
                            matchedCategory = "RO Repair";
                        } else if (keyword.equalsIgnoreCase("tv") || keyword.equalsIgnoreCase("tv repair")) {
                            matchedCategory = "TV Repair";
                        } else {
                            matchedCategory = capitalizeWords(keyword);
                        }
                        break; // Stop at the first (Highest Priority) match
                    }
                }

                if (matchedCategory != null) {
                    Contact newContact = new Contact(name, number, matchedCategory);
                    contactList.add(0, newContact); // Add to top of the list

                    existingNumbers.add(cleanNumber);
                    existingNames.add(lowerName);

                    importedCount++;
                }
            }
            cursor.close();
        }

        // Sync adapter data backup and update category chips
        adapter.updateData(contactList);
        refreshCategoryChips();

        if (importedCount > 0) {
            saveContactsToStorage();
            if (showToast) {
                Toast.makeText(this, "Imported " + importedCount + " new service contacts.", Toast.LENGTH_SHORT).show();
            }
        } else if (showToast) {
            Toast.makeText(this, "No new service contacts found.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Utilities ---

    private String normalizePhoneNumber(String rawNumber) {
        if (rawNumber == null) return "";
        // Keep only digits
        String digitsOnly = rawNumber.replaceAll("[^0-9]", "");
        // Handle potential country codes by keeping only the last 10 digits
        if (digitsOnly.length() > 10) {
            return digitsOnly.substring(digitsOnly.length() - 10);
        }
        return digitsOnly;
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}