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
                        Contact cloudContact = doc.toObject(Contact.class);
                        if (cloudContact != null && cloudContact.getPhone() != null) {
                            String phone = cloudContact.getPhone();
                            if (!dbHelper.contactExists(phone)) {
                                cloudContact.setCloud(true);
                                dbHelper.addContact(cloudContact);
                                addedCount++;
                            }
                        }
                    }
                    callback.onSyncComplete(addedCount);
                })
                .addOnFailureListener(e -> callback.onSyncComplete(0));
    }
}