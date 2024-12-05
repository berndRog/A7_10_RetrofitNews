package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.dtos.News
import de.rogallab.mobile.data.remote.network.httpStatusMessage
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.domain.utilities.max
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.lang.RuntimeException

fun <T> handleApiRequest(
   tag: String,
   dispatcher: CoroutineDispatcher,
   exceptionHandler: CoroutineExceptionHandler,
   // api call is function type
   apiCall: suspend () -> Response<T>,
): Flow<ResultData<T>> = flow {

   try {
      // make the api call
      val response: Response<T> = apiCall()
      logResponse(tag, response)

      // if the response is successful, emit the body
      if (response.isSuccessful) {
         val body: T? = response.body()
         body?.let { it: T ->
            emit(ResultData.Success(it))
         } ?: run {
            val t = RuntimeException("response body() is null")
            emit(ResultData.Error(t))
         }
      // if the response is not successful, emit an error
      } else {
         val statusCode = response.code()
         val statusMessage = httpStatusMessage(statusCode)
         val t = RuntimeException("response is not successful: $statusCode, $statusMessage")
         emit(ResultData.Error(t))
      }
   // if the api call throws an exception, emit an error
   } catch (e: Exception) {
      emit(ResultData.Error(e))
   }
// flowOn the dispatcher and exceptionHandler
}.flowOn(dispatcher+exceptionHandler)

// helper function to log the response
private fun <T> logResponse(
   tag: String,
   response: Response<T>
) {
   logVerbose(tag, "Request ${response.raw().request.method} ${response.raw().request.url}")
   logVerbose(tag, "Request Headers")
   response.raw().request.headers.forEach {
      val text = "   %-15s %s".format(it.first, it.second )
      logVerbose(tag, "$text")
   }

   val ms = response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis
   logVerbose(tag, "took $ms ms")
   logVerbose(tag, "Response isSuccessful ${response.isSuccessful()}")

   logVerbose(tag, "Response Headers")
   response.raw().headers.forEach {
      val text = "   %-15s %s".format(it.first, it.second)
      logVerbose(tag, "$text")
   }

   logVerbose(tag, "Response Body")
   if (response.body() is News) {
      val news = response.body() as News
      logVerbose(tag, "   articles.size ${news.articles.size}")
      logVerbose(tag, "   totalResults  $news.totalResults}")
   }
   logVerbose(tag, "   Status Code ${response.code().toString().max(100)}")
   logVerbose(tag, "   Status Message ${response.message().toString().max(100)}")
}