package com.varsitycollege.schedulist.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.varsitycollege.schedulist.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for MainActivity
 * This will execute on an Android device or emulator
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivity_launches_successfully() {
        // This test verifies that the MainActivity can be launched without crashing
        activityRule.scenario.onActivity { activity ->
            assert(activity != null)
            assert(!activity.isFinishing)
        }
    }

    @Test
    fun mainActivity_has_valid_title() {
        activityRule.scenario.onActivity { activity ->
            // Verify that the activity has a title
            assert(activity.title != null || activity.supportActionBar?.title != null)
        }
    }
}

