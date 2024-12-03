package de.rogallab.mobile.ui.features.news

import de.rogallab.mobile.data.dtos.Article

sealed class NewsIntent {
   data class  SearchTextChange(val searchText: String) : NewsIntent()
   data object TriggerSearch : NewsIntent()
}