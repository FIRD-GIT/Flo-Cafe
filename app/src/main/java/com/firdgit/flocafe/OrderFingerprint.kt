package com.firdgit.flocafe

object OrderFingerprint {
    fun from(lines: List<BasketLine>): String =
        lines.sortedBy { it.item.id }
            .joinToString("|") {
                "${it.item.id}:${it.quantity}:${it.customisation}"
            }
}
