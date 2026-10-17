package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UpdateCheckerTest {

    private boolean isVersionGreater(String remote, String local) {
        if (remote == null || remote.isEmpty() || local == null || local.isEmpty()) return false;
        try {
            String[] rParts = remote.replace("v", "").trim().split("\\.");
            String[] lParts = local.replace("v", "").trim().split("\\.");
            int length = Math.max(rParts.length, lParts.length);
            for (int i = 0; i < length; i++) {
                int rVal = i < rParts.length ? Integer.parseInt(rParts[i].replaceAll("[^0-9]", "")) : 0;
                int lVal = i < lParts.length ? Integer.parseInt(lParts[i].replaceAll("[^0-9]", "")) : 0;
                if (rVal > lVal) return true;
                if (rVal < lVal) return false;
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Test
    public void testVersionComparison_RemoteIsGreater() {
        assertTrue(isVersionGreater("2.9.0", "2.8.0"));
        assertTrue(isVersionGreater("v3.0.0", "2.8.0"));
        assertTrue(isVersionGreater("2.8.1", "2.8.0"));
    }

    @Test
    public void testVersionComparison_VersionsAreEqual() {
        assertFalse(isVersionGreater("2.8.0", "2.8.0"));
        assertFalse(isVersionGreater("v2.8.0", "2.8.0"));
    }

    @Test
    public void testVersionComparison_LocalIsGreater() {
        assertFalse(isVersionGreater("2.7.0", "2.8.0"));
        assertFalse(isVersionGreater("1.5.0", "2.8.0"));
    }

    @Test
    public void testVersionComparison_InvalidInput() {
        assertFalse(isVersionGreater(null, "2.8.0"));
        assertFalse(isVersionGreater("", "2.8.0"));
        assertFalse(isVersionGreater("2.8.0", null));
    }
}