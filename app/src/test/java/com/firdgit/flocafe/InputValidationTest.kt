package com.firdgit.flocafe

import org.junit.Assert.assertEquals
import org.junit.Test

class InputValidationTest {
    @Test
    fun blankEmailIsRejected() {
        val error = runCatching { InputValidation.requireEmail("  ") }.exceptionOrNull()
        assertEquals("Email is required.", error?.message)
    }

    @Test
    fun blankLoginPasswordIsRejected() {
        val error = runCatching { InputValidation.requireLoginPassword("") }.exceptionOrNull()
        assertEquals("Password is required.", error?.message)
    }

    @Test
    fun shortRegistrationPasswordIsRejected() {
        val error = runCatching { InputValidation.requireRegistrationPassword("12345") }
            .exceptionOrNull()
        assertEquals("Password must contain at least 6 characters.", error?.message)
    }
}
