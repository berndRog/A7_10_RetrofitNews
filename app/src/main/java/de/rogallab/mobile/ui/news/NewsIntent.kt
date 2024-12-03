package de.rogallab.mobile.ui.news

import de.rogallab.mobile.data.dtos.Article

sealed class NewsIntent {
   data class  SearchTextChange(val searchText: String) : NewsIntent()
   data object TriggerSearch : NewsIntent()
   data class  SelectedArticleChange(val article: Article) : NewsIntent()
   data object SaveArticle: NewsIntent()
   data class RemoveArticle(val article: Article) : NewsIntent()
   data object UndoRemoveArticle : NewsIntent()
}