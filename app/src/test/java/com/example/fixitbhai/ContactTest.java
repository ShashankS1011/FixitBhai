package com.example.fixitbhai;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContactTest {

    @Test
    public void testFormatWithCountryCode_10DigitNumber() {
        String input = "9876543210";
        String expected = "+919876543210";
        assertEquals(expected, Contact.formatWithCountryCode(input));
    }

    @Test
    public void testFormatWithCountryCode_NumberWithSpacesAndHyphens() {
        String input = "98765-43210";
        String expected = "+919876543210";
        assertEquals(expected, Contact.formatWithCountryCode(input));
    }

    @Test
    public void testFormatWithCountryCode_AlreadyHasPlus91() {
        String input = "+919876543210";
        String expected = "+919876543210";
        assertEquals(expected, Contact.formatWithCountryCode(input));
    }

    @Test
    public void testFormatWithCountryCode_WithLeadingZeros() {
        String input = "09876543210";
        String expected = "+919876543210";
        assertEquals(expected, Contact.formatWithCountryCode(input));
    }

    @Test
    public void testFormatWithCountryCode_12DigitWith91() {
        String input = "919876543210";
        String expected = "+919876543210";
        assertEquals(expected, Contact.formatWithCountryCode(input));
    }

    @Test
    public void testFormatWithCountryCode_NullOrEmpty() {
        assertEquals("", Contact.formatWithCountryCode(null));
        assertEquals("", Contact.formatWithCountryCode("   "));
    }

    @Test
    public void testContactConstructorAndDefaults() {
        Contact contact = new Contact();
        assertEquals("General", contact.getCity());
        assertEquals("", contact.getNotes());
        assertEquals(0.0f, contact.getRating(), 0.001f);
        assertFalse(contact.isDoNotCall());
        assertFalse(contact.isCloud());
    }

    @Test
    public void testContactMutators() {
        Contact contact = new Contact();
        contact.setId("1");
        contact.setName("Suresh Electrician");
        contact.setPhone("9988776655");
        contact.setCategory("Electrician");
        contact.setCity("Lucknow");
        contact.setRating(5.0f);
        contact.setNotes("Expert wiring");
        contact.setDoNotCall(true);
        contact.setCloud(true);

        assertEquals("1", contact.getId());
        assertEquals("Suresh Electrician", contact.getName());
        assertEquals("+919988776655", contact.getPhone());
        assertEquals("Electrician", contact.getCategory());
        assertEquals("Lucknow", contact.getCity());
        assertEquals(5.0f, contact.getRating(), 0.001f);
        assertEquals("Expert wiring", contact.getNotes());
        assertTrue(contact.isDoNotCall());
        assertTrue(contact.isCloud());
    }

    @Test
    public void testContactToMap() {
        Contact contact = new Contact("Ramesh Plumber", "9876543210", "Plumber", "Prayagraj", true);
        contact.setRating(4.5f);
        contact.setNotes("Good service");
        contact.setDoNotCall(false);

        Map<String, Object> map = contact.toMap();
        assertNotNull(map);
        assertEquals("Ramesh Plumber", map.get("name"));
        assertEquals("+919876543210", map.get("phone"));
        assertEquals("Plumber", map.get("category"));
        assertEquals("Prayagraj", map.get("city"));
        assertEquals(4.5f, (Float) map.get("rating"), 0.001f);
        assertEquals("Good service", map.get("notes"));
        assertEquals(Boolean.FALSE, map.get("doNotCall"));
        assertEquals(Boolean.TRUE, map.get("isCloud"));
    }
}