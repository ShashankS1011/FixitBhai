package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminRoleTest {

    @Test
    public void testIsAdminEmail_ShashankPrimaryGmails() {
        assertTrue(ContactAdapter.isAdminEmail("shashanksingh631@gmail.com"));
        assertTrue(ContactAdapter.isAdminEmail("SHASHANKSINGH631@GMAIL.COM"));
        assertTrue(ContactAdapter.isAdminEmail(" shashankkawai631@gmail.com "));
    }

    @Test
    public void testIsAdminEmail_AdminStringAlias() {
        assertTrue(ContactAdapter.isAdminEmail("admin"));
        assertTrue(ContactAdapter.isAdminEmail(" ADMIN "));
    }

    @Test
    public void testIsAdminEmail_NonAdminEmails() {
        assertFalse(ContactAdapter.isAdminEmail("user@example.com"));
        assertFalse(ContactAdapter.isAdminEmail("butteryjam@gmail.com"));
        assertFalse(ContactAdapter.isAdminEmail(""));
        assertFalse(ContactAdapter.isAdminEmail(null));
    }
}