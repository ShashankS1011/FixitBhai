package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ContactValidationTest {

    @Test
    public void testPhoneFormattingStandard10Digits() {
        String formatted = Contact.formatWithCountryCode("9876543210");
        assertEquals("+919876543210", formatted);
    }

    @Test
    public void testPhoneFormattingWithCountryCodePresent() {
        String formatted = Contact.formatWithCountryCode("+919876543210");
        assertEquals("+919876543210", formatted);
    }

    @Test
    public void testUnassignedCityDefaultFallback() {
        Contact contact = new Contact("Test", "9876543210", "Plumber", "", false);
        assertEquals("Unassigned", contact.getCity());
    }
}