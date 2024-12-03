package de.rogallab.mobile.ui.news

import androidx.compose.runtime.Immutable

@Immutable
data class SearchUiState(
   val searchText: String = "",
)