package app.polaroam.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import app.polaroam.R
import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data class QuestBoard(val walkId: String) : Route {
        companion object {
            const val ARG_WALK_ID = "walkId"
        }
    }

    @Serializable
    data class Capture(val questId: String) : Route {
        companion object {
            const val ARG_QUEST_ID = "questId"
        }
    }

    @Serializable
    data object Gallery : Route

    @Serializable
    data object Stats : Route
}

enum class TopLevelDestination(val route: Route, @StringRes val label: Int, @DrawableRes val icon: Int) {
    HOME(Route.Home, R.string.nav_home, R.drawable.icon_nav_walk),
    GALLERY(Route.Gallery, R.string.nav_gallery, R.drawable.icon_nav_gallery),
    STATS(Route.Stats, R.string.nav_stats, R.drawable.icon_nav_stats),
}
