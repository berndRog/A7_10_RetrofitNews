package de.rogallab.mobile.ui.news.search

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.rogallab.mobile.R
import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorState
import de.rogallab.mobile.ui.errors.showError
import de.rogallab.mobile.ui.news.NewsIntent
import de.rogallab.mobile.ui.news.NewsViewModel
import de.rogallab.mobile.ui.navigation.composables.AppNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
   viewModel: NewsViewModel,
   navController: NavController
) {
   //12345678901234567890123
   val tag = "<-NewsListScreen"

   val newsUiState by viewModel.newsUiStateFlow.collectAsStateWithLifecycle()
   val searchUiState by viewModel.searchUiStateFlow.collectAsStateWithLifecycle()

   val snackbarHostState = remember { SnackbarHostState() }

   // Back navigation
   val activity = LocalContext.current as Activity
   BackHandler(
      enabled = true,
      onBack = {  activity.finish() }
   )

   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.safeGestures)

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      topBar = {
         TopAppBar(
            title = { Text(stringResource(R.string.searchnews)) },
            navigationIcon = {
               IconButton(onClick = { activity.finish()
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
      Column(
         modifier = Modifier.padding(paddingValues = paddingValues)
      ) {
         val keyboardController = LocalSoftwareKeyboardController.current
         OutlinedTextField(
            modifier = Modifier
               .padding(horizontal = 8.dp)
               .padding(bottom = 8.dp)
               .fillMaxWidth(),
            value = searchUiState.searchText,
            onValueChange = { it ->
               viewModel.onProcessNewsIntent(NewsIntent.SearchTextChange(it))
            },
            label = {
               Text(text = stringResource(R.string.searchtext))
            },
            leadingIcon = {
               Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search News")
            },
            keyboardOptions = KeyboardOptions(
               keyboardType = KeyboardType.Text,
               imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
               onSearch = {
                  viewModel.onProcessNewsIntent(NewsIntent.TriggerSearch)
                  keyboardController?.hide()
               }
            ),
            textStyle = MaterialTheme.typography.titleMedium,
            singleLine = true,
         )

         if (newsUiState.loading) {
            Column(
               modifier = Modifier.padding(horizontal = 8.dp).fillMaxSize(),
               verticalArrangement = Arrangement.Center,
               horizontalAlignment = Alignment.CenterHorizontally
            ) {
               CircularProgressIndicator(modifier = Modifier.size(80.dp))
            }
         } else if (newsUiState.news != null)

            newsUiState.news!!.articles?.let { articles: List<Article> ->
               LazyColumn(state = rememberLazyListState()) {
                  items(articles) { article: Article ->
                     // content
                     NewsItem(
                        article,
                        onClick = {
                           viewModel.onProcessNewsIntent(NewsIntent.SelectedArticleChange(article))
                        }
                     )
                  }
               }
            }
       } // Column
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