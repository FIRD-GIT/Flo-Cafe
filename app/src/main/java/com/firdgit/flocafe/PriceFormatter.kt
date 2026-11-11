package com.firdgit.flocafe

import java.util.Locale

object PriceFormatter {
    fun format(amount: Double): String =
        if (amount % 1.0 == 0.0) {
            String.format(Locale.UK, "£%.0f", amount)
        } else {
            String.format(Locale.UK, "£%.2f", amount)
        }
}
