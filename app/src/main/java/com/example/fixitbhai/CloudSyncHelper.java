package com.example.fixitbhai;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CloudSyncHelper {

    public interface SyncCallback {
        void onSyncComplete(int newCloudContacts);
    }

    public static void fetchCloudContacts(String city, DatabaseHelper dbHelper, SyncCallback callback) {
        FirebaseFirestore.getInstance()
                .collection("cities")
                .document(city)
                .collection("contacts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int addedCount = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String category = doc.getString("category");

                        if (phone != null && !dbHelper.contactExists(phone)) {
                            // Notice we set isCloud to TRUE here
                            Contact cloudContact = new Contact(name, phone, category, city, true);

                            if (doc.contains("rating")) {
                                Double rating = doc.getDouble("rating");
                                if (rating != null) cloudContact.setRating(rating.floatValue());
                            }

                            dbHelper.addContact(cloudContact);
                            addedCount++;
                        }
                    }
                    callback.onSyncComplete(addedCount);
                })
                .addOnFailureListener(e -> callback.onSyncComplete(0));
    }
}