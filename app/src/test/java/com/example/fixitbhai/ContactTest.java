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
        assertEquals("Unassigned", contact.getCity());
        assertEquals("", contact.getNotes());
        assertEquals(0.0f, contact.getRating(), 0.001f);
        assertFalse(contact.isDoNotCall());
        assertFalse(contact.isCloud());
        assertFalse(contact.isEmergency());
        assertFalse(contact.isVerified());
        assertEquals("", contact.getUploadedBy());
        assertEquals("", contact.getUploaderUid());
        assertEquals("", contact.getReviewTags());
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
        contact.setUploadedBy("admin@fixitbhai.com");
        contact.setUploaderUid("uid12345");
        contact.setVerified(true);
        contact.setEmergency(true);
        contact.setReviewTags("Punctual, Fair Rates");

        assertEquals("1", contact.getId());
        assertEquals("Suresh Electrician", contact.getName());
        assertEquals("+919988776655", contact.getPhone());
        assertEquals("Electrician", contact.getCategory());
        assertEquals("Lucknow", contact.getCity());
        assertEquals(5.0f, contact.getRating(), 0.001f);
        assertEquals("Expert wiring", contact.getNotes());
        assertTrue(contact.isDoNotCall());
        assertTrue(contact.isCloud());
        assertEquals("admin@fixitbhai.com", contact.getUploadedBy());
        assertEquals("uid12345", contact.getUploaderUid());
        assertTrue(contact.isVerified());
        assertTrue(contact.isEmergency());
        assertEquals("Punctual, Fair Rates", contact.getReviewTags());
    }

    @Test
    public void testContactToMap() {
        Contact contact = new Contact("Ramesh Plumber", "9876543210", "Plumber", "Prayagraj", true);
        contact.setRating(4.5f);
        contact.setNotes("Good service");
        contact.setDoNotCall(false);
        contact.setUploadedBy("user@example.com");
        contact.setUploaderUid("uid_abc");
        contact.setVerified(true);
        contact.setEmergency(true);
        contact.setReviewTags("Quick Service");

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
        assertEquals("user@example.com", map.get("uploadedBy"));
        assertEquals("uid_abc", map.get("uploaderUid"));
        assertEquals(Boolean.TRUE, map.get("isVerified"));
        assertEquals(Boolean.TRUE, map.get("isEmergency"));
        assertEquals("Quick Service", map.get("reviewTags"));
    }

    @Test
    public void testLocalContactIsCloudDefaultsToFalse() {
        Contact localContact = new Contact("Local AC Repair", "9876543210", "AC Repair", "Noida", false);
        assertFalse(localContact.isCloud());
        assertEquals("Noida", localContact.getCity());
    }

    @Test
    public void testPrivateUserBackupPathFiltering() {
        String publicCityPath = "cities/Noida/contacts/9876543210";
        String privateUserBackupPath = "users/uid12345/contacts/9876543210";

        assertFalse(isUserBackupPath(publicCityPath));
        assertTrue(isUserBackupPath(privateUserBackupPath));
    }

    private boolean isUserBackupPath(String path) {
        return path != null && (path.contains("/users/") || path.startsWith("users/"));
    }
}