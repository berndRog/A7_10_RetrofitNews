package de.rogallab.mobile.ui.news

import android.content.Context
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.data.dtos.News
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.base.BaseViewModel
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import kotlinx.coroutines.CoroutineDispatcher
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

class NewsViewModel(
   private val _context: Context,
   private val _repository: INewsRepository,
   private val _dispatcher: CoroutineDispatcher
) : BaseViewModel(TAG) {

   var everythingPage = 1
   private var _newsUiStateFlow: MutableStateFlow<NewsUiState> = MutableStateFlow(NewsUiState())

   // Refreshable Scenario
   private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1)
   init {
      logDebug(TAG, "init{}")
      //triggerSearch()
   }

   // fetch news from webApi
   @OptIn(ExperimentalCoroutinesApi::class)
   val newsUiStateFlow: StateFlow<NewsUiState> = reloadTrigger.flatMapLatest {
      _repository.getEverything(
         _searchUiStateFlow.value.searchText, everythingPage
      ).map { resultData: ResultData<News> ->
         when (resultData) {
            is ResultData.Loading -> {
               _newsUiStateFlow.update { it: NewsUiState ->
                  it.copy(loading = true)
               }
            }
            is ResultData.Success -> {
               _newsUiStateFlow.update { it: NewsUiState ->
                  it.copy(loading = false, news = resultData.data)
               }
            }
            is ResultData.Error -> {
               onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
            }
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

   var selectedArticle: Article? = null
      private set

   private var _searchUiStateFlow: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState())
   val searchUiStateFlow: StateFlow<SearchUiState> = _searchUiStateFlow.asStateFlow()

   // transform intent into an action
   fun onProcessNewsIntent(intent: NewsIntent) {
      when (intent) {
         is NewsIntent.SearchTextChange -> onSearchChange(intent.searchText)
         is NewsIntent.TriggerSearch -> triggerSearch()
         is NewsIntent.SelectedArticleChange -> navigateToArticleWebScreen(intent.article)
         is NewsIntent.SaveArticle -> upsert()
         is NewsIntent.RemoveArticle -> remove(intent.article)
         is NewsIntent.UndoRemoveArticle -> undoRemove()
      }
   }

   private fun onSearchChange(searchText: String) {
      logInfo(TAG, "searchText: $searchText")
      if (searchText == _searchUiStateFlow.value.searchText) return
      _searchUiStateFlow.update { it ->
         it.copy(searchText = searchText)
      }
   }

   private fun navigateToArticleWebScreen(article: Article) {
      logInfo(TAG, "onNavigateToArticle ()")
      selectedArticle = article
      onNavigate(NavEvent.NavigateForward(NavScreen.ArticleWebScreen.route))
   }

   private fun upsert() {
      logInfo(TAG, "upsert()")
      selectedArticle?.let { article ->
         viewModelScope.launch(exceptionHandler) {
            when (val result = _repository.upsert(article)) {
               is ResultData.Error -> {
                  onErrorEvent(ErrorParams(throwable = result.throwable, navEvent = null))
               }
               else -> {  }
            }
         }
      }
   }

   private var _removedArticle: Article? = null
   private fun remove(article: Article) {
      viewModelScope.launch(exceptionHandler) {
         _removedArticle = article
         when (val result = _repository.delete(article)) {
            is ResultData.Error -> {
               onErrorEvent(ErrorParams(throwable = result.throwable, navEvent = null))
            }
            else -> {}
         }
      }
   }
   private fun undoRemove() {
      _removedArticle?.let { article ->
         logDebug(TAG, "undoRemovePerson()")
         viewModelScope.launch(exceptionHandler) {
            when (val resultData = _repository.upsert(article)) {
               is ResultData.Success -> {
                  _removedArticle = null
               }
               is ResultData.Error ->
                  onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
               else -> {}
            }
         }
      }
   }

  private var _articlesStateFlow: MutableStateFlow<ArticlesUiState> = MutableStateFlow(ArticlesUiState())
   val articlesUiStateFlow: StateFlow<ArticlesUiState> =
      _repository.selectArticles().map { resultData: ResultData<List<Article>> ->
         when (resultData) {
            is ResultData.Loading -> {
               _articlesStateFlow.update { it: ArticlesUiState ->
                  it.copy(loading = true)
               }
            }
            is ResultData.Success -> {
               _articlesStateFlow.update { it: ArticlesUiState ->
                  it.copy(loading = false, articles = resultData.data)
               }
            }
            is ResultData.Error -> {
               onErrorEvent(ErrorParams(throwable = resultData.throwable, navEvent = null))
            }
         }
         return@map _articlesStateFlow.value
      }.stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(),
         initialValue = _articlesStateFlow.value
      )



   companion object {
      private const val TAG = "<-NewsViewModel"
   }
}
