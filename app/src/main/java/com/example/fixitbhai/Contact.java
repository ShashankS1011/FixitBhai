package com.example.fixitbhai;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Contact implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String category;
    private String city;
    private float rating;
    private String notes;
    private boolean doNotCall;

    @Exclude
    private boolean isSelected;

    // Differentiate between Phonebook and Firebase contacts
    private boolean isCloud;

    // Default No-Argument Constructor (Required for Firestore doc.toObject(Contact.class))
    public Contact() {
        this.city = "Prayagraj";
        this.notes = "";
        this.rating = 0.0f;
        this.doNotCall = false;
        this.isSelected = false;
        this.isCloud = false;
    }

    public Contact(String name, String phone, String category, String city, boolean isCloud) {
        this();
        this.name = name;
        this.phone = formatWithCountryCode(phone);
        this.category = category;
        this.city = city != null ? city : "Prayagraj";
        this.isCloud = isCloud;
    }

    // Constructor for database extraction
    public Contact(String id, String name, String phone, String category, String city, float rating, String notes, boolean doNotCall, boolean isCloud) {
        this.id = id;
        this.name = name;
        this.phone = formatWithCountryCode(phone);
        this.category = category;
        this.city = city != null ? city : "Prayagraj";
        this.rating = rating;
        this.notes = notes != null ? notes : "";
        this.doNotCall = doNotCall;
        this.isSelected = false;
        this.isCloud = isCloud;
    }

    public static String formatWithCountryCode(String rawNumber) {
        if (rawNumber == null || rawNumber.trim().isEmpty()) return "";
        String cleaned = rawNumber.replaceAll("[^0-9+]", "");
        if (!cleaned.startsWith("+")) {
            String digitsOnly = cleaned.replaceAll("^0+", "");
            if (digitsOnly.length() == 10) {
                return "+91" + digitsOnly;
            } else if (digitsOnly.startsWith("91") && digitsOnly.length() == 12) {
                return "+" + digitsOnly;
            }
            return "+91" + digitsOnly;
        }
        return cleaned;
    }

    // --- Getters & Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = formatWithCountryCode(phone); }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCity() { return city != null ? city : "Prayagraj"; }
    public void setCity(String city) { this.city = city; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getNotes() { return notes != null ? notes : ""; }
    public void setNotes(String notes) { this.notes = notes; }

    @PropertyName("doNotCall")
    public boolean isDoNotCall() { return doNotCall; }

    @PropertyName("doNotCall")
    public void setDoNotCall(boolean doNotCall) { this.doNotCall = doNotCall; }

    @Exclude
    public boolean isSelected() { return isSelected; }

    @Exclude
    public void setSelected(boolean selected) { isSelected = selected; }

    @PropertyName("isCloud")
    public boolean isCloud() { return isCloud; }

    @PropertyName("isCloud")
    public void setCloud(boolean cloud) { isCloud = cloud; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("phone", getPhone());
        map.put("category", category);
        map.put("city", getCity());
        map.put("rating", rating);
        map.put("notes", getNotes());
        map.put("doNotCall", doNotCall);
        map.put("isCloud", isCloud);
        return map;
    }
}