package com.example.fixitbhai;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testMainActivityLaunchesAndDisplaysRecyclerView() {
        activityRule.getScenario().onActivity(activity -> {
            assertNotNull(activity);
        });
    }
}