package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.data.local.IArticleDao
import de.rogallab.mobile.domain.IArticleRepository
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class ArticleRepository(
   private val _articleDao: IArticleDao,
   private val _dispatcher: CoroutineDispatcher
) : IArticleRepository {

   /**
    * Read all articles from Room as a reactive stream.
    *
    * Important:
    * - We do NOT call `collect {}` on the DAO flow here.
    *   Instead, we return a transformed Flow.
    * - Because we are not inside `flow { ... }`, `map {}` already
    *   takes care of emitting values downstream. No `emit()` is needed
    *   in the success path.
    */
   override fun selectArticles(): Flow<Result<List<Article>>> =
      _articleDao.select()
         .map { articles: List<Article> ->
            // This `map` operator transforms each emission.
            // It does *not* require `emit()`, because:
            // - map is a higher-order operator
            // - it automatically emits its return value downstream.
            logDebug(TAG, "selectArticles() ${articles.size}")
            Result.success(articles)
         }
         .catch { t ->
            // This `catch` block *does* act as a collector for upstream errors.
            // Here we must use `emit()` explicitly to pass a value downstream,
            // because `catch` is a special operator that intercepts exceptions.
            if (t is CancellationException) { throw t }
            emit(Result.failure(t))  // <-- `emit` IS required here
         }
         // Run DB + mapping on the given dispatcher (usually Dispatchers.IO)
         .flowOn(_dispatcher)


   // Upsert (insert or update) a single article.
   override suspend fun upsert(article: Article): Result<Unit> =
      withContext(_dispatcher) {
         return@withContext try {
            logDebug(TAG, "upsert article")
            _articleDao.upsert(article)
            Result.success(Unit)
         } catch (t: Throwable) {
            Result.failure(t)
         }
      }

   // Remove a single article.
   override suspend fun remove(article: Article): Result<Unit> =
      withContext(_dispatcher) {
         return@withContext try {
            logDebug(TAG, "delete article")
            _articleDao.remove(article)
            Result.success(Unit)
         } catch (t: Throwable) {
            Result.failure(t)
         }
      }

   companion object {
      private const val TAG = "<-ArticleRepository"
   }
}