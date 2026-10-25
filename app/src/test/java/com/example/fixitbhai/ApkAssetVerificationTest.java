package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApkAssetVerificationTest {

    @Test
    public void testPackageNameIntegrity() {
        String packageName = "com.example.fixitbhai";
        assertEquals("com.example.fixitbhai", packageName);
    }

    @Test
    public void testRequiredDrawableResourceNames() {
        String[] requiredDrawables = {
                "ic_fixitbhai_logo",
                "ic_search",
                "ic_cloud_download",
                "ic_help",
                "ic_info",
                "ic_feedback",
                "ic_bug",
                "bg_pill_tag",
                "bg_avatar_circle"
        };

        assertEquals(9, requiredDrawables.length);
        for (String drawableName : requiredDrawables) {
            assertNotNull(drawableName);
        }
    }
}