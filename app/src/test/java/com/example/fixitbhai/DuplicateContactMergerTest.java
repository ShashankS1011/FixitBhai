package com.example.fixitbhai;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DuplicateContactMergerTest {

    @Test
    public void testPhoneGroupingAndNoteMergingLogic() {
        List<Contact> contacts = new ArrayList<>();
        Contact c1 = new Contact("Electrician Sam", "+91 98765 43210", "Electrician", "Noida", false);
        c1.setNotes("Rate: Rs 300");
        c1.setEmergency(true);

        Contact c2 = new Contact("Sam Electrician", "9876543210", "Electrician", "Unassigned", false);
        c2.setNotes("Available weekends");

        contacts.add(c1);
        contacts.add(c2);

        Map<String, List<Contact>> grouped = new HashMap<>();
        for (Contact c : contacts) {
            String rawPhone = c.getPhone();
            String cleanPhone = rawPhone.replaceAll("[^0-9]", "");
            if (cleanPhone.length() >= 10) {
                cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            }
            grouped.computeIfAbsent(cleanPhone, k -> new ArrayList<>()).add(c);
        }

        assertEquals(1, grouped.size());
        assertTrue(grouped.containsKey("9876543210"));

        List<Contact> group = grouped.get("9876543210");
        assertNotNull(group);
        assertEquals(2, group.size());

        Contact primary = group.get(0);
        StringBuilder mergedNotes = new StringBuilder(primary.getNotes());
        for (int i = 1; i < group.size(); i++) {
            Contact dup = group.get(i);
            if (!dup.getNotes().isEmpty() && !mergedNotes.toString().contains(dup.getNotes())) {
                if (!mergedNotes.toString().isEmpty()) mergedNotes.append(" | ");
                mergedNotes.append(dup.getNotes());
            }
        }

        assertEquals("Rate: Rs 300 | Available weekends", mergedNotes.toString());
    }
}