package com.example.fixitbhai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ContactDiffUtilTest {

    @Test
    public void testContactPrimitiveFieldEqualsOptimization() {
        Contact c1 = new Contact("Sam", "9876543210", "Plumber", "Noida", false);
        c1.setEmergency(true);
        c1.setDoNotCall(false);

        Contact c2 = new Contact("Sam", "9876543210", "Plumber", "Noida", false);
        c2.setEmergency(true);
        c2.setDoNotCall(false);

        Contact c3 = new Contact("Sam", "9876543210", "Plumber", "Noida", false);
        c3.setEmergency(false);
        c3.setDoNotCall(false);

        assertTrue(arePrimitivesEqual(c1, c2));
        assertFalse(arePrimitivesEqual(c1, c3));
    }

    private boolean arePrimitivesEqual(Contact a, Contact b) {
        return a.isEmergency() == b.isEmergency()
                && a.isDoNotCall() == b.isDoNotCall()
                && a.isCloud() == b.isCloud();
    }

    @Test
    public void testContactNotesComparison() {
        Contact c1 = new Contact();
        c1.setNotes("Rate: Rs 500");

        Contact c2 = new Contact();
        c2.setNotes("Rate: Rs 500");

        assertEquals(c1.getNotes(), c2.getNotes());
    }
}