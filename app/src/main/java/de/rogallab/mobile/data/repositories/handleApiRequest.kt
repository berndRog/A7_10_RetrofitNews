package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.dtos.News
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

// helper function to get the http status message from the http status code
private fun httpStatusMessage(httpStatusCode:Int) : String =
   when(httpStatusCode) {
      200 -> "OK"
      201 -> "Created"
      202 -> "Accepted"
      203 -> "Non-Authoritative Information"
      204 -> "No Content"
      205 -> "Reset Content"
      206 -> "Partial Content"
      300 -> "Multiple Choices"
      301 -> "Moved Permanently"
      302 -> "Found"
      303 -> "See Other"
      304 -> "Not Modified"
      305 -> "Use Proxy"
      307 -> "Temporary Redirect"
      308 -> "Permanent Redirect"
      400 -> "Bad Request"
      401 -> "Unauthorized"
      403 -> "Forbidden"
      404 -> "Not Found"
      405 -> "Method Not Allowed"
      406 -> "Not Acceptable"
      407 -> "Proxy Authentication Required"
      408 -> "Request Timeout"
      409 -> "Conflict"
      410 -> "Gone"
      411 -> "Length Required"
      412 -> "Precondition Failed"
      413 -> "Payload Too Large"
      414 -> "URI Too Long"
      415 -> "Unsupported Media Type"
      416 -> "Range Not Satisfiable"
      417 -> "Expectation Failed"
      418 -> "I'm a teapot"
      421 -> "Misdirected Request"
      422 -> "Unprocessable Entity"
      423 -> "Locked"
      424 -> "Failed Dependency"
      425 -> "Too Early"
      426 -> "Upgrade Required"
      428 -> "Precondition Required"
      429 -> "Too Many Requests"
      431 -> "Request Header Fields Too Large"
      451 -> "Unavailable For Legal Reasons"
      500 -> "Internal Server Error"
      501 -> "Not Implemented"
      502 -> "Bad Gateway"
      503 -> "Service Unavailable"
      504 -> "Gateway Timeout"
      505 -> "HTTP Version Not Supported"
      506 -> "Variant Also Negotiates"
      507 -> "Insufficient Storage"
      508 -> "Loop Detected"
      510 -> "Not Extended"
      511 -> "Network Authentication Required"
      else -> "Unknown"
   }

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