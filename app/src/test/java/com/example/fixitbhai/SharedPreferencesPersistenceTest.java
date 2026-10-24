package com.example.fixitbhai;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SharedPreferencesPersistenceTest {

    @Test
    public void testPreferenceKeysAndDefaults() {
        assertEquals("FixitBhaiPrefs", MainActivity.PREFS_NAME);
        assertEquals("CloudSyncEnabled", MainActivity.KEY_CLOUD_SYNC_PREF);
        assertEquals("CloudSyncPrefSet", MainActivity.KEY_PREF_SET);
    }

    @Test
    public void testContactListJsonSerialization() {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("Plumber Dave", "9876543210", "Plumber", "Noida", false));
        contacts.add(new Contact("Electrician Sam", "9876543211", "Electrician", "Gurgaon", true));

        String json = gson.toJson(contacts);
        assertNotNull(json);
        assertTrue(json.contains("Plumber Dave"));
        assertTrue(json.contains("Electrician Sam"));

        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<ArrayList<Contact>>() {}.getType();
        List<Contact> restored = gson.fromJson(json, type);
        assertNotNull(restored);
        assertEquals(2, restored.size());
        assertEquals("Plumber Dave", restored.get(0).getName());
        assertEquals("Electrician Sam", restored.get(1).getName());
    }
}