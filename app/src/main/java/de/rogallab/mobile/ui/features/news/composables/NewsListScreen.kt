package de.rogallab.mobile.ui.features.news.composables

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.ImageLoader
import de.rogallab.mobile.R
import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorState
import de.rogallab.mobile.ui.errors.showError
import de.rogallab.mobile.ui.features.article.ArticleIntent
import de.rogallab.mobile.ui.features.article.ArticlesViewModel
import de.rogallab.mobile.ui.features.news.NewsIntent
import de.rogallab.mobile.ui.features.news.NewsViewModel
import de.rogallab.mobile.ui.navigation.composables.AppNavBar
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
   newsViewModel: NewsViewModel,
   articlesViewModel: ArticlesViewModel,
   navController: NavController,
   imageLoader: ImageLoader = koinInject()
) {
   val tag = "<-NewsListScreen"

   val newsUiState by newsViewModel.newsUiStateFlow.collectAsStateWithLifecycle()
   val searchUiState by newsViewModel.searchUiStateFlow.collectAsStateWithLifecycle()

   val activity = LocalContext.current as Activity
   BackHandler(
      enabled = true,
      onBack = {  activity.finish() }
   )

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.searchnews)) },
            navigationIcon = {
               IconButton(onClick = {
                  activity.finish()
               }) {
                  Icon(
                     imageVector = Icons.Default.Menu,
                     contentDescription = stringResource(R.string.back)
                  )
               }
            }
         )
      },
      bottomBar = {
         AppNavBar(navController, newsViewModel)
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }
   ) { paddingValues ->
      Column(
         modifier = Modifier.padding(paddingValues = paddingValues)
      ) {
         SearchField(
            searchText = searchUiState.searchText,
            onSearchTextChange = { it ->
               newsViewModel.onProcessIntent(NewsIntent.SearchTextChange(it))
            },
            onTriggerSearch = {
               newsViewModel.onProcessIntent(NewsIntent.TriggerSearch)
            }
         )

         if (newsUiState.loading) {
            Column(
               modifier = Modifier.fillMaxSize(),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
            ) {
               CircularProgressIndicator(modifier = Modifier.size(80.dp))
            }
         } else if (newsUiState.news != null) {

            newsUiState.news!!.articles?.let { articles: List<Article> ->
               LazyColumn(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  state = rememberLazyListState()) {
                  items(articles) { article: Article ->
                     // content
                     NewsItem(
                        article,
                        onClick = {
                           articlesViewModel.onProcessIntent(
                              ArticleIntent.ShowWebArticle(true, article))
                        },
                        imageLoader = imageLoader
                     )
                  }
               }
            }
         }
       } // Column
   } // Scaffold

   val errorState: ErrorState
      by newsViewModel.errorStateFlow.collectAsStateWithLifecycle()

   LaunchedEffect(errorState.params) {
      errorState.params?.let { params: ErrorParams ->
         logDebug(tag, "ErrorUiState: ${errorState.params}")
         // show the error with a snackbar
         showError(snackbarHostState, params, newsViewModel::onNavigate )
         // reset the errorState, params are copied to showError
         newsViewModel.onErrorEventHandled()
      }
   }
}