package com.example.fixitbhai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    // Uploader and verification tracking
    private String uploadedBy;
    private String uploaderUid;
    private boolean isVerified;

    // 24/7 Emergency and Community Review Tags
    private boolean isEmergency;
    private String reviewTags;

    // Default No-Argument Constructor (Required for Firestore doc.toObject(Contact.class))
    public Contact() {
        this.city = "Unassigned";
        this.notes = "";
        this.rating = 0.0f;
        this.doNotCall = false;
        this.isSelected = false;
        this.isCloud = false;
        this.uploadedBy = "";
        this.uploaderUid = "";
        this.isVerified = false;
        this.isEmergency = false;
        this.reviewTags = "";
    }

    public Contact(String name, String phone, String category, String city, boolean isCloud) {
        this();
        this.name = name;
        this.phone = formatWithCountryCode(phone);
        this.category = category;
        this.city = (city != null && !city.trim().isEmpty() && !city.equalsIgnoreCase("General")) ? city.trim() : "Unassigned";
        this.isCloud = isCloud;
    }

    // Constructor for database extraction
    public Contact(String id, String name, String phone, String category, String city, float rating, String notes, boolean doNotCall, boolean isCloud) {
        this.id = id;
        this.name = name;
        this.phone = formatWithCountryCode(phone);
        this.category = category;
        this.city = (city != null && !city.trim().isEmpty() && !city.equalsIgnoreCase("General")) ? city.trim() : "Unassigned";
        this.rating = rating;
        this.notes = notes != null ? notes : "";
        this.doNotCall = doNotCall;
        this.isSelected = false;
        this.isCloud = isCloud;
    }

    @NonNull
    public static String formatWithCountryCode(@Nullable String rawNumber) {
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

    @Nullable
    public String getId() { return id; }
    public void setId(@Nullable String id) { this.id = id; }

    @Nullable
    public String getName() { return name; }
    public void setName(@Nullable String name) { this.name = name; }

    @NonNull
    public String getPhone() { return phone != null ? phone : ""; }
    public void setPhone(@Nullable String phone) { this.phone = formatWithCountryCode(phone); }

    @Nullable
    public String getCategory() { return category; }
    public void setCategory(@Nullable String category) { this.category = category; }

    @NonNull
    public String getCity() { return (city != null && !city.trim().isEmpty() && !city.equalsIgnoreCase("General")) ? city.trim() : "Unassigned"; }
    public void setCity(@Nullable String city) { this.city = (city != null && !city.trim().isEmpty() && !city.equalsIgnoreCase("General")) ? city.trim() : "Unassigned"; }

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

    @PropertyName("uploadedBy")
    public String getUploadedBy() { return uploadedBy != null ? uploadedBy : ""; }

    @PropertyName("uploadedBy")
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    @PropertyName("uploaderUid")
    public String getUploaderUid() { return uploaderUid != null ? uploaderUid : ""; }

    @PropertyName("uploaderUid")
    public void setUploaderUid(String uploaderUid) { this.uploaderUid = uploaderUid; }

    @PropertyName("isVerified")
    public boolean isVerified() { return isVerified; }

    @PropertyName("isVerified")
    public void setVerified(boolean verified) { this.isVerified = verified; }

    @PropertyName("isEmergency")
    public boolean isEmergency() { return isEmergency; }

    @PropertyName("isEmergency")
    public void setEmergency(boolean emergency) { this.isEmergency = emergency; }

    @PropertyName("reviewTags")
    public String getReviewTags() { return reviewTags != null ? reviewTags : ""; }

    @PropertyName("reviewTags")
    public void setReviewTags(String reviewTags) { this.reviewTags = reviewTags; }

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
        map.put("uploadedBy", getUploadedBy());
        map.put("uploaderUid", getUploaderUid());
        map.put("isVerified", isVerified());
        map.put("isEmergency", isEmergency());
        map.put("reviewTags", getReviewTags());
        return map;
    }
}