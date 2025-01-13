package de.rogallab.mobile.ui.navigation.composables

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavBar(
   navController: NavController,
   navigationHandler: INavigationHandler
) {
   val topLevelScreens = listOf(
      NavScreen.NewsListScreen,
      NavScreen.ArticlesListScreen
   )
   NavigationBar(
//    containerColor = MaterialTheme.colorScheme.primary
//    contentColor = MaterialTheme.colors.onSecondary
   ) {
      val tag = "<-AppNavigationBar"

      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentRoute = navBackStackEntry?.destination?.route

      topLevelScreens.forEach { topLevelScreen ->
         NavigationBarItem(
            icon = {
               Icon(
                  imageVector =
                     if(currentRoute == topLevelScreen.route) topLevelScreen.selectedIcon
                     else                                     topLevelScreen.unSelectedIcon,
                  contentDescription = topLevelScreen.title
               )
            },
            label = { Text( text = topLevelScreen.title)   },
            alwaysShowLabel = true,
            selected = currentRoute == topLevelScreen.route,
            onClick = {
               navigationHandler.onNavigate(NavEvent.NavigateLateral(topLevelScreen.route))
            }
         )
      }
   }
}