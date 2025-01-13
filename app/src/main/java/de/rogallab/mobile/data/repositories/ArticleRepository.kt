package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.local.IArticleDao
import de.rogallab.mobile.data.dtos.Article
import de.rogallab.mobile.domain.IArticleRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ArticleRepository(
   private val _articleDao: IArticleDao,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : IArticleRepository {

   override fun selectArticles(): Flow<ResultData<List<Article>>> = flow {
      emit(ResultData.Loading)
      logDebug(tag, "selectArticles")
      _articleDao.select().distinctUntilChanged().collect { articles: List<Article> ->
         emit(ResultData.Success(data = articles))
      }
   }.catch {
      emit(ResultData.Error(it))
   }.flowOn(_dispatcher + _exceptionHandler)

   override suspend fun upsert(article: Article): ResultData<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext try {
            logDebug(tag, "upsert article")
            _articleDao.upsert(article)
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun remove(article: Article): ResultData<Unit> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext try {
            logDebug(tag, "delete article")
            _articleDao.remove(article)
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   companion object {
      private const val tag = "<-ArticleRepository"
   }
}