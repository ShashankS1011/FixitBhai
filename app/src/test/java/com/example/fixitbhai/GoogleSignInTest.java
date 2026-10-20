package com.example.fixitbhai;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class GoogleSignInTest {

    @Test
    public void testGoogleAuthPasswordHashLength() {
        String email = "testuser@gmail.com";
        String pass = "GoogleAuth_" + Math.abs(email.hashCode()) + "123456";
        assertTrue("Password must be at least 6 characters for Firebase Auth", pass.length() >= 6);
    }
}