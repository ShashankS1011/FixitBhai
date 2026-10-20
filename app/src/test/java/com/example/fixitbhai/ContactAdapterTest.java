package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContactAdapterTest {

    @Test
    public void testIsAdminEmail_ValidAdminEmails() {
        assertTrue(ContactAdapter.isAdminEmail("shashanksingh631@gmail.com"));
        assertTrue(ContactAdapter.isAdminEmail("  SHASHANKKAWAI631@GMAIL.COM "));
        assertTrue(ContactAdapter.isAdminEmail("admin"));
    }

    @Test
    public void testIsAdminEmail_InvalidOrRegularEmails() {
        assertFalse(ContactAdapter.isAdminEmail("regularuser@gmail.com"));
        assertFalse(ContactAdapter.isAdminEmail(""));
        assertFalse(ContactAdapter.isAdminEmail(null));
        assertFalse(ContactAdapter.isAdminEmail("administrator@fixitbhai.com"));
    }
}