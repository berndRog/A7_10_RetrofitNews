package de.rogallab.mobile

import androidx.room.Room
import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.INewsWebservice
import de.rogallab.mobile.data.database.AppDatabase
import de.rogallab.mobile.data.network.ApiKey
import de.rogallab.mobile.data.network.BearerToken
import de.rogallab.mobile.data.network.NetworkConnection
import de.rogallab.mobile.data.network.NetworkConnectivity
import de.rogallab.mobile.data.network.createOkHttpClient
import de.rogallab.mobile.data.network.createRetrofit
import de.rogallab.mobile.data.network.createWebservice
import de.rogallab.mobile.data.repositories.NewsRepository
import de.rogallab.mobile.domain.INewsRepository
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.news.NewsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val uiModules: Module = module {
   val tag = "<-uiModules"

   logInfo(tag, "viewModel -> NewsViewModel")
   viewModel<NewsViewModel> {
      NewsViewModel(
         _context = androidContext(),
         _repository = get<INewsRepository>(),
         _dispatcher = Dispatchers.IO
      )
   }
}

val domainModules: Module = module {
   val tag = "<-domainModules"

   single<CoroutineExceptionHandler> {
      CoroutineExceptionHandler { _, exception ->
         // Handle the exception here
         val e = RuntimeException("Coroutine exception: ${exception.localizedMessage}")
         logError(tag, e.message ?: "Coroutine exception")
         val params = ErrorParams(throwable = e)
         get<IErrorHandler>().onErrorEvent(params)
      }
   }

   single<CoroutineDispatcher>(named("DispatcherIO")) { Dispatchers.IO }
   single<CoroutineDispatcher>(named("DispatcherMain")) { Dispatchers.Main }
}

val dataModules = module {
   val tag = "<-dataModules"

   // local Room Database -----------------------------------------------------
   logInfo(tag, "single    -> AppDatabase")
   single<AppDatabase> {
      Room.databaseBuilder(
         context = androidContext(),
         klass = AppDatabase::class.java,
         name = AppStart.database_name
      ).build()
   }
   logInfo(tag, "single    -> IArticleDao")
   single<IArticleDao> { get<AppDatabase>().createArticleDao() }


   // remote OkHttp/Retrofit Webservice ---------------------------------------
   logInfo(tag, "single    -> NetworkConnection")
   single<NetworkConnection> {
      NetworkConnection(context = androidContext())
   }
   logInfo(tag, "single    -> NetworkConnectivity")
   single<NetworkConnectivity> { NetworkConnectivity(get<NetworkConnection>()) }

   logInfo(tag, "single    -> BearerToken")
   single<BearerToken> { BearerToken() }

   logInfo(tag, "single    -> ApiKey")
   single<ApiKey> { ApiKey() }

   logInfo(tag, "single    -> HttpLoggingInterceptor")
   single<HttpLoggingInterceptor> {
      HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
   }
   logInfo(tag, "single    -> OkHttpClient")
   single<OkHttpClient> {
      createOkHttpClient(
         bearerToken = get<BearerToken>(),
         apiKey = get<ApiKey>(),
         networkConnectivity = get<NetworkConnectivity>(),
         loggingInterceptor = get<HttpLoggingInterceptor>()
      )
   }

   logInfo(tag, "single    -> GsonConverterFactory")
   single<GsonConverterFactory> { GsonConverterFactory.create() }

   logInfo(tag, "single    -> Retrofit")
   single<Retrofit> {
      createRetrofit(
         okHttpClient = get<OkHttpClient>(),
         gsonConverterFactory = get<GsonConverterFactory>()
      )
   }
   logInfo(tag, "single    -> INewsWebservice")
   single<INewsWebservice> {
      createWebservice<INewsWebservice>(
         get<Retrofit>(),
         "INewsWebservice"
      )
   }

   // Provide IPersonRepository, injecting the `viewModelScope`
   logInfo(tag, "single    -> PersonRepository: IPersonRepository")
   single<INewsRepository> {
      NewsRepository(
         _articleDao = get<IArticleDao>(),
         _newsWebservice = get<INewsWebservice>(),
         _dispatcher = get<CoroutineDispatcher>(named("DispatcherIO")),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }
//   factory<IPersonRepository> { (viewModelScope: CoroutineScope) ->
//      PersonRepository(
//         _viewModelScope = viewModelScope,
//         _personDao = get<IPersonDao>(),
//         _coroutineDispatcher = Dispatchers.IO
//      )
//   }

}
