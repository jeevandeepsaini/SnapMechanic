package com.snapmechanic.app.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * TimeValidator — validates whether a selected time falls within the garage's working hours.
 *
 * Example:
 *   openTime  = "09:00"
 *   closeTime = "18:00"
 *   selected  = "14:30" → valid ✓
 *   selected  = "20:00" → invalid ✗
 */
object TimeValidator {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    /**
     * Result of a validation check.
     * isValid = true if time is acceptable
     * message = human-readable explanation (shown to user on failure)
     */
    data class ValidationResult(val isValid: Boolean, val message: String = "")

    /**
     * Checks if selectedTime is within [openTime, closeTime] inclusive.
     * All times in 24-hour "HH:mm" format.
     *
     * Returns ValidationResult with isValid=false and an error message if invalid.
     * Returns ValidationResult(true) when time is acceptable.
     *
     * If openTime/closeTime are empty (API didn't provide them), we skip validation.
     */
    fun isWithinWorkingHours(
        selectedTime: String,
        openTime: String,
        closeTime: String
    ): ValidationResult {
        // If garage didn't provide working hours, allow any time
        if (openTime.isEmpty() || closeTime.isEmpty()) {
            return ValidationResult(isValid = true)
        }

        return try {
            val selected = timeFormat.parse(selectedTime)
                ?: return ValidationResult(false, "Invalid time format.")
            val open = timeFormat.parse(openTime)
                ?: return ValidationResult(true) // Can't parse → allow
            val close = timeFormat.parse(closeTime)
                ?: return ValidationResult(true)

            val withinHours = !selected.before(open) && !selected.after(close)
            if (withinHours) {
                ValidationResult(isValid = true)
            } else {
                ValidationResult(
                    isValid = false,
                    message = "Garage is open from $openTime to $closeTime. Please select a time within these hours."
                )
            }
        } catch (e: Exception) {
            // Parsing error — allow the time and don't block the user
            ValidationResult(isValid = true)
        }
    }

    /**
     * Returns true if the selected date is today or in the future
     * dateString format: "dd/MM/yyyy"
     */
    fun isDateValid(dateString: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selected = dateFormat.parse(dateString) ?: return false
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            !selected.before(today)
        } catch (e: Exception) {
            false
        }
    }
}
