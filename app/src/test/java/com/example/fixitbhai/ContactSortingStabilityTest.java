package com.example.fixitbhai;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ContactSortingStabilityTest {

    @Test
    public void testPrimaryNameAlphabeticalSortingStability() {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("Zuresh Electrician", "9876543210", "Electrician", "Noida", false));
        contacts.add(new Contact("Amit Plumber", "9876543211", "Plumber", "Prayagraj", false));
        contacts.add(new Contact("Bikram AC Repair", "9876543212", "AC Repair", "Delhi", false));

        Collections.sort(contacts, (c1, c2) -> {
            String n1 = c1.getName() != null ? c1.getName() : "";
            String n2 = c2.getName() != null ? c2.getName() : "";
            return n1.compareToIgnoreCase(n2);
        });

        assertEquals("Amit Plumber", contacts.get(0).getName());
        assertEquals("Bikram AC Repair", contacts.get(1).getName());
        assertEquals("Zuresh Electrician", contacts.get(2).getName());

        // Modify city of Amit Plumber from Prayagraj to Gurgaon
        contacts.get(0).setCity("Gurgaon");

        // Re-sort
        Collections.sort(contacts, (c1, c2) -> {
            String n1 = c1.getName() != null ? c1.getName() : "";
            String n2 = c2.getName() != null ? c2.getName() : "";
            return n1.compareToIgnoreCase(n2);
        });

        // Verify index 0 remains Amit Plumber
        assertEquals("Amit Plumber", contacts.get(0).getName());
    }
}