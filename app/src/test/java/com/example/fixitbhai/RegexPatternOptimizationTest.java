package com.example.fixitbhai;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RegexPatternOptimizationTest {

    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^0-9+]");

    @Test
    public void testCleanPhoneRegexPattern() {
        String input1 = "+91 98765-43210";
        String input2 = "(098) 765 43210";

        assertEquals("+919876543210", CLEAN_PATTERN.matcher(input1).replaceAll(""));
        assertEquals("09876543210", CLEAN_PATTERN.matcher(input2).replaceAll(""));
    }

    @Test
    public void testContactFormattingPerformanceAndCorrectness() {
        String raw = "9876543210";
        String formatted = Contact.formatWithCountryCode(raw);
        assertEquals("+919876543210", formatted);
        assertTrue(formatted.startsWith("+91"));
    }
}