package com.example.fixitbhai;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
    public void testFormatWithCountryCode_NullOrEmpty() {
        assertEquals("", Contact.formatWithCountryCode(null));
        assertEquals("", Contact.formatWithCountryCode("   "));
    }

    @Test
    public void testContactConstructorAndDefaults() {
        Contact contact = new Contact();
        assertEquals("Prayagraj", contact.getCity());
        assertEquals("", contact.getNotes());
        assertEquals(0.0f, contact.getRating(), 0.001f);
        assertFalse(contact.isDoNotCall());
        assertFalse(contact.isCloud());
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