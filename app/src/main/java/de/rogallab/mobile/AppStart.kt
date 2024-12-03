package de.rogallab.mobile

import android.app.Application
import de.rogallab.mobile.domain.utilities.logInfo
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AppStart : Application() {

   val apiKey = BuildConfig.NEWS_API_KEY

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

   }

   companion object {
      private const val TAG = "<-AppStart"
      const val isInfo = true
      const val isDebug = true
      const val database_name:    String = "A7_10_RetrofitNews.db"
      const val database_version: Int    = 1
      const val base_url: String = "https://newsapi.org/"
      const val api_key:  String = BuildConfig.NEWS_API_KEY
      const val bearer_token:  String = ""
   }
}