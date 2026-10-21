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
    // INCREASED VERSION TO 5 for emergency and review tags
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_CONTACTS = "contacts";
    private static final String TABLE_CLOUD_CACHE = "cloud_cache";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_CITY = "city";
    private static final String KEY_RATING = "rating";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_DO_NOT_CALL = "do_not_call";
    private static final String KEY_IS_CLOUD = "is_cloud";
    private static final String KEY_IS_EMERGENCY = "is_emergency";
    private static final String KEY_REVIEW_TAGS = "review_tags";

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
                + KEY_IS_CLOUD + " INTEGER DEFAULT 0,"
                + KEY_IS_EMERGENCY + " INTEGER DEFAULT 0,"
                + KEY_REVIEW_TAGS + " TEXT DEFAULT ''" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

        String CREATE_CLOUD_CACHE_TABLE = "CREATE TABLE " + TABLE_CLOUD_CACHE + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_PHONE + " TEXT UNIQUE,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_CITY + " TEXT,"
                + KEY_RATING + " REAL DEFAULT 0.0,"
                + KEY_NOTES + " TEXT,"
                + KEY_DO_NOT_CALL + " INTEGER DEFAULT 0,"
                + KEY_IS_CLOUD + " INTEGER DEFAULT 1,"
                + KEY_IS_EMERGENCY + " INTEGER DEFAULT 0,"
                + KEY_REVIEW_TAGS + " TEXT DEFAULT ''" + ")";
        db.execSQL(CREATE_CLOUD_CACHE_TABLE);

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_contacts_city_cat ON " + TABLE_CONTACTS + "(" + KEY_CITY + ", " + KEY_CATEGORY + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_contacts_phone ON " + TABLE_CONTACTS + "(" + KEY_PHONE + ")");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        try {
            db.execSQL("UPDATE " + TABLE_CONTACTS + " SET " + KEY_CITY + " = 'Unassigned' WHERE " + KEY_CITY + " = 'General' OR " + KEY_CITY + " IS NULL OR " + KEY_CITY + " = ''");
            db.execSQL("UPDATE " + TABLE_CLOUD_CACHE + " SET " + KEY_CITY + " = 'Unassigned' WHERE " + KEY_CITY + " = 'General' OR " + KEY_CITY + " IS NULL OR " + KEY_CITY + " = ''");
        } catch (Exception ignored) {}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_CITY + " TEXT DEFAULT 'General'");
            }
            if (oldVersion < 3) {
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_RATING + " REAL DEFAULT 0.0");
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_NOTES + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_DO_NOT_CALL + " INTEGER DEFAULT 0");
            }
            if (oldVersion < 4) {
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_IS_CLOUD + " INTEGER DEFAULT 0");
            }
            if (oldVersion < 5) {
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_IS_EMERGENCY + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + TABLE_CONTACTS + " ADD COLUMN " + KEY_REVIEW_TAGS + " TEXT DEFAULT ''");
            }
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_contacts_city_cat ON " + TABLE_CONTACTS + "(" + KEY_CITY + ", " + KEY_CATEGORY + ")");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_contacts_phone ON " + TABLE_CONTACTS + "(" + KEY_PHONE + ")");
        } catch (Exception ignored) {}
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
        values.put(KEY_IS_EMERGENCY, contact.isEmergency() ? 1 : 0);
        values.put(KEY_REVIEW_TAGS, contact.getReviewTags());

        long id = db.insertWithOnConflict(TABLE_CONTACTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        // Attach the newly generated SQLite ID back to the object
        if (id != -1) {
            contact.setId(String.valueOf(id));
        }

        db.close();
        return id;
    }

    public void updateContactDetails(String phone, float rating, String notes, boolean doNotCall) {
        updateContactDetails(phone, rating, notes, doNotCall, false, "");
    }

    public void updateContactDetails(String phone, float rating, String notes, boolean doNotCall, boolean isEmergency, String reviewTags) {
        updateContactCityAndDetails(phone, null, rating, notes, doNotCall, isEmergency, reviewTags);
    }

    public void updateContactCityAndDetails(String phone, String city, float rating, String notes, boolean doNotCall, boolean isEmergency, String reviewTags) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (city != null && !city.trim().isEmpty()) {
            values.put(KEY_CITY, city.trim());
        }
        values.put(KEY_RATING, rating);
        values.put(KEY_NOTES, notes);
        values.put(KEY_DO_NOT_CALL, doNotCall ? 1 : 0);
        values.put(KEY_IS_EMERGENCY, isEmergency ? 1 : 0);
        values.put(KEY_REVIEW_TAGS, reviewTags != null ? reviewTags : "");

        db.update(TABLE_CONTACTS, values, KEY_PHONE + "=?", new String[]{phone});
        db.close();
    }

    public List<String> getDistinctCities() {
        List<String> cities = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT " + KEY_CITY + " FROM " + TABLE_CONTACTS + " WHERE " + KEY_CITY + " IS NOT NULL AND TRIM(" + KEY_CITY + ") != '' ORDER BY " + KEY_CITY + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                cities.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cities;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " ORDER BY " + KEY_CITY + " ASC, " + KEY_NAME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int nameIdx = cursor.getColumnIndex(KEY_NAME);
            int phoneIdx = cursor.getColumnIndex(KEY_PHONE);
            int catIdx = cursor.getColumnIndex(KEY_CATEGORY);
            int cityIdx = cursor.getColumnIndex(KEY_CITY);
            int ratingIdx = cursor.getColumnIndex(KEY_RATING);
            int notesIdx = cursor.getColumnIndex(KEY_NOTES);
            int dncIdx = cursor.getColumnIndex(KEY_DO_NOT_CALL);
            int isCloudIdx = cursor.getColumnIndex(KEY_IS_CLOUD);
            int emgIdx = cursor.getColumnIndex(KEY_IS_EMERGENCY);
            int revIdx = cursor.getColumnIndex(KEY_REVIEW_TAGS);
            int idIdx = cursor.getColumnIndex(KEY_ID);

            do {
                contactList.add(extractContactFromCursor(cursor, nameIdx, phoneIdx, catIdx, cityIdx, ratingIdx, notesIdx, dncIdx, isCloudIdx, emgIdx, revIdx, idIdx));
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

        query.append(" ORDER BY ").append(KEY_CITY).append(" ASC, ").append(KEY_NAME).append(" ASC");
        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            int nameIdx = cursor.getColumnIndex(KEY_NAME);
            int phoneIdx = cursor.getColumnIndex(KEY_PHONE);
            int catIdx = cursor.getColumnIndex(KEY_CATEGORY);
            int cityIdx = cursor.getColumnIndex(KEY_CITY);
            int ratingIdx = cursor.getColumnIndex(KEY_RATING);
            int notesIdx = cursor.getColumnIndex(KEY_NOTES);
            int dncIdx = cursor.getColumnIndex(KEY_DO_NOT_CALL);
            int isCloudIdx = cursor.getColumnIndex(KEY_IS_CLOUD);
            int emgIdx = cursor.getColumnIndex(KEY_IS_EMERGENCY);
            int revIdx = cursor.getColumnIndex(KEY_REVIEW_TAGS);
            int idIdx = cursor.getColumnIndex(KEY_ID);

            do {
                contactList.add(extractContactFromCursor(cursor, nameIdx, phoneIdx, catIdx, cityIdx, ratingIdx, notesIdx, dncIdx, isCloudIdx, emgIdx, revIdx, idIdx));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactList;
    }

    // Helper method to parse database rows safely using pre-fetched column indices
    private Contact extractContactFromCursor(Cursor cursor) {
        return extractContactFromCursor(cursor,
                cursor.getColumnIndex(KEY_NAME),
                cursor.getColumnIndex(KEY_PHONE),
                cursor.getColumnIndex(KEY_CATEGORY),
                cursor.getColumnIndex(KEY_CITY),
                cursor.getColumnIndex(KEY_RATING),
                cursor.getColumnIndex(KEY_NOTES),
                cursor.getColumnIndex(KEY_DO_NOT_CALL),
                cursor.getColumnIndex(KEY_IS_CLOUD),
                cursor.getColumnIndex(KEY_IS_EMERGENCY),
                cursor.getColumnIndex(KEY_REVIEW_TAGS),
                cursor.getColumnIndex(KEY_ID));
    }

    private Contact extractContactFromCursor(Cursor cursor, int nameIdx, int phoneIdx, int catIdx, int cityIdx, int ratingIdx, int notesIdx, int dncIdx, int isCloudIdx, int emgIdx, int revIdx, int idIdx) {
        String name = (nameIdx != -1 && !cursor.isNull(nameIdx)) ? cursor.getString(nameIdx) : "Unknown";
        String phone = (phoneIdx != -1 && !cursor.isNull(phoneIdx)) ? cursor.getString(phoneIdx) : "";
        String category = (catIdx != -1 && !cursor.isNull(catIdx)) ? cursor.getString(catIdx) : "General";
        String city = (cityIdx != -1 && !cursor.isNull(cityIdx) && !cursor.getString(cityIdx).trim().isEmpty() && !cursor.getString(cityIdx).equalsIgnoreCase("General")) ? cursor.getString(cityIdx) : "Unassigned";
        float rating = (ratingIdx != -1) ? cursor.getFloat(ratingIdx) : 0.0f;
        String notes = (notesIdx != -1 && !cursor.isNull(notesIdx)) ? cursor.getString(notesIdx) : "";
        boolean dnc = (dncIdx != -1) && (cursor.getInt(dncIdx) == 1);
        boolean isCloud = (isCloudIdx != -1) && (cursor.getInt(isCloudIdx) == 1);
        boolean isEmergency = (emgIdx != -1) && (cursor.getInt(emgIdx) == 1);
        String reviewTags = (revIdx != -1 && !cursor.isNull(revIdx)) ? cursor.getString(revIdx) : "";

        Contact contact = new Contact(name, phone, category, city, isCloud);
        if (idIdx != -1 && !cursor.isNull(idIdx)) {
            contact.setId(String.valueOf(cursor.getLong(idIdx)));
        }

        contact.setRating(rating);
        contact.setNotes(notes);
        contact.setDoNotCall(dnc);
        contact.setEmergency(isEmergency);
        contact.setReviewTags(reviewTags);

        return contact;
    }

    public void cacheCloudContact(Contact contact) {
        if (contact == null || contact.getPhone() == null) return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PHONE, contact.getPhone());
        values.put(KEY_CATEGORY, contact.getCategory());
        values.put(KEY_CITY, contact.getCity());
        values.put(KEY_RATING, contact.getRating());
        values.put(KEY_NOTES, contact.getNotes());
        values.put(KEY_DO_NOT_CALL, contact.isDoNotCall() ? 1 : 0);
        values.put(KEY_IS_CLOUD, 1);
        values.put(KEY_IS_EMERGENCY, contact.isEmergency() ? 1 : 0);
        values.put(KEY_REVIEW_TAGS, contact.getReviewTags());
        db.insertWithOnConflict(TABLE_CLOUD_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<Contact> getCachedCloudContacts(String filterCity) {
        List<Contact> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor;
        if (filterCity == null || filterCity.trim().isEmpty() || filterCity.equalsIgnoreCase("All Cities") || filterCity.equalsIgnoreCase("All")) {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CLOUD_CACHE, null);
        } else {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CLOUD_CACHE + " WHERE " + KEY_CITY + " LIKE ?", new String[]{filterCity.trim()});
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(extractContactFromCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return list;
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