package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.remote.INewsWebservice
import de.rogallab.mobile.data.dtos.News
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class NewsRepository(
   private val _newsWebservice: INewsWebservice,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : INewsRepository {

   override fun getEverything(searchText: String, page: Int) = flow<ResultData<News>> {
      emit(ResultData.Loading)
      logDebug(tag, "getEverything search:$searchText, page:$page")
      handleApiRequest(tag, _dispatcher, _exceptionHandler) {
         _newsWebservice.getEverything(searchText, page)
      }.collect { it: ResultData<News> ->
         emit(it)
      }
   }.catch {
      emit(ResultData.Error(it))
   }.flowOn(_dispatcher + _exceptionHandler)


   companion object {
      private const val tag = "<-NewsRepository"
   }
}