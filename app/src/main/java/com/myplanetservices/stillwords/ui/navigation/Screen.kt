package com.myplanetservices.stillwords.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Explore : Screen("explore")
    object Create : Screen("create?quoteId={quoteId}") {
        fun createRoute(quoteId: String? = null): String {
            return if (quoteId != null) "create?quoteId=$quoteId" else "create"
        }
    }
    object Favorites : Screen("favorites")
    object Search : Screen("search")
    object QuoteDetail : Screen("quote_detail/{quoteId}") {
        fun createRoute(quoteId: String): String = "quote_detail/$quoteId"
    }
    object CollectionDetail : Screen("collection_detail/{collectionId}") {
        fun createRoute(collectionId: Long): String = "collection_detail/$collectionId"
    }
    object Settings : Screen("settings")
    object PhotoCredits : Screen("photo_credits")
    object Onboarding : Screen("onboarding")
}

