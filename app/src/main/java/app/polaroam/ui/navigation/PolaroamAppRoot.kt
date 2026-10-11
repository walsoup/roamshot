package app.polaroam.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.polaroam.ui.board.QuestBoardScreen
import app.polaroam.ui.capture.CaptureScreen
import app.polaroam.ui.components.PixelChip
import app.polaroam.ui.gallery.GalleryScreen
import app.polaroam.ui.home.HomeScreen
import app.polaroam.ui.skin.pixelFrame
import app.polaroam.ui.stats.StatsScreen
import app.polaroam.ui.theme.PolaroamTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PolaroamAppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTopLevel = backStackEntry?.destination?.topLevelDestination()
    val isImeVisible = WindowInsets.isImeVisible

    Scaffold(
        containerColor = PolaroamTheme.colors.background,
        bottomBar = {
            if (currentTopLevel != null && !isImeVisible) {
                PixelBottomBar(
                    current = currentTopLevel,
                    onSelect = { navController.navigateToTopLevel(it) },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = Modifier.padding(padding),
        ) {
            composable<Route.Home> {
                HomeScreen(onOpenWalk = { walkId -> navController.navigate(Route.QuestBoard(walkId)) })
            }
            composable<Route.QuestBoard> {
                QuestBoardScreen(
                    onQuestSelected = { questId -> navController.navigate(Route.Capture(questId)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Route.Capture> {
                CaptureScreen(onFinished = { navController.popBackStack() })
            }
            composable<Route.Gallery> {
                GalleryScreen()
            }
            composable<Route.Stats> {
                StatsScreen()
            }
        }
    }
}

private fun NavDestination.topLevelDestination(): TopLevelDestination? =
    TopLevelDestination.entries.firstOrNull { destination ->
        hierarchy.any { it.hasRoute(destination.route::class) }
    }

private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun PixelBottomBar(
    current: TopLevelDestination,
    onSelect: (TopLevelDestination) -> Unit,
) {
    val spacing = PolaroamTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pixelFrame(PolaroamTheme.skin.bottomBar)
            .navigationBarsPadding()
            .padding(horizontal = spacing.s, vertical = spacing.s),
        horizontalArrangement = Arrangement.spacedBy(spacing.s),
    ) {
        TopLevelDestination.entries.forEach { destination ->
            PixelChip(
                text = stringResource(destination.label),
                selected = destination == current,
                onClick = { onSelect(destination) },
                icon = destination.icon,
                contentPadding = PaddingValues(horizontal = spacing.xs, vertical = spacing.xs),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
