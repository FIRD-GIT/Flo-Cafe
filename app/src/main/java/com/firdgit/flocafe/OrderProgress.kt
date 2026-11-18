package com.firdgit.flocafe

object OrderProgress {
    fun percent(status: String): Int = when (status) {
        "Pending" -> 25
        "Preparing" -> 50
        "Ready for Collection" -> 75
        "Collected" -> 100
        else -> 0
    }
}
