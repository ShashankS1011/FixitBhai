package com.example.fixitbhai;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ContactModelEdgeCasesTest {

    @Test
    public void testPhoneFormattingWithParenthesesAndSpaces() {
        String input = "(98765) 43210";
        String expected = "+919876543210";
        assertEquals(expected, Contact.formatWithCountryCode(input));
    }

    @Test
    public void testPhoneFormattingWithLeadingZeros() {
        String input = "009876543210";
        String expected = "+919876543210";
        assertEquals(expected, Contact.formatWithCountryCode(input));
    }

    @Test
    public void testCityFallbackGeneralToUnassigned() {
        Contact c1 = new Contact("Electrician John", "9876543210", "Electrician", "General", false);
        assertEquals("Unassigned", c1.getCity());

        Contact c2 = new Contact("Plumber Dave", "9876543210", "Plumber", "   ", false);
        assertEquals("Unassigned", c2.getCity());

        Contact c3 = new Contact("AC Guy", "9876543210", "AC Repair", null, false);
        assertEquals("Unassigned", c3.getCity());
    }

    @Test
    public void testCitySetterSanitizesGeneral() {
        Contact contact = new Contact();
        contact.setCity("General");
        assertEquals("Unassigned", contact.getCity());

        contact.setCity("  Noida  ");
        assertEquals("Noida", contact.getCity());
    }

    @Test
    public void testToMapCompleteness() {
        Contact contact = new Contact("Ramesh", "9876543210", "Plumber", "Lucknow", true);
        contact.setRating(4.8f);
        contact.setNotes("Prompt service");
        contact.setEmergency(true);
        contact.setDoNotCall(false);
        contact.setReviewTags("Fair Rates, Verified");

        Map<String, Object> map = contact.toMap();
        assertNotNull(map);
        assertEquals("Ramesh", map.get("name"));
        assertEquals("+919876543210", map.get("phone"));
        assertEquals("Plumber", map.get("category"));
        assertEquals("Lucknow", map.get("city"));
        Object ratingObj = map.get("rating");
        assertNotNull(ratingObj);
        assertEquals(4.8f, ((Float) ratingObj), 0.001f);
        assertEquals("Prompt service", map.get("notes"));
        assertEquals(Boolean.FALSE, map.get("doNotCall"));
        assertEquals(Boolean.TRUE, map.get("isCloud"));
        assertEquals(Boolean.TRUE, map.get("isEmergency"));
        assertEquals("Fair Rates, Verified", map.get("reviewTags"));
    }
}