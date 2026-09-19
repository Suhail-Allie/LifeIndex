package com.lifeindex.app.util

object ValidationUtils {

    private val emailPattern =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun isValidEmail(email: String): Boolean {
        return email.trim().matches(emailPattern)
    }

    fun isValidDisplayName(name: String): Boolean {
        return name.trim().length >= 2
    }

    fun isValidNewPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isValidLoginPassword(password: String): Boolean {
        return password.isNotBlank()
    }

    fun isValidTrackerTitle(title: String): Boolean {
        return title.trim().isNotEmpty()
    }

    fun isValidReminderMinutes(value: String): Boolean {
        val minutes = value.toIntOrNull()
        return minutes != null && minutes > 0
    }
}