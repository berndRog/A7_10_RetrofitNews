package de.rogallab.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SavedSearch
import androidx.compose.material.icons.outlined.Search


import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavScreen(
   val route: String,
   val title: String,
   val selectedIcon: ImageVector,
   val unSelectedIcon: ImageVector,
) {
   data object NewsListScreen:  NavScreen(
      route = "NewsListScreen",
      title = "Suchen",
      selectedIcon = Icons.Outlined.Search,
      unSelectedIcon = Icons.Filled.Search,
   )

   data object WebArticleScreen:  NavScreen(
      route = "WebArticleScreen",
      title ="Anzeigen",
      selectedIcon = Icons.AutoMirrored.Outlined.Article,
      unSelectedIcon = Icons.AutoMirrored.Filled.Article,
   )

   data object ArticlesListScreen:  NavScreen(
      route = "ArticlesListScreen",
      title = "Gespeichert",
      selectedIcon = Icons.Outlined.SavedSearch,
      unSelectedIcon = Icons.Filled.SavedSearch
   )
}