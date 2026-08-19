package com.example.fixitbhai;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactImporter {

    private static final String[] KEYWORDS = {
            "plumber", "electrician", "carpenter", "mistri", "painter",
            "repair", "ac", "mechanic", "washing machine", "technician",
            "welder", "key", "lock", "cleaner", "maid"
    };

    public static int importServiceContacts(Context context, DatabaseHelper db) {
        int importedCount = 0;
        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null, null, null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String phone = cursor.getString(1);

                if (name != null) {
                    String lowerName = name.toLowerCase();

                    for (String keyword : KEYWORDS) {
                        if (lowerName.contains(keyword)) {
                            String category = capitalize(keyword);

                            // Normalize phone number to prevent duplicates
                            String cleanPhone = phone.replaceAll("[^0-9+]", "");

                            if (!db.contactExists(cleanPhone)) {
                                db.addContact(new ServiceContact(name, cleanPhone, category, "Auto-imported"));
                                importedCount++;
                            }
                            break;
                        }
                    }
                }
            }
            cursor.close();
        }

        return importedCount;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}