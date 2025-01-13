package de.rogallab.mobile.ui.features.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.domain.IArticleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArticlesViewModel(
   private val _repository: IArticleRepository,
   private val _navigationHandler: INavigationHandler,
   private val _errorHandler: IErrorHandler,
   private val _exceptionHandler: CoroutineExceptionHandler,
) : ViewModel(),
   INavigationHandler by _navigationHandler,
   IErrorHandler by _errorHandler {

   // A R T I C L E S   L I S T   S C R E E N
   private var _articlesStateFlow: MutableStateFlow<ArticlesUiState> = MutableStateFlow(ArticlesUiState())
   val articlesUiStateFlow: StateFlow<ArticlesUiState> =
      _repository.selectArticles().map { resultData: ResultData<List<Article>> ->
         when (resultData) {
            is ResultData.Loading ->
               _articlesStateFlow.update { it: ArticlesUiState ->
                  it.copy(loading = true)
               }
            is ResultData.Success ->
               _articlesStateFlow.update { it: ArticlesUiState ->
                  it.copy(loading = false, articles = resultData.data)
               }
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
         }
         return@map _articlesStateFlow.value
      }.stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(),
         initialValue = _articlesStateFlow.value
      )

   // W E B   A R T I C L E   S C R E E N
   private var _webArticleUiStateFlow: MutableStateFlow<WebArticleUiState> = MutableStateFlow(WebArticleUiState())
   val webArticleUiStateFlow: StateFlow<WebArticleUiState> = _webArticleUiStateFlow.asStateFlow()

   // transform intent into an action
   fun onProcessIntent(intent: ArticleIntent) {
      when (intent) {
         is ArticleIntent.ShowWebArticle ->
            selectArticle(intent.isNews, intent.article)
         is ArticleIntent.SaveArticle -> upsert()
         is ArticleIntent.RemoveArticle -> remove(intent.article)
         is ArticleIntent.UndoRemoveArticle -> undoRemove()
      }
   }

   private fun selectArticle(isNews: Boolean, article: Article) {
      _webArticleUiStateFlow.update { it: WebArticleUiState ->
         it.copy(isNews = isNews, article = article)
      }
      onNavigate(NavEvent.NavigateForward(NavScreen.WebArticleScreen.route))
   }

   private fun upsert() {
      _webArticleUiStateFlow.value.article?.let { article ->
         viewModelScope.launch(_exceptionHandler) {
            when (val resultData = _repository.upsert(article)) {
               is ResultData.Error -> handleErrorEvent(resultData.throwable)
               else -> {}
            }
         }
      }
   }

   private var _removedArticle: Article? = null

   private fun remove(article: Article) {
      viewModelScope.launch(_exceptionHandler) {
         when (val resultData = _repository.remove(article)) {
            is ResultData.Success -> _removedArticle = article
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            else -> {}
         }
      }
   }
   private fun undoRemove() {
      _removedArticle?.let { article ->
         viewModelScope.launch(_exceptionHandler) {
            when (val resultData = _repository.upsert(article)) {
               is ResultData.Success -> _removedArticle = null
               is ResultData.Error -> handleErrorEvent(resultData.throwable)
               else -> {}
            }
         }
      }
   }

   companion object {
      private const val TAG = "<-ArticlesViewModel"
   }
}