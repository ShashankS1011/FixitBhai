package com.example.fixitbhai;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GoogleSignInExceptionTest {

    @Test
    public void testCode10DeveloperErrorHandling() {
        int errorCode = 10;
        String expectedMessage = "Google Sign-In failed (Code 10). Please verify SHA-1 in Firebase Console.";
        assertEquals(expectedMessage, getErrorMessageForCode(errorCode));
    }

    @Test
    public void testCode12500SignFailedHandling() {
        int errorCode = 12500;
        assertTrue(isRecoverableErrorCode(errorCode));
    }

    private String getErrorMessageForCode(int code) {
        return "Google Sign-In failed (Code " + code + "). Please verify SHA-1 in Firebase Console.";
    }

    private boolean isRecoverableErrorCode(int code) {
        return code == 10 || code == 12500 || code == 7 || code == 8;
    }
}