package com.snapmechanic.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TimeValidator — checks that service booking time validation works correctly.
 *
 * These are pure JVM tests (no Android device needed) because TimeValidator
 * only does string/number comparisons, no Android APIs.
 *
 * Run with: ./gradlew test
 */
class TimeValidatorTest {

    @Test
    fun `valid time within working hours returns isValid true`() {
        val result = TimeValidator.isWithinWorkingHours("14:00", "09:00", "18:00")
        assertTrue("Expected valid time to pass", result.isValid)
    }

    @Test
    fun `time exactly at open time is valid`() {
        val result = TimeValidator.isWithinWorkingHours("09:00", "09:00", "18:00")
        assertTrue("Opening time should be valid", result.isValid)
    }

    @Test
    fun `time exactly at close time is valid`() {
        val result = TimeValidator.isWithinWorkingHours("18:00", "09:00", "18:00")
        assertTrue("Closing time should be valid", result.isValid)
    }

    @Test
    fun `time before opening hours returns isValid false`() {
        val result = TimeValidator.isWithinWorkingHours("07:00", "09:00", "18:00")
        assertFalse("Time before opening should fail", result.isValid)
    }

    @Test
    fun `time after closing hours returns isValid false`() {
        val result = TimeValidator.isWithinWorkingHours("20:00", "09:00", "18:00")
        assertFalse("Time after closing should fail", result.isValid)
    }

    @Test
    fun `error message contains garage hours info`() {
        val result = TimeValidator.isWithinWorkingHours("07:00", "09:00", "18:00")
        assertFalse(result.isValid)
        assertTrue("Error message should mention hours",
            result.message.contains("09:00") && result.message.contains("18:00"))
    }

    @Test
    fun `midnight time handled correctly`() {
        val result = TimeValidator.isWithinWorkingHours("00:00", "09:00", "18:00")
        assertFalse("Midnight should be outside working hours", result.isValid)
    }

    @Test
    fun `empty working hours string allows any time`() {
        // If the API doesn't provide working hours, we should not block the user
        val result = TimeValidator.isWithinWorkingHours("10:00", "", "")
        assertTrue("Empty hours should allow any time", result.isValid)
    }
}
