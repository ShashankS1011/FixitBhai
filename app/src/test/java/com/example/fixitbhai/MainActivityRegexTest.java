package com.example.fixitbhai;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class MainActivityRegexTest {

    private static final Pattern NORMALIZE_PHONE_PATTERN = Pattern.compile("[^0-9]");

    @Test
    public void testNormalizePhoneDigitsOnly() {
        String input1 = "+91 98765-43210";
        String input2 = "09876543210";

        String digits1 = NORMALIZE_PHONE_PATTERN.matcher(input1).replaceAll("");
        String digits2 = NORMALIZE_PHONE_PATTERN.matcher(input2).replaceAll("");

        assertEquals("919876543210", digits1);
        assertEquals("09876543210", digits2);
    }

    @Test
    public void testPhoneNormalizationEdgeCases() {
        String input = "+91 98765 43210";
        String rawDigits = NORMALIZE_PHONE_PATTERN.matcher(input).replaceAll("");
        String cleanDigits = (rawDigits.length() > 10 && rawDigits.startsWith("91")) ? rawDigits.substring(rawDigits.length() - 10) : rawDigits;
        assertEquals("9876543210", cleanDigits);
    }

    @Test
    public void testNullAndBlankInputNormalization() {
        String input1 = "";
        String input2 = "   ";
        assertEquals("", NORMALIZE_PHONE_PATTERN.matcher(input1).replaceAll(""));
        assertEquals("", NORMALIZE_PHONE_PATTERN.matcher(input2).replaceAll(""));
    }
}