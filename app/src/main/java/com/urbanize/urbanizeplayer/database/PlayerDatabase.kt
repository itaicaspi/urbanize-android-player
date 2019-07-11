package com.urbanize.urbanizeplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Campaign::class], version = 3, exportSchema = false)
abstract class PlayerDatabase : RoomDatabase() {

    abstract val playerDatabaseDao: PlayerDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: PlayerDatabase? = null

        fun getInstance(context: Context) : PlayerDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PlayerDatabase::class.java,
                        "player_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}