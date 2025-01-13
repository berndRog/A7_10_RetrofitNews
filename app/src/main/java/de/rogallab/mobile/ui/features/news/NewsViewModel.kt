package de.rogallab.mobile.ui.features.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import de.rogallab.mobile.data.dtos.News
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(
   private val _repository: INewsRepository,
   private val _imageLoader: ImageLoader,
   private val _navigationHandler: INavigationHandler,
   private val _errorHandler: IErrorHandler,
   private val _exceptionHandler: CoroutineExceptionHandler,
) : ViewModel(),
   INavigationHandler by _navigationHandler,
   IErrorHandler by _errorHandler {

   // N E W S   L I S T   S C R E E N
   private var _newsUiStateFlow: MutableStateFlow<NewsUiState> = MutableStateFlow(NewsUiState())

   // Refreshable Scenario, fetch news from webApi
   private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1)
   init {
      logDebug(TAG, "init{}")
      //triggerSearch()
   }
   var everythingPage = 1
   @OptIn(ExperimentalCoroutinesApi::class)
   val newsUiStateFlow: StateFlow<NewsUiState> = reloadTrigger.flatMapLatest {
      _repository.getEverything(
         _searchUiStateFlow.value.searchText, everythingPage
      ).map { resultData: ResultData<News> ->
         when (resultData) {
            is ResultData.Loading -> _newsUiStateFlow.update { it: NewsUiState ->
                  it.copy(loading = true)
               }
            is ResultData.Success -> _newsUiStateFlow.update { it: NewsUiState ->
                  it.copy(loading = false, news = resultData.data)
               }
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
         }
         return@map _newsUiStateFlow.value
      }
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(),
      NewsUiState()
   )
   fun triggerSearch() {
      viewModelScope.launch {
         reloadTrigger.emit(Unit)
      }
   }

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
      if (searchText == _searchUiStateFlow.value.searchText) return
      _searchUiStateFlow.update { it ->
         it.copy(searchText = searchText)
      }
   }

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
