package de.rogallab.mobile.ui.features.news

import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import de.rogallab.mobile.data.dtos.News
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.base.updateState
import de.rogallab.mobile.ui.navigation.INavHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(
   private val _repository: INewsRepository,
   private val _imageLoader: ImageLoader,
   navHandler: INavHandler,
) : BaseViewModel(navHandler, TAG) {

   // S E A R C H   B A R   O N   T O P
   private var _searchUiStateFlow: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())
   val searchUiStateFlow: StateFlow<SearchUiState> = _searchUiStateFlow.asStateFlow()

   // transform intent into an action
   fun onProcessIntent(intent: NewsIntent) {
      logDebug(TAG, "onProcessIntent: $intent")
      when (intent) {
         is NewsIntent.SearchTextChange -> onSearchChange(intent.searchText)
         is NewsIntent.TriggerSearch -> triggerSearch()
      }
   }

   private fun onSearchChange(searchText: String) {
      logDebug(TAG, "searchText: ($searchText) (${_searchUiStateFlow.value.searchText})")
      // Avoid unnecessary recompositions and reloads
      if (searchText == _searchUiStateFlow.value.searchText) return
      // Update search UI state atomically
      updateState(_searchUiStateFlow) { copy(searchText = searchText) }
   }

   // N E W S   L I S T   S C R E E N    (R E F R E S H A B L E)
   // Refreshable Scenario, fetch news from webApi
   // Trigger flow to start a reload action
   // Using replay=1 so the last trigger is remembered for collectors
   private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1)

   // Pagination counter (incremented externally if needed)
   var everythingPage = 1

   // -----------------------------------------------------
   // MAIN NEWS UI STATE FLOW
   //
   // This flow:
   // 1. Emits loading=true whenever reloadTrigger fires
   // 2. Fetches news from the repository
   // 3. Transforms the Result<News> into NewsUiState
   // 4. Handles errors and reports them to BaseViewModel
   // 5. Exposes the resulting StateFlow to the UI
   // -----------------------------------------------------
   val newsUiStateFlow: StateFlow<NewsUiState> = reloadTrigger
      .flatMapLatest {
         // New reload started -> begin data pipeline
         _repository.getEverything(_searchUiStateFlow.value.searchText, everythingPage)
            .map { result: Result<News> ->
               // Transform Result<News> into NewsUiState
               result.fold(
                  onSuccess = { news ->
                     logDebug(TAG, "loading = false, news = ${news.articles.size}")
                     return@map NewsUiState(loading = false, news = news)
                  },
                  onFailure = { throwable ->
                     logDebug(TAG, "loading = false, error = ${throwable.message}")
                     handleErrorEvent(throwable)
                     return@map NewsUiState(loading = false, news = null)
                  }
               )
            }
            .onStart {
               // BEFORE the first repository value arrives,
               // emit a loading UI state to show the spinner
               logDebug(TAG, "loading = true")
               emit(NewsUiState(loading = true))
            }

      }.stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(),
         // Initial UI state before anything is loaded
         initialValue = NewsUiState(loading = true)
      )

   fun triggerSearch() {
      viewModelScope.launch {
         logDebug(TAG, "triggerSearch: ${_searchUiStateFlow.value.searchText}")
         reloadTrigger.emit(Unit)
      }
   }


   @OptIn(ExperimentalCoilApi::class)
   override fun onCleared() {
      logDebug(TAG, "onCleared(): clear caches")
      _imageLoader.memoryCache?.clear()
      _imageLoader.diskCache?.clear()
      super.onCleared()
   }


   companion object {
      private const val TAG = "<-NewsViewModel"
   }
}
