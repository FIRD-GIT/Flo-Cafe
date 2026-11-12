package com.firdgit.flocafe

object BasketCalculator {
    fun total(lines: List<BasketLine>): Double =
        lines.sumOf { it.item.price * it.quantity }
}
