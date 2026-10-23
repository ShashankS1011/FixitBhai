package com.example.fixitbhai;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CityModerationTest {

    @Test
    public void testCityAdditionRequestMapPayload() {
        String city = "Agra";
        String uploaderEmail = "user@example.com";
        String uploaderUid = "uid_123456";

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("action", "ADD_CITY");
        requestMap.put("city", city);
        requestMap.put("name", city);
        requestMap.put("uploaderEmail", uploaderEmail);
        requestMap.put("uploaderUid", uploaderUid);
        requestMap.put("status", "PENDING");

        assertEquals("ADD_CITY", requestMap.get("action"));
        assertEquals("Agra", requestMap.get("city"));
        assertEquals("user@example.com", requestMap.get("uploaderEmail"));
        assertEquals("PENDING", requestMap.get("status"));
    }

    @Test
    public void testPublicCityMapKeys() {
        Map<String, Object> cityMap = new HashMap<>();
        cityMap.put("name", "Bhopal");
        cityMap.put("createdAt", System.currentTimeMillis());

        assertNotNull(cityMap.get("name"));
        assertEquals("Bhopal", cityMap.get("name"));
        assertTrue(cityMap.containsKey("createdAt"));
    }

    @Test
    public void testCityCapitalizationUtility() {
        String rawCity = "greater noida";
        String formatted = capitalizeWords(rawCity);
        assertEquals("Greater Noida", formatted);
    }

    private String capitalizeWords(String str) {
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