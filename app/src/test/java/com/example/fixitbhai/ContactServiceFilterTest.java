package com.example.fixitbhai;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContactServiceFilterTest {

    @Test
    public void testAlphabeticalAndCitySorting() {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("Vijay Plumber", "9876543210", "Plumber", "Prayagraj", false));
        contacts.add(new Contact("Anand AC Service", "9876543211", "AC Repair", "Lucknow", true));
        contacts.add(new Contact("Balu Electrician", "9876543212", "Electrician", "Prayagraj", false));

        // Sort city ASC, then name ASC
        Collections.sort(contacts, (c1, c2) -> {
            int cityCompare = c1.getCity().compareToIgnoreCase(c2.getCity());
            if (cityCompare != 0) return cityCompare;
            return c1.getName().compareToIgnoreCase(c2.getName());
        });

        assertEquals("Anand AC Service", contacts.get(0).getName());
        assertEquals("Lucknow", contacts.get(0).getCity());

        assertEquals("Balu Electrician", contacts.get(1).getName());
        assertEquals("Prayagraj", contacts.get(1).getCity());

        assertEquals("Vijay Plumber", contacts.get(2).getName());
        assertEquals("Prayagraj", contacts.get(2).getCity());
    }

    @Test
    public void testEmergencyServiceMatching() {
        Contact emergencyPlumber = new Contact("Emergency Plumber 24/7", "9876543210", "Plumber", "Prayagraj", true);
        emergencyPlumber.setEmergency(true);
        emergencyPlumber.setReviewTags("24/7 Available, Night Service");

        Contact regularMechanic = new Contact("Day Mechanic", "9876543211", "Mechanic", "Prayagraj", false);
        regularMechanic.setEmergency(false);

        assertTrue(emergencyPlumber.isEmergency());
        assertTrue(emergencyPlumber.getReviewTags().contains("24/7"));
        assertFalse(regularMechanic.isEmergency());
    }

    @Test
    public void testCityFilteringLogic() {
        Contact chennaiContact = new Contact("Suresh Plumber", "9876543210", "Plumber", "Chennai", false);
        Contact gurgaonContact = new Contact("Ramesh Electrician", "9876543211", "Electrician", "Gurgaon", false);
        Contact generalContact = new Contact("Local Technician", "9876543212", "AC Repair", "General", false);

        List<Contact> allContacts = new ArrayList<>();
        allContacts.add(chennaiContact);
        allContacts.add(gurgaonContact);
        allContacts.add(generalContact);

        // Test filtering for "Gurgaon"
        List<Contact> gurgaonFiltered = filterByCity(allContacts, "Gurgaon");
        assertEquals(1, gurgaonFiltered.size());
        assertEquals("Gurgaon", gurgaonFiltered.get(0).getCity());

        // Test filtering for "Chennai"
        List<Contact> chennaiFiltered = filterByCity(allContacts, "Chennai");
        assertEquals(1, chennaiFiltered.size());
        assertEquals("Chennai", chennaiFiltered.get(0).getCity());

        // Test filtering for "All Cities" (empty filter)
        List<Contact> allFiltered = filterByCity(allContacts, "");
        assertEquals(3, allFiltered.size());
    }

    private List<Contact> filterByCity(List<Contact> list, String cityFilter) {
        List<Contact> result = new ArrayList<>();
        String filter = cityFilter != null ? cityFilter.trim().toLowerCase() : "";
        for (Contact item : list) {
            boolean matchesCity = filter.isEmpty()
                    || filter.equalsIgnoreCase("all")
                    || filter.equalsIgnoreCase("all cities")
                    || (item.getCity() != null && item.getCity().equalsIgnoreCase(filter));
            if (matchesCity) {
                result.add(item);
            }
        }
        return result;
    }
}