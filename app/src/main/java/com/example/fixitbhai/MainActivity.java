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
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;

    private ContactAdapter adapter;
    private List<Contact> contactList;

    // UI View References strictly matched to XML
    private ChipGroup chipGroupCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Bind UI Views according to XML ID attributes
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        RecyclerView recyclerViewContacts = findViewById(R.id.recyclerViewContacts);
        ImageButton btnAbout = findViewById(R.id.btnAbout);
        FloatingActionButton btnSearch = findViewById(R.id.btnSearch);

        // Configure RecyclerView
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));

        // Initialize contact dataset
        contactList = new ArrayList<>();

        // 2. Load cached contacts from local storage
        loadContactsFromStorage();

        // 3. Setup RecyclerView Adapter
        adapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(adapter);

        // 4. Check permissions and scan contacts on initial load
        checkContactPermissionAndScan(false);

        // 5. Setup Click Listeners
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            });
        }

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> checkContactPermissionAndScan(true));
        }
    }

    // --- Permission Handling ---

    private void checkContactPermissionAndScan(boolean showToastIfNoNew) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            importContactsFromPhone(showToastIfNoNew);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACTS);
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
                Toast.makeText(this, "Permission denied. Cannot import contacts without permission.", Toast.LENGTH_LONG).show();
                refreshCategoryChips();
            }
        }
    }

    // --- Storage & Dynamic UI Logic ---

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

    private void saveContactsToStorage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(contactList);

        editor.putString(KEY_CONTACT_LIST, json);
        editor.apply();
    }

    private void refreshCategoryChips() {
        chipGroupCategories.removeAllViews();

        // Default "All" Category Chip
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnClickListener(v -> adapter.getFilter().filter(""));
        chipGroupCategories.addView(allChip);

        // Get unique categories from current contact dataset
        Set<String> categories = new LinkedHashSet<>();
        for (Contact contact : contactList) {
            if (contact.getCategory() != null && !contact.getCategory().trim().isEmpty()) {
                categories.add(contact.getCategory().trim());
            }
        }

        // Dynamically append chip items to ChipGroup
        for (String categoryName : categories) {
            Chip categoryChip = new Chip(this);
            categoryChip.setText(categoryName);
            categoryChip.setCheckable(true);
            categoryChip.setOnClickListener(v -> adapter.getFilter().filter(categoryName));
            chipGroupCategories.addView(categoryChip);
        }
    }

    private void importContactsFromPhone(boolean showToast) {
        String[] serviceKeywords = {
                // High Priority Trades
                "ac repair", "ac service", "ac", "aircon", "air conditioner",
                "ro repair", "ro service", "ro", "water purifier",
                "tv repair", "tv", "television", "led tv",

                // Basic Trades
                "plumber", "electrician", "carpenter", "painter", "mistri", "mistry",
                "mason", "welder", "glazier", "fabricator", "roofer", "tiler", "pop",
                "plaster", "contractor", "hardware", "builder",

                // Home Appliances
                "washing machine", "washer", "dryer", "fridge", "refrigerator",
                "appliance", "microwave", "oven", "chimney", "geyser", "water heater",
                "inverter", "battery", "generator", "cooler", "air cooler", "fan", "dishwasher",

                // Automotive Services
                "mechanic", "garage", "puncture", "puncture wala", "tyre", "tire",
                "auto", "cab", "driver", "taxi", "crane", "towing", "denter",
                "painter auto", "wheel alignment", "car service", "bike service",

                // Sanitation & Maintenance
                "cleaner", "housekeeper", "pest control", "disinfection", "septic",
                "tank cleaner", "sofa cleaning", "carpet cleaning", "maid", "cook",

                // Machinery & Borewell
                "gardener", "mali", "borewell", "pump", "excavator",

                // Generic Fallbacks
                "technician", "installation", "maintenance", "fitting", "repair", "service", "fix", "helper", "vendor", "supplier"
        };

        String[] relativeBlacklist = {
                "chacha", "chachi", "mama", "mami", "bua", "fufa", "tau", "tai",
                "masi", "mausa", "bhaiya", "bhai", "didi", "di", "dadi", "dada",
                "nani", "nana", "kaka", "kaki", "bhabhi", "jija", "jijaji", "saas",
                "sasur", "devar", "nanad", "sala", "sali", "beta", "beti", "pota",
                "poti", "natina", "nati", "bhanja", "bhanji", "bhatija", "bhatiji",
                "uncle", "aunt", "aunty", "cousin", "bro", "brother", "sis",
                "sister", "mom", "mummy", "dad", "papa", "pop", "son", "daughter",
                "grandma", "grandpa", "mother", "father", "husband", "wife", "niece", "nephew"
        };

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
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

                if (existingNumbers.contains(cleanNumber) || existingNames.contains(lowerName)) {
                    continue;
                }

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

                if (matchedCategory != null) {
                    Contact newContact = new Contact(name, number, matchedCategory);
                    contactList.add(0, newContact);

                    existingNumbers.add(cleanNumber);
                    existingNames.add(lowerName);

                    importedCount++;
                }
            }
            cursor.close();
        }

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

    // --- Helper Methods ---

    private String normalizePhoneNumber(String rawNumber) {
        if (rawNumber == null) return "";
        String digitsOnly = rawNumber.replaceAll("[^0-9]", "");
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