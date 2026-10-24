package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PhoneNormalizationEdgeCasesTest {

    @Test
    public void testPhoneNumberEdgeCasesWithCountryCode() {
        assertEquals("+919876543210", Contact.formatWithCountryCode("9876543210"));
        assertEquals("+919876543210", Contact.formatWithCountryCode("+91 98765 43210"));
        assertEquals("+919876543210", Contact.formatWithCountryCode("09876543210"));
        assertEquals("+919876543210", Contact.formatWithCountryCode("919876543210"));
        assertEquals("+919876543210", Contact.formatWithCountryCode("+91-98765-43210"));
    }

    @Test
    public void testEmptyAndNullPhoneNumberFormatting() {
        assertEquals("", Contact.formatWithCountryCode(null));
        assertEquals("", Contact.formatWithCountryCode(""));
        assertEquals("", Contact.formatWithCountryCode("   "));
    }

    @Test
    public void testCityAliasPrayagrajAllahabadMatching() {
        Contact c1 = new Contact("Electrician", "9876543210", "Electrician", "Prayagraj", false);
        Contact c2 = new Contact("Plumber", "9876543210", "Plumber", "Allahabad", false);

        assertTrue(c1.getCity().equalsIgnoreCase("Prayagraj"));
        assertTrue(c2.getCity().equalsIgnoreCase("Allahabad"));
    }
}