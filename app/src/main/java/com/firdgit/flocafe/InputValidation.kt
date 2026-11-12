package com.firdgit.flocafe

object InputValidation {
    fun requireEmail(email: String) {
        require(email.isNotBlank()) { "Email is required." }
    }

    fun requireLoginPassword(password: String) {
        require(password.isNotBlank()) { "Password is required." }
    }

    fun requireRegistrationPassword(password: String) {
        require(password.length >= 6) {
            "Password must contain at least 6 characters."
        }
    }
}

object BasketCalculator {
    fun total(lines: List<BasketLine>): Double =
        lines.sumOf { it.item.price * it.quantity }
}
