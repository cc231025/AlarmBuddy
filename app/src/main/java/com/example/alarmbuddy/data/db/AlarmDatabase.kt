package com.example.alarmbuddy.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.alarmbuddy.data.Converters

@Database(entities = [AlarmEntity::class], version = 1)
@TypeConverters(Converters::class) // Register the converters
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun AlarmDao(): AlarmDao

    companion object{
        @Volatile
        private var Instance: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {

            return Instance ?: synchronized(this){

                val instance =
                    Room.databaseBuilder(context, AlarmDatabase::class.java, "AlarmDatabase")
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
