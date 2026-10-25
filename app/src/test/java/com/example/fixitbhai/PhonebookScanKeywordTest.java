package com.example.fixitbhai;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhonebookScanKeywordTest {

    private static final String[] RELATIVE_BLACKLIST = {
            "chacha", "chachaji", "chachi", "mama", "mamaji", "mami", "bua", "buaji", "fufa",
            "bhaiya", "bhai", "bhaya", "didi", "didiji", "di", "dadi", "dadiji", "dada", "dadaji",
            "uncle", "aunt", "aunty", "cousin", "bro", "brother", "sis", "sister", "mom", "dad"
    };

    @Test
    public void testRelativeBlacklistFilterMatching() {
        String name1 = "Ramesh Chachaji";
        String name2 = "Suresh Electrician";
        String name3 = "Didi Plumber";

        assertTrue(isRelativeName(name1));
        assertFalse(isRelativeName(name2));
        assertTrue(isRelativeName(name3));
    }

    private boolean isRelativeName(String rawName) {
        if (rawName == null) return false;
        String lower = rawName.toLowerCase().trim();
        for (String relative : RELATIVE_BLACKLIST) {
            if (lower.contains(relative)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testServiceKeywordRegexMatching() {
        Pattern acPattern = Pattern.compile("(?i).*\\b(ac|ac repair|ac service)\\b.*");
        Pattern plumberPattern = Pattern.compile("(?i).*\\b(plumber|plumbing)\\b.*");

        assertTrue(acPattern.matcher("John AC Service").matches());
        assertTrue(plumberPattern.matcher("Ramesh Plumber Noida").matches());
        assertFalse(acPattern.matcher("Personal Friend").matches());
    }
}