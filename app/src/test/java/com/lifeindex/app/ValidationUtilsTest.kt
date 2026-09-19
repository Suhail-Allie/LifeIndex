package com.lifeindex.app

import com.lifeindex.app.util.ValidationUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun validEmailIsAccepted() {
        assertTrue(
            ValidationUtils.isValidEmail(
                "test@lifeindex.com"
            )
        )
    }

    @Test
    fun invalidEmailIsRejected() {
        assertFalse(
            ValidationUtils.isValidEmail(
                "test-lifeindex.com"
            )
        )
    }

    @Test
    fun validDisplayNameIsAccepted() {
        assertTrue(
            ValidationUtils.isValidDisplayName(
                "Test User"
            )
        )
    }

    @Test
    fun blankDisplayNameIsRejected() {
        assertFalse(
            ValidationUtils.isValidDisplayName("")
        )
    }

    @Test
    fun strongNewPasswordIsAccepted() {
        assertTrue(
            ValidationUtils.isValidNewPassword(
                "LifeIndex123"
            )
        )
    }

    @Test
    fun shortNewPasswordIsRejected() {
        assertFalse(
            ValidationUtils.isValidNewPassword(
                "123"
            )
        )
    }

    @Test
    fun blankLoginPasswordIsRejected() {
        assertFalse(
            ValidationUtils.isValidLoginPassword("")
        )
    }

    @Test
    fun validTrackerTitleIsAccepted() {
        assertTrue(
            ValidationUtils.isValidTrackerTitle(
                "Passport"
            )
        )
    }

    @Test
    fun blankTrackerTitleIsRejected() {
        assertFalse(
            ValidationUtils.isValidTrackerTitle("   ")
        )
    }

    @Test
    fun validReminderIsAccepted() {
        assertTrue(
            ValidationUtils.isValidReminderMinutes("1440")
        )
    }

    @Test
    fun invalidReminderIsRejected() {
        assertFalse(
            ValidationUtils.isValidReminderMinutes("0")
        )
    }
}