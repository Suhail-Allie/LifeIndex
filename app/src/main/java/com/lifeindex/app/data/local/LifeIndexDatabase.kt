package com.lifeindex.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TrackerCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LifeIndexDatabase : RoomDatabase() {
    abstract fun trackerCacheDao(): TrackerCacheDao

    companion object {
        @Volatile private var INSTANCE: LifeIndexDatabase? = null

        fun getInstance(context: Context): LifeIndexDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LifeIndexDatabase::class.java,
                    "lifeindex_offline.db"
                ).build().also { INSTANCE = it }
            }
    }
}