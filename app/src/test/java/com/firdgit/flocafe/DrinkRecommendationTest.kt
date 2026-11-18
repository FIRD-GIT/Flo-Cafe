package com.firdgit.flocafe

import org.junit.Assert.assertEquals
import org.junit.Test

class DrinkRecommendationTest {
    @Test
    fun transparentRuleBasedRecommendationsAreDeterministic() {
        assertEquals(
            "Vanilla Latte",
            DrinkRecommendation.recommend(
                DrinkRecommendation.Preferences("Semi-skimmed", "Vanilla", "Extra sweet"),
            ),
        )
        assertEquals(
            "Oat Caramel Latte",
            DrinkRecommendation.recommend(
                DrinkRecommendation.Preferences("Oat", "Caramel", "Regular"),
            ),
        )
        assertEquals(
            "Black Americano",
            DrinkRecommendation.recommend(
                DrinkRecommendation.Preferences("Semi-skimmed", "No flavour", "Unsweetened"),
            ),
        )
    }

    @Test
    fun unknownPreferencesUseSafeDefault() {
        assertEquals(
            "Classic Cappuccino",
            DrinkRecommendation.recommend(
                DrinkRecommendation.Preferences("Almond", "No flavour", "Regular"),
            ),
        )
    }
}
