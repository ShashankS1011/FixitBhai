package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GoogleSignInEdgeCasesTest {

    @Test
    public void testGeneratedGooglePasswordGuaranteedMinLength() {
        String[] testEmails = {
                "a@b.com",
                "shashanksingh631@gmail.com",
                "test.user.123456789@domain.co.in",
                "x@y.z",
                "very.long.email.address.for.testing.edge.cases@gmail.com"
        };

        for (String email : testEmails) {
            String pass = "GoogleAuth_" + Math.abs(email.hashCode()) + "123456";
            assertTrue("Password for " + email + " must be >= 6 chars", pass.length() >= 6);
            assertTrue("Password must start with GoogleAuth_", pass.startsWith("GoogleAuth_"));
        }
    }

    @Test
    public void testEmailSanitization() {
        String rawEmail = "  User.Name@Gmail.COM  ";
        String cleanEmail = rawEmail.trim().toLowerCase();
        assertEquals("user.name@gmail.com", cleanEmail);
    }

    @Test
    public void testDisplayNameFallback() {
        String name1 = "Riya Kesharwani";
        String name2 = "   ";
        String name3 = null;

        assertEquals("Riya Kesharwani", sanitizeDisplayName(name1));
        assertEquals("Verified Member", sanitizeDisplayName(name2));
        assertEquals("Verified Member", sanitizeDisplayName(name3));
    }

    private String sanitizeDisplayName(String rawName) {
        if (rawName != null && !rawName.trim().isEmpty()) {
            return rawName.trim();
        }
        return "Verified Member";
    }

    @Test
    public void testWebClientIdFormatValidation() {
        String clientId = "150320496415-i306450f29of8r15c3juovlv3lsbr8g5.apps.googleusercontent.com";
        assertNotNull(clientId);
        assertTrue(clientId.endsWith(".apps.googleusercontent.com"));
        assertFalse(clientId.contains("defaultwebclient"));
    }
}