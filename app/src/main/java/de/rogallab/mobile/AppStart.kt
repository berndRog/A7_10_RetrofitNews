package de.rogallab.mobile

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import de.rogallab.mobile.domain.utilities.logInfo
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.java.KoinJavaComponent.inject

class AppStart : Application() { //, ImageLoaderFactory {

// private lateinit var _imageLoader: ImageLoader

   override fun onCreate() {
      super.onCreate()

      val maxMemory = (Runtime.getRuntime().maxMemory() / 1024 ).toInt()
      logInfo(TAG, "onCreate() maxMemory $maxMemory kB")

      logInfo(TAG, "onCreate(): startKoin{...}")
      startKoin {
         // Log Koin into Android logger
         androidLogger(Level.DEBUG)
         // Reference Android context
         androidContext(this@AppStart)
         // Load modules
         modules(domainModules, dataModules, uiModules)
      }

//    _imageLoader = newImageLoader()
   }

//   override fun newImageLoader(): ImageLoader {
//      return ImageLoader(this).newBuilder()
//         .memoryCachePolicy(CachePolicy.ENABLED)
//         .memoryCache {
//            MemoryCache.Builder(this)
//               .maxSizePercent(0.1)
//               .strongReferencesEnabled(true)
//               .build()
//         }
//         .diskCachePolicy(CachePolicy.ENABLED)
//         .diskCache {
//            DiskCache.Builder()
//               .maxSizePercent(0.03)
//               .directory(cacheDir)
//               .build()
//         }
//         .logger(DebugLogger())
//         .build()
//   }


   companion object {
      private const val TAG = "<-AppStart"
      const val IS_INFO = true
      const val IS_DEBUG = true
      const val DATABASE_NAME:    String = "A7_10_RetrofitNews.db"
      const val DATABASE_VERSION: Int    = 1

      const val BASE_URL: String = "https://newsapi.org/"
      const val API_KEY:  String = BuildConfig.NEWS_API_KEY
      const val BEARER_TOKEN:  String = ""
   }
}