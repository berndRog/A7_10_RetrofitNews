package de.rogallab.mobile.ui.features.article.composables

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorState
import de.rogallab.mobile.ui.errors.showError
import de.rogallab.mobile.ui.features.article.ArticleIntent
import de.rogallab.mobile.ui.features.article.ArticlesViewModel
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.composables.AppNavBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleWebScreen(
   viewModel: ArticlesViewModel,
   navController: NavController,
) {
   val webArticleUiState by viewModel.webArticleUiStateFlow.collectAsStateWithLifecycle()

   BackHandler {
      if(webArticleUiState.isNews)
         viewModel.onNavigate(NavEvent.NavigateBack(NavScreen.NewsListScreen.route))
      else
         viewModel.onNavigate(NavEvent.NavigateBack(NavScreen.ArticlesListScreen.route))
   }

   val snackbarHostState = remember { SnackbarHostState() }
   val coroutineScope = rememberCoroutineScope()

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.readarticle)) },
            navigationIcon = {
               IconButton(onClick = {
                  if(webArticleUiState.isNews)
                     viewModel.onNavigate(NavEvent.NavigateReverse(NavScreen.NewsListScreen.route))
                  else
                     viewModel.onNavigate(NavEvent.NavigateReverse(NavScreen.ArticlesListScreen.route))
               }) {
                  Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack,
                     contentDescription = stringResource(R.string.back))
               }
            }
         )
      },
      floatingActionButton = {
         FloatingActionButton(
            containerColor = MaterialTheme.colorScheme.secondary,
            onClick = {
               viewModel.onProcessIntent(ArticleIntent.SaveArticle)
               coroutineScope.launch {
                  val snackbarResult = snackbarHostState.showSnackbar(
                     message = "Artikel gespeichert"
                  )
               }
            }
         ) {
            Icon(Icons.Default.Favorite, "Add an article")
         }
      },
      bottomBar = {
         AppNavBar(navController, viewModel)
      },
      snackbarHost = {
         SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }) { paddingValues ->

      webArticleUiState.article?.let { article ->
         AndroidView(
            modifier = Modifier
               .padding(paddingValues = paddingValues)
               .fillMaxSize(),
            factory = { context ->
               WebView(context).apply {
                  layoutParams = ViewGroup.LayoutParams(
                     ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.MATCH_PARENT
                  )
                  webViewClient = WebViewClient()
                  settings.loadWithOverviewMode = true
                  settings.javaScriptEnabled = true
                  loadUrl(article.url)
               }
            }, update = {
            it.loadUrl(article.url)
         })
      }
   } // Scaffold

   val errorState: ErrorState
      by viewModel.errorStateFlow.collectAsStateWithLifecycle()

   LaunchedEffect(errorState.params) {
      errorState.params?.let { params: ErrorParams ->
         showError(snackbarHostState, params, viewModel::onNavigate)
         viewModel.onErrorEventHandled()
      }
   }
}