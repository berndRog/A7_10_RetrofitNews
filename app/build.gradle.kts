import java.util.Properties

plugins {
   alias(libs.plugins.android.application)
   alias(libs.plugins.kotlin.android)
   alias(libs.plugins.kotlin.compose)
   alias(libs.plugins.google.devtools.ksp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
   localPropertiesFile.inputStream().use { stream ->
      localProperties.load(stream)
   }
}

android {
   namespace = "de.rogallab.mobile"
   compileSdk = 35

   defaultConfig {
      applicationId = "de.rogallab.mobile"
      minSdk = 26
      targetSdk = 35
      versionCode = 1
      versionName = "1.0"

//    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      testInstrumentationRunner = "de.rogallab.mobile.CustomTestRunner"

      vectorDrawables {
         useSupportLibrary = true
      }

      // API Key from local.properties
      val newsApiKey = localProperties["NEWS_API_KEY"] as String
      buildConfigField("String", "NEWS_API_KEY", "\"$newsApiKey\"")
   }

   buildTypes {
      release {
         isMinifyEnabled = false
         proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      }
   }
   compileOptions {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
   }
   kotlinOptions {
      jvmTarget = "11"
   }
   buildFeatures {
      compose = true
      buildConfig = true
   }
}

dependencies {

   // Gradle version catalo
   // https://www.youtube.com/watch?v=MWw1jcwPK3Q

   // Kotlin
   // https://developer.android.com/jetpack/androidx/releases/core
   implementation(libs.androidx.core.ktx)
   implementation(libs.androidx.lifecycle.runtime.ktx)

   // Ui Activity
   // https://developer.android.com/jetpack/androidx/releases/activity
   implementation(libs.androidx.activity.compose)

   // Ui Compose
   // https://developer.android.com/develop/ui/compose/bom/bom-mapping
   val composeBom = platform(libs.androidx.compose.bom)
   implementation(composeBom)
   testImplementation(composeBom)
   androidTestImplementation(composeBom)
   implementation(libs.androidx.foundation)
   implementation(libs.androidx.ui)
   implementation(libs.androidx.ui.graphics)
   implementation(libs.androidx.ui.tooling.preview)
   implementation(libs.androidx.material.icons)
   implementation(libs.androidx.material3)
   implementation(libs.androidx.google.fonts)

   // Kotlin Coroutines
   // https://kotlinlang.org/docs/releases.html
   implementation (libs.kotlinx.coroutines.core)
   implementation (libs.kotlinx.coroutines.android)
   implementation (libs.kotlinx.datetime)

   // Ui Navigation
   // https://developer.android.com/jetpack/androidx/releases/navigation
   // Jetpack Compose Integration
   implementation(libs.androidx.navigation.compose)

   // Ui Lifecycle
   // https://developer.android.com/jetpack/androidx/releases/lifecycle
   // implementation(libs.androidx.lifecycle.viewmodel.ktx)
   // ViewModel utilities for Compose
   implementation(libs.androidx.lifecycle.viewmodel.compose)
   // Lifecycle utilities for Compose
   implementation (libs.androidx.lifecycle.runtime.ktx)

   // Image loading
   // https://coil-kt.github.io/coil/
   implementation(libs.coil.compose)

   // Koin
   implementation(libs.koin.android)
   implementation(libs.koin.androidx.compose)
   implementation(libs.koin.androidx.compose.navigation)

   // Room
   implementation(libs.room.ktx)
   implementation(libs.room.runtime)
   ksp(libs.room.compiler)


   // Retrofit,  OkHttp Logging
   implementation (libs.gson.json)
   implementation (libs.retrofit2.core)
   implementation (libs.retrofit2.gson)
   implementation (libs.retrofit2.logging)


   // TESTS ---------------
   testImplementation(libs.junit)


   // https://developer.android.com/jetpack/androidx/releases/test
   // To use the androidx.test.core APIs
   androidTestImplementation(libs.test.core)

   // To use the androidx.test.espresso
   androidTestImplementation(libs.test.espresso.core)

   // To use the JUnit Extension APIs
   androidTestImplementation(libs.test.junit)
   androidTestImplementation(libs.test.junit.ktx)

   // To use the Truth Extension APIs
   androidTestImplementation(libs.test.truth)

   // To use the androidx.test.runner APIs
   androidTestImplementation(libs.test.runner)

   // To use Compose Testing
   androidTestImplementation(libs.test.ui.junit4)
   // testing navigation
   androidTestImplementation(libs.test.navigation)
   // testing coroutines
   androidTestImplementation(libs.test.kotlinx.coroutines)

   // Koin Testing
   androidTestImplementation(libs.test.koin)
   androidTestImplementation(libs.test.koin.junit4)
   androidTestImplementation(libs.test.koin.android)
   // Room Testing
   androidTestImplementation(libs.test.room)
   androidTestImplementation(libs.test.arch.core)
   // Mockito Testing
   androidTestImplementation(libs.test.mockito.core)
   androidTestImplementation(libs.test.mockito.android)
   androidTestImplementation(libs.test.mockito.kotlin)

   androidTestImplementation(libs.test.ui.manifest)

   debugImplementation(libs.test.ui.manifest)
   debugImplementation(libs.androidx.ui.tooling)

}