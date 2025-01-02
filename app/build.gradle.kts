import org.gradle.kotlin.dsl.android
import java.util.Properties

/**
 * Module-level functions
 * These functions are used to provide dependencies for the app.
 *
 * The first section in the build configuration applies the Android Gradle plugin
 * to this build and makes the android block available to specify
 * Android-specific build options.
 */
plugins {
   alias(libs.plugins.android.application)
   alias(libs.plugins.kotlin.android)
   alias(libs.plugins.google.devtools.ksp)
   alias(libs.plugins.kotlin.serialization)
   alias(libs.plugins.kotlin.compose.compiler)
}

/**
 * Locate (and possibly download) a JDK used to build your kotlin
 * source code. This also acts as a default for sourceCompatibility,
 * targetCompatibility and jvmTarget. Note that this does not affect which JDK
 * is used to run the Gradle build itself, and does not need to take into
 * account the JDK version required by Gradle plugins (such as the
 * Android Gradle Plugin)
 */
kotlin {
   jvmToolchain(17)
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
      minSdk = 32
      targetSdk = 34
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
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
   }
   kotlinOptions {
      jvmTarget = "17"
   }

   lint {
      abortOnError = false
      disable  += "unchecked"
   }
   buildFeatures {
      compose = true
      buildConfig = true
   }
   packaging {
      resources {
         excludes += "/META-INF/{AL2.0,LGPL2.1}"
         excludes += "/META-INF/LICENSE.md"
         excludes += "/META-INF/LICENSE-notice.md"
      }
   }
}

dependencies {
   // Gradle version catalo
   // https://www.youtube.com/watch?v=MWw1jcwPK3Q

   // Kotlin
   // https://developer.android.com/jetpack/androidx/releases/core
   implementation(libs.core.ktx)
   // Kotlin Coroutines
   // https://kotlinlang.org/docs/releases.html
   implementation (libs.kotlinx.coroutines.core)
   implementation (libs.kotlinx.coroutines.android)
   implementation (libs.kotlinx.datetime)

   // Ui Activity
   // https://developer.android.com/jetpack/androidx/releases/activity
   implementation(libs.compose.activity)

   // Ui Compose
   // https://developer.android.com/jetpack/compose/bom/bom-mapping
   val composeBom = platform(libs.compose.bom)
   implementation(composeBom)
   testImplementation(composeBom)
   androidTestImplementation(composeBom)

   implementation(libs.compose.foundation)
   implementation(libs.compose.material.icons)
   implementation(libs.compose.ui)
   implementation(libs.compose.ui.graphics)
   implementation(libs.compose.ui.tooling)
   implementation(libs.compose.ui.tooling.preview)
   implementation(libs.compose.material.icons)
   implementation(libs.compose.material3)
   implementation(libs.compose.runtime)
   implementation(libs.compose.google.fonts)


   // Ui Navigation
   // https://developer.android.com/jetpack/androidx/releases/navigation
   // Jetpack Compose Integration
   implementation(libs.compose.navigation)

   // Ui Lifecycle
   // https://developer.android.com/jetpack/androidx/releases/lifecycle
   // implementation(libs.androidx.lifecycle.viewmodel.ktx)
   // ViewModel utilities for Compose
   implementation(libs.compose.lifecycle.viewmodel)
   // Lifecycle utilities for Compose
   implementation (libs.compose.lifecycle.runtime)

   // Image loading
   // https://coil-kt.github.io/coil/
   implementation(libs.coil.compose)

   // Room
   implementation(libs.room.ktx)
   implementation(libs.room.runtime)
   ksp(libs.room.compiler)

   // Retrofit,  OkHttp Logging
   implementation (libs.gson.json)
   implementation (libs.retrofit2.core)
   implementation (libs.retrofit2.gson)
   implementation (libs.retrofit2.logging)

   // Koin
   implementation(libs.koin.android)
   implementation(libs.koin.androidx.compose)
   implementation(libs.koin.androidx.compose.navigation)

   // TESTS -----------------------
   testImplementation(libs.junit)

   // ANDROID TESTS ---------------
   // https://developer.android.com/jetpack/androidx/releases/test
   // To use the androidx.test.core APIs
   androidTestImplementation(libs.test.core)
   androidTestImplementation(libs.core.ktx)

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
}




