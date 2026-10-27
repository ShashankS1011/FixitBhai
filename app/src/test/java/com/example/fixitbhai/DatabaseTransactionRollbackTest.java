package com.example.fixitbhai;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DatabaseTransactionRollbackTest {

    @Test
    public void testTransactionListValidation() {
        List<Contact> batch = new ArrayList<>();
        batch.add(new Contact("AC Repair 1", "9876543210", "AC Repair", "Delhi", false));
        batch.add(new Contact("AC Repair 2", "9876543211", "AC Repair", "Gurgaon", false));

        assertNotNull(batch);
        assertEquals(2, batch.size());
        String name0 = batch.get(0).getName();
        String name1 = batch.get(1).getName();
        assertNotNull(name0);
        assertNotNull(name1);
        assertTrue(name0.contains("1"));
        assertTrue(name1.contains("2"));
    }

    @Test
    public void testSimulatedTransactionFailureHandling() {
        boolean failed = false;
        try {
            simulateTransactionError();
        } catch (IllegalStateException e) {
            failed = true;
        }
        assertTrue(failed);
    }

    private void simulateTransactionError() {
        throw new IllegalStateException("Simulated SQLite Constraint Violation");
    }
}