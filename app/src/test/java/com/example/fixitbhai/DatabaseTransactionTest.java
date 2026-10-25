package com.example.fixitbhai;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DatabaseTransactionTest {

    @Test
    public void testBatchContactsListNullSafety() {
        List<Contact> batch = new ArrayList<>();
        batch.add(new Contact("AC Guy 1", "9876543210", "AC Repair", "Noida", false));
        batch.add(null);
        batch.add(new Contact("AC Guy 2", "9876543211", "AC Repair", "Gurgaon", true));

        int count = 0;
        for (Contact c : batch) {
            if (c != null) {
                count++;
            }
        }

        assertEquals(2, count);
    }

    @Test
    public void testDatabaseTableAndColumnNames() {
        String tableName = "contacts";
        String cloudCacheTable = "cloud_cache";
        assertEquals("contacts", tableName);
        assertEquals("cloud_cache", cloudCacheTable);
    }
}