package com.example.fixitbhai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FixitBhai.db";
    // INCREASED VERSION TO 4 for the new is_cloud column
    private static final int DATABASE_VERSION = 4;

    private static final String TABLE_CONTACTS = "contacts";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_CITY = "city";
    private static final String KEY_RATING = "rating";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_DO_NOT_CALL = "do_not_call";
    // NEW COLUMN
    private static final String KEY_IS_CLOUD = "is_cloud";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_PHONE + " TEXT UNIQUE,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_CITY + " TEXT,"
                + KEY_RATING + " REAL DEFAULT 0.0,"
                + KEY_NOTES + " TEXT,"
                + KEY_DO_NOT_CALL + " INTEGER DEFAULT 0,"
                + KEY_IS_CLOUD + " INTEGER DEFAULT 0" + ")"; // Added is_cloud
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_CITY + " TEXT DEFAULT 'Prayagraj'");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_RATING + " REAL DEFAULT 0.0");
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_NOTES + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_DO_NOT_CALL + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 4) {
            // Upgrade for version 4 to add is_cloud
            db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_IS_CLOUD + " INTEGER DEFAULT 0");
        }
    }

    public long addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PHONE, contact.getPhone());
        values.put(KEY_CATEGORY, contact.getCategory());
        values.put(KEY_CITY, contact.getCity());
        values.put(KEY_RATING, contact.getRating());
        values.put(KEY_NOTES, contact.getNotes());
        values.put(KEY_DO_NOT_CALL, contact.isDoNotCall() ? 1 : 0);
        // Save the isCloud state
        values.put(KEY_IS_CLOUD, contact.isCloud() ? 1 : 0);

        long id = db.insertWithOnConflict(TABLE_CONTACTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        // Attach the newly generated SQLite ID back to the object
        if (id != -1) {
            contact.setId(String.valueOf(id));
        }

        db.close();
        return id;
    }

    public void updateContactDetails(String phone, float rating, String notes, boolean doNotCall) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_RATING, rating);
        values.put(KEY_NOTES, notes);
        values.put(KEY_DO_NOT_CALL, doNotCall ? 1 : 0);

        db.update(TABLE_CONTACTS, values, KEY_PHONE + "=?", new String[]{phone});
        db.close();
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " ORDER BY " + KEY_NAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                contactList.add(extractContactFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactList;
    }

    public List<Contact> getContactsByCategoryAndCity(String category, String city) {
        List<Contact> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        boolean filterCategory = category != null && !category.isEmpty() && !category.equalsIgnoreCase("All");
        boolean filterCity = city != null && !city.isEmpty();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_CONTACTS + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (filterCategory) {
            query.append(" AND ").append(KEY_CATEGORY).append("=?");
            args.add(category);
        }

        if (filterCity) {
            query.append(" AND ").append(KEY_CITY).append("=?");
            args.add(city);
        }

        query.append(" ORDER BY ").append(KEY_NAME).append(" ASC");
        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                contactList.add(extractContactFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactList;
    }

    // Helper method to parse database rows safely
    private Contact extractContactFromCursor(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY));

        int cityIdx = cursor.getColumnIndex(KEY_CITY);
        String city = (cityIdx != -1 && !cursor.isNull(cityIdx)) ? cursor.getString(cityIdx) : "Prayagraj";

        int ratingIdx = cursor.getColumnIndex(KEY_RATING);
        float rating = (ratingIdx != -1) ? cursor.getFloat(ratingIdx) : 0.0f;

        int notesIdx = cursor.getColumnIndex(KEY_NOTES);
        String notes = (notesIdx != -1) ? cursor.getString(notesIdx) : "";

        int dncIdx = cursor.getColumnIndex(KEY_DO_NOT_CALL);
        boolean dnc = (dncIdx != -1) && (cursor.getInt(dncIdx) == 1);

        // Extract the isCloud flag
        int isCloudIdx = cursor.getColumnIndex(KEY_IS_CLOUD);
        boolean isCloud = (isCloudIdx != -1) && (cursor.getInt(isCloudIdx) == 1);

        // FIXED: Using the updated 5-parameter constructor
        Contact contact = new Contact(name, phone, category, city, isCloud);

        // Pull the ID from the database and set it on the object
        long dbId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID));
        contact.setId(String.valueOf(dbId));

        contact.setRating(rating);
        contact.setNotes(notes);
        contact.setDoNotCall(dnc);

        return contact;
    }

    public boolean contactExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_ID + " FROM " + TABLE_CONTACTS + " WHERE " + KEY_PHONE + "=?", new String[]{phone});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    public void deleteContact(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_PHONE + "=?", new String[]{phone});
        db.close();
    }
}