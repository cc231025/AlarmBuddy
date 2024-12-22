package com.example.alarmbuddy.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.alarmbuddy.data.Converters

@Database(entities = [BarcodeEntity::class], version = 1)
abstract class BarcodeDatabase : RoomDatabase() {

    abstract fun BarcodeDao(): BarcodeDao

    companion object{
        @Volatile
        private var Instance: BarcodeDatabase? = null

        fun getDatabase(context: Context): BarcodeDatabase {

            return Instance ?: synchronized(this){

                val instance =
                    Room.databaseBuilder(context, BarcodeDatabase::class.java, "BarcodeDatabase")
                        /**
                         * Setting this option in your app's database builder means that Room
                         * permanently deletes all data from the tables in your database when it
                         * attempts to perform a migration with no defined migration path.
                         */
                        .fallbackToDestructiveMigration()
                        .build()
                Instance = instance
                return instance
            }
        }
    }
}
