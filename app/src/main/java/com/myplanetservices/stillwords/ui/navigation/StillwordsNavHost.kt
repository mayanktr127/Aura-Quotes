package com.myplanetservices.stillwords.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.myplanetservices.stillwords.data.preferences.UserPreferences
import com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import com.myplanetservices.stillwords.notification.DailyReminderManager
import com.myplanetservices.stillwords.ui.screens.collection.CollectionDetailScreen
import com.myplanetservices.stillwords.ui.screens.collection.CollectionDetailViewModel
import com.myplanetservices.stillwords.ui.screens.credits.PhotoCreditsScreen
import com.myplanetservices.stillwords.ui.screens.designer.QuoteDesignerScreen
import com.myplanetservices.stillwords.ui.screens.designer.QuoteDesignerViewModel
import com.myplanetservices.stillwords.ui.screens.detail.QuoteDetailScreen
import com.myplanetservices.stillwords.ui.screens.detail.QuoteDetailViewModel
import com.myplanetservices.stillwords.ui.screens.explore.ExploreScreen
import com.myplanetservices.stillwords.ui.screens.explore.ExploreViewModel
import com.myplanetservices.stillwords.ui.screens.favorites.FavoritesScreen
import com.myplanetservices.stillwords.ui.screens.favorites.FavoritesViewModel
import com.myplanetservices.stillwords.ui.screens.home.HomeScreen
import com.myplanetservices.stillwords.ui.screens.home.HomeViewModel
import com.myplanetservices.stillwords.ui.screens.onboarding.OnboardingScreen
import com.myplanetservices.stillwords.ui.screens.read.ReadScreen
import com.myplanetservices.stillwords.ui.screens.read.ReadViewModel
import com.myplanetservices.stillwords.ui.screens.search.SearchScreen
import com.myplanetservices.stillwords.ui.screens.search.SearchViewModel
import com.myplanetservices.stillwords.ui.screens.settings.SettingsScreen
import com.myplanetservices.stillwords.ui.screens.settings.SettingsViewModel
import com.myplanetservices.stillwords.ui.theme.TabLabelStyle

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Today", Screen.Home.route, Icons.Filled.WbSunny, Icons.Outlined.WbSunny),
    BottomNavItem("Explore", Screen.Explore.route, Icons.Filled.Explore, Icons.Outlined.Explore),
    BottomNavItem("Create", Screen.Create.createRoute(), Icons.Filled.AddBox, Icons.Outlined.AddBox),
    BottomNavItem("Saved", Screen.Favorites.route, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder)
)

@Composable
fun StillwordsApp(
    quoteRepository: QuoteRepository,
    preferencesRepository: UserPreferencesRepository,
    reminderManager: DailyReminderManager,
    userPreferences: UserPreferences,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val appContext = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if bottom bar should be visible (exactly the 4 main tabs)
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Create.route,
        "create",
        Screen.Favorites.route
    )

    val startDestination = remember {
        if (userPreferences.onboardingCompleted) {
            Screen.Home.route
        } else {
            Screen.Onboarding.route
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen && showBottomBar) {
            // Adaptive Navigation Rail for Tablets / Wide screens
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(80.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute?.startsWith(item.route.split("?").first()) == true
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title, style = TabLabelStyle) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AppNavigationHost(
                        navController = navController,
                        startDestination = startDestination,
                        quoteRepository = quoteRepository,
                        preferencesRepository = preferencesRepository,
                        reminderManager = reminderManager
                    )
                }
            }
        } else {
            // Compact Phone Layout with Styled 4-Tab Bottom Navigation Bar
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (showBottomBar) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bottom_nav_bar"),
                            color = MaterialTheme.colorScheme.background,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .height(76.dp)
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                bottomNavItems.forEach { item ->
                                    val selected = currentRoute?.startsWith(item.route.split("?").first()) == true
                                    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    val textStyle = TabLabelStyle.copy(
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = tint
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            tint = tint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.title,
                                            style = textStyle,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    AppNavigationHost(
                        navController = navController,
                        startDestination = startDestination,
                        quoteRepository = quoteRepository,
                        preferencesRepository = preferencesRepository,
                        reminderManager = reminderManager
                    )
                }
            }
        }
    }
}

@Composable
private fun AppNavigationHost(
    navController: NavHostController,
    startDestination: String,
    quoteRepository: QuoteRepository,
    preferencesRepository: UserPreferencesRepository,
    reminderManager: DailyReminderManager
) {
    val appContext = LocalContext.current.applicationContext
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(180)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(180)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                preferencesRepository = preferencesRepository,
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = HomeViewModel.Factory(quoteRepository, preferencesRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRead = { initialQuoteId, categories ->
                    navController.navigate(Screen.Read.createRoute(initialQuoteId, categories))
                },
                onNavigateToDetail = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                },
                onNavigateToDesigner = { quoteId ->
                    navController.navigate(Screen.Create.createRoute(quoteId))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToCategory = { category ->
                    navController.navigate(Screen.Explore.route)
                },
                onNavigateToSaved = {
                    navController.navigate(Screen.Favorites.route)
                }
            )
        }

        // Read feed — docs/DESIGN_SPEC.md Sec. 6 (Screen 5)
        composable(
            route = Screen.Read.route,
            arguments = listOf(
                navArgument("initialQuoteId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("categories") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialQuoteId = backStackEntry.arguments?.getString("initialQuoteId")
            val categoriesParam = backStackEntry.arguments?.getString("categories")
            val categoriesList = categoriesParam?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            val readViewModel: ReadViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = ReadViewModel.Factory(initialQuoteId, categoriesList, quoteRepository, preferencesRepository)
            )
            ReadScreen(
                viewModel = readViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDesigner = { quoteId ->
                    navController.navigate(Screen.Create.createRoute(quoteId))
                }
            )
        }

        // Explore
        composable(Screen.Explore.route) {
            val exploreViewModel: ExploreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = ExploreViewModel.Factory(quoteRepository)
            )
            ExploreScreen(
                viewModel = exploreViewModel,
                onNavigateToDetail = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                },
                onNavigateToDesigner = { quoteId ->
                    navController.navigate(Screen.Create.createRoute(quoteId))
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        // Create / Designer
        composable(
            route = Screen.Create.route,
            arguments = listOf(
                navArgument("quoteId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getString("quoteId")
            val designerViewModel: QuoteDesignerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = QuoteDesignerViewModel.Factory(appContext, quoteId, quoteRepository)
            )
            QuoteDesignerScreen(
                viewModel = designerViewModel,
                onNavigateBack = if (navController.previousBackStackEntry != null) {
                    { navController.popBackStack() }
                } else null
            )
        }

        // Favorites
        composable(Screen.Favorites.route) {
            val favoritesViewModel: FavoritesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = FavoritesViewModel.Factory(quoteRepository)
            )
            FavoritesScreen(
                viewModel = favoritesViewModel,
                onNavigateToDetail = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                },
                onNavigateToDesigner = { quoteId ->
                    navController.navigate(Screen.Create.createRoute(quoteId))
                },
                onNavigateToCollection = { collectionId ->
                    navController.navigate(Screen.CollectionDetail.createRoute(collectionId))
                }
            )
        }

        // Collection Detail
        composable(
            route = Screen.CollectionDetail.route,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getLong("collectionId") ?: 0L
            val collectionViewModel: CollectionDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = CollectionDetailViewModel.Factory(collectionId, quoteRepository)
            )
            CollectionDetailScreen(
                viewModel = collectionViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                },
                onNavigateToDesigner = { quoteId ->
                    navController.navigate(Screen.Create.createRoute(quoteId))
                }
            )
        }

        // Search
        composable(Screen.Search.route) {
            val searchViewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = SearchViewModel.Factory(quoteRepository)
            )
            SearchScreen(
                viewModel = searchViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { quoteId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(quoteId))
                },
                onNavigateToDesigner = { quoteId ->
                    navController.navigate(Screen.Create.createRoute(quoteId))
                }
            )
        }

        // Quote Detail
        composable(
            route = Screen.QuoteDetail.route,
            arguments = listOf(
                navArgument("quoteId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quoteId = backStackEntry.arguments?.getString("quoteId") ?: ""
            val detailViewModel: QuoteDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = QuoteDetailViewModel.Factory(quoteId, quoteRepository)
            )
            QuoteDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDesigner = { qId ->
                    navController.navigate(Screen.Create.createRoute(qId))
                },
                onNavigateToOtherQuote = { qId ->
                    navController.navigate(Screen.QuoteDetail.createRoute(qId))
                },
                onNavigateToSeeAll = { category ->
                    navController.navigate(Screen.Read.createRoute(categories = listOf(category)))
                }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = SettingsViewModel.Factory(preferencesRepository, quoteRepository, reminderManager)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = if (navController.previousBackStackEntry != null) {
                    { navController.popBackStack() }
                } else null,
                onOpenOnboarding = {
                    navController.navigate(Screen.Onboarding.route)
                },
                onNavigateToPhotoCredits = {
                    navController.navigate(Screen.PhotoCredits.route)
                }
            )
        }

        // Photo Credits
        composable(Screen.PhotoCredits.route) {
            PhotoCreditsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

