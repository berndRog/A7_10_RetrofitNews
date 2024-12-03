package de.rogallab.mobile.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.IArticleDao
import de.rogallab.mobile.data.dtos.Article

@Database(
   entities = [ Article::class],
   version = AppStart.database_version,
   exportSchema = false)
@TypeConverters(Converters::class)

abstract class AppDatabase: RoomDatabase() {
    abstract fun createArticleDao(): IArticleDao
}