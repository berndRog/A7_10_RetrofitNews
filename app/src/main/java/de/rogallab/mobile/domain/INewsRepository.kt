package de.rogallab.mobile.domain

import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.data.dtos.News
import kotlinx.coroutines.flow.Flow

interface INewsRepository {
   fun getEverything(searchText: String, page: Int): Flow<ResultData<News>>
   fun selectArticles(): Flow<ResultData<List<Article>>>
   suspend fun upsert(article: Article): ResultData<Unit>
   suspend fun delete(article: Article): ResultData<Unit>
}

