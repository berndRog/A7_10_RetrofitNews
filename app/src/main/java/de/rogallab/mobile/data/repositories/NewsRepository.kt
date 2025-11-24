package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.dtos.News
import de.rogallab.mobile.data.remote.INewsWebservice
import de.rogallab.mobile.domain.INewsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class NewsRepository(
   private val _newsWebservice: INewsWebservice,
   private val _dispatcher: CoroutineDispatcher
) : INewsRepository {

   override fun getEverything(
      searchText: String,
      page: Int
   ): Flow<Result<News>> = flow {

      // Trim the text to avoid triggering the API for whitespace-only input
      val query = searchText.trim()

      // SHORT-CIRCUIT: if the search text is empty, immediately emit an empty News() object.
      if (query.isBlank()) {
         emit(Result.success(News()))
         return@flow  // stop execution of the flow builder
      }

      // Normal API call
      try {
         // Call the remote webservice (suspend function)
         val news: News = _newsWebservice.getEverything(text = query, page = page)
         // Emit the successful result with the received news data
         emit(Result.success(news))
      }
      // CancellationException must be re-thrown
      catch (e: CancellationException) { throw e }
      // Any other exception is converted into a failure Result.
      catch (t: Throwable) { emit(Result.failure(t)) }

      // Ensure the entire pipeline runs on the provided dispatcher (usually Dispatchers.IO)
   }.flowOn(_dispatcher)

   companion object {
      private const val tag = "<-NewsRepository"
   }
}