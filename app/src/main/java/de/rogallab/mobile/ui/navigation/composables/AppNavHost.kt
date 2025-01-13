package de.rogallab.mobile.ui.navigation.composables

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.features.article.ArticlesViewModel
import de.rogallab.mobile.ui.features.article.composables.ArticleWebScreen
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.NavState
import de.rogallab.mobile.ui.features.article.composables.ArticlesListScreen
import de.rogallab.mobile.ui.features.news.composables.NewsListScreen
import de.rogallab.mobile.ui.features.news.NewsViewModel
import kotlinx.coroutines.flow.combine
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavHost(
   navController: NavHostController = rememberNavController(),
   newsViewModel: NewsViewModel = koinViewModel<NewsViewModel>(),
   articlesViewModel: ArticlesViewModel = koinViewModel<ArticlesViewModel>()
) {

   val duration = 800  // in ms
   val tag = "<-AppNavHost"

   // region N A V H O S T   C O M P O S A B L E S -----------------------------
   NavHost(
      navController = navController,
      startDestination = NavScreen.NewsListScreen.route,
      enterTransition = { enterTransition(duration) },
      exitTransition = { exitTransition(duration) },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {
      composable(route = NavScreen.NewsListScreen.route) {
         NewsListScreen(
            newsViewModel = newsViewModel,
            articlesViewModel = articlesViewModel,
            navController = navController
         )
      }
      composable(route = NavScreen.ArticlesListScreen.route) {
         ArticlesListScreen(
            viewModel = articlesViewModel,
            navController = navController
         )
      }
      composable(
         route = NavScreen.WebArticleScreen.route,
      ) {
         ArticleWebScreen(
            navController = navController,
            viewModel = articlesViewModel,
         )
      }
   }
   //endregion

   //region O N E   T I M E   E V E N T S   N A V I G A T I O N ---------------------
   // Observing the navigation state and handle navigation
   // Combine navStateFlow from multiple ViewModels
   val combinedNavEvent: NavEvent? by combine(
      newsViewModel.navStateFlow,
      articlesViewModel.navStateFlow,
   ) { navStates: Array<NavState> ->
      logDebug(tag, "navStates: $navStates")
      // Combine the states as needed, here we just return the first non-null event
      navStates.mapNotNull { it.navEvent }.firstOrNull()
   }.collectAsStateWithLifecycle(initialValue = null)

   combinedNavEvent?.let { navEvent: NavEvent ->
      logInfo(tag, "navEvent: $navEvent")
      // check which ViewModel has the navEvent
      val navigationHandler: INavigationHandler = when {
         newsViewModel.navStateFlow.value.navEvent == navEvent -> newsViewModel
         articlesViewModel.navStateFlow.value.navEvent == navEvent -> articlesViewModel
         else -> return@let
      }

      logVerbose(tag, "navEvent: $navEvent")
      when (navEvent) {
         is NavEvent.NavigateLateral -> {
            navController.navigate(navEvent.route) {
               popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
               }
               launchSingleTop = true
               restoreState = true
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateForward -> {
            // Each navigate() pushes the given destination
            // to the top of the stack.
            navController.navigate(navEvent.route)
            // onNavEventHandled() resets the navEvent to null
            navigationHandler.onNavEventHandled()
         }
         is NavEvent.NavigateReverse -> {
            navController.navigate(navEvent.route) {
               popUpTo(navEvent.route) {  // clears the back stack up to the given route
                  inclusive = true        // ensures that any previous instances of
               }                          // that route are removed
            }
            // onNavEventHandled() resets the navEvent to null
            navigationHandler.onNavEventHandled()
         }
         is NavEvent.NavigateBack -> {
            navController.popBackStack()
            // onNavEventHandled() resets the navEvent to null
            navigationHandler.onNavEventHandled()
         }
      } // end of when (it) {
   } // end of navEvent?.let { it: NavEvent ->
   //endregion
}

//region A N I M A T I O N S --------------------------------------------------------
private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
   duration: Int
) = fadeIn(
   animationSpec = tween(duration)
) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
   duration: Int
) = fadeOut(
   animationSpec = tween(duration)
) + slideOutOfContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(
   duration: Int
) = scaleIn(
   initialScale = 0.1f,
   animationSpec = tween(duration)
) + fadeIn(animationSpec = tween(duration))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(
   duration: Int
) = scaleOut(
   targetScale = 3.0f,
   animationSpec = tween(duration)
) + fadeOut(animationSpec = tween(duration))
//endregion