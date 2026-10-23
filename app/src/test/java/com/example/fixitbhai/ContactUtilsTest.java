package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContactUtilsTest {

    @Test
    public void testNormalizePhoneNumberFormatting() {
        String phone1 = "9876543210";
        String phone2 = "+91 98765 43210";
        String phone3 = "09876543210";

        assertTrue(Contact.formatWithCountryCode(phone1).contains("9876543210"));
        assertTrue(Contact.formatWithCountryCode(phone2).contains("9876543210"));
        assertTrue(Contact.formatWithCountryCode(phone3).contains("9876543210"));
    }

    @Test
    public void testCapitalizeWordsUtility() {
        String input = "ac repair electrician";
        String expected = "Ac Repair Electrician";
        assertEquals(expected, capitalize(input));
    }

    private String capitalize(String str) {
        if (str == null || str.trim().isEmpty()) return "";
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                        .append(w.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}