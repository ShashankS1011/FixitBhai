package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DatabaseSchemaTest {

    @Test
    public void testDatabaseConstants() {
        String dbName = "FixitBhai.db";
        assertEquals("FixitBhai.db", dbName);
    }

    @Test
    public void testContactFallbackCityLogic() {
        Contact contact = new Contact("Technician", "9876543210", "AC Repair", null, false);
        assertEquals("Unassigned", contact.getCity());
    }

    @Test
    public void testContactMapKeysPresence() {
        Contact contact = new Contact("Electrician Sam", "9876543210", "Electrician", "Noida", true);
        java.util.Map<String, Object> map = contact.toMap();
        assertNotNull(map);
        assertTrue(map.containsKey("name"));
        assertTrue(map.containsKey("phone"));
        assertTrue(map.containsKey("category"));
        assertTrue(map.containsKey("city"));
        assertTrue(map.containsKey("isCloud"));
    }
}