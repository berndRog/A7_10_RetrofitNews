@file:Suppress("UnstableApiUsage")

pluginManagement {
   repositories {
      google()
      mavenCentral()
      gradlePluginPortal()
   }
}
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
   }
}
val NEWS_API_KEY = "a904cda52f054306a6cc9a3494b36aad"
rootProject.name = "A7_10_RetrofitNews"
include(":app")
