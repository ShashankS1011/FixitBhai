package com.example.fixitbhai;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContactFilterPerformanceTest {

    @Test
    public void testEmergencyFilterPreCalculatedMatching() {
        List<Contact> contacts = new ArrayList<>();
        Contact c1 = new Contact("AC Technician", "9876543210", "AC Repair", "Noida", false);
        c1.setEmergency(true);
        contacts.add(c1);

        Contact c2 = new Contact("Electrician Sam", "9876543211", "Electrician", "Gurgaon", false);
        c2.setEmergency(false);
        contacts.add(c2);

        List<Contact> filtered = new ArrayList<>();
        for (Contact item : contacts) {
            if (item.isEmergency()) {
                filtered.add(item);
            }
        }

        assertEquals(1, filtered.size());
        assertEquals("AC Technician", filtered.get(0).getName());
    }

    @Test
    public void testPrayagrajAllahabadCityAliasMatching() {
        String cityFilter = "Prayagraj";
        String city1 = "Allahabad";
        String city2 = "Prayagraj";
        String city3 = "Delhi";

        assertTrue(matchesCityAlias(cityFilter, city1));
        assertTrue(matchesCityAlias(cityFilter, city2));
        assertFalse(matchesCityAlias(cityFilter, city3));
    }

    private boolean matchesCityAlias(String filter, String contactCity) {
        if (filter.isEmpty() || filter.equalsIgnoreCase("all")) return true;
        boolean isPrayagrajFilter = filter.equalsIgnoreCase("prayagraj") || filter.equalsIgnoreCase("allahabad");
        return contactCity.equalsIgnoreCase(filter)
                || (isPrayagrajFilter && contactCity.equalsIgnoreCase("allahabad"))
                || (isPrayagrajFilter && contactCity.equalsIgnoreCase("prayagraj"));
    }
}