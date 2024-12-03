package de.rogallab.mobile.ui.news.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorState
import de.rogallab.mobile.ui.errors.showError
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.composables.AppNavigationBar
import de.rogallab.mobile.ui.news.NewsIntent
import de.rogallab.mobile.ui.news.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun SavedArticlesScreen(
   viewModel: NewsViewModel,
   navController: NavController
) {
   val tag = "<-SavedArticlesScreen"

   val articlesUiState by viewModel.articlesUiStateFlow.collectAsStateWithLifecycle()

   BackHandler {
      viewModel.onNavigate(NavEvent.NavigateBack(NavScreen.NewsListScreen.route))
   }

   val snackbarHostState = remember { SnackbarHostState() }
   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.safeGestures)

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.savedarticles)) },
            navigationIcon = {
               IconButton(onClick = {
                  viewModel.onNavigate(NavEvent.NavigateReverse(NavScreen.NewsListScreen.route))
               }) {
                  Icon(
                     imageVector = Icons.AutoMirrored.Default.ArrowBack,
                     contentDescription = stringResource(R.string.back)
                  )
               }
            }
         )
      },
      bottomBar = {
         AppNavigationBar(navController, viewModel)
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
      if (articlesUiState.loading) {
         Column(
            modifier = Modifier
               .padding(paddingValues = paddingValues)
               .padding(horizontal = 16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
         ) {
            CircularProgressIndicator(modifier = Modifier.size(80.dp))
         }
      } else if (articlesUiState.articles != null) {
         LazyColumn(
            modifier = Modifier
               .padding(paddingValues = paddingValues)
               .padding(horizontal = 16.dp).fillMaxSize(),
            state = rememberLazyListState()
         ) {
            items(
               items = articlesUiState.articles!!.sortedBy { it.id }.reversed(),
               key = { it: Article -> it.id!! },
            ) { article ->
               //
               SwipeArticleListItem(
                  article = article,                        // item
                  onNavigate = { it -> viewModel.onNavigate(it) },     // navigate to DetailScreen
                  onProcessIntent = {                     // remove item
                     viewModel.onProcessNewsIntent(NewsIntent.RemoveArticle(article)) },
                  onErrorEvent = viewModel::onErrorEvent, // undo -> show snackbar
                  onUndoAction = {                        // undo -> action
                     viewModel.onProcessNewsIntent(NewsIntent.UndoRemoveArticle)
                  }
               ) {
                  NewsItem(
                     article,
                     onClick = { }
                  )
               }
            }
         }
      }
   } // Scaffold

   val errorState: ErrorState
      by viewModel.errorStateFlow.collectAsStateWithLifecycle()

   LaunchedEffect(errorState.params) {
      errorState.params?.let { params: ErrorParams ->
         logDebug(tag, "ErrorUiState: ${errorState.params}")
         // show the error with a snackbar
         showError(snackbarHostState, params, viewModel::onNavigate )
         // reset the errorState, params are copied to showError
         viewModel.onErrorEventHandled()
      }
   }
}