package de.rogallab.mobile.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import coil.ImageLoader
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.navigation.composables.AppNavHost
import de.rogallab.mobile.ui.theme.AppTheme
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext

class MainActivity : BaseActivity(TAG) {

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      setContent {
         KoinContext {
            AppTheme {
               Surface(modifier = Modifier.fillMaxSize()) {
                  AppNavHost()
               }
            }
         }
      }
   }

   companion object {
      private const val TAG = "<-MainActivity"
   }
}