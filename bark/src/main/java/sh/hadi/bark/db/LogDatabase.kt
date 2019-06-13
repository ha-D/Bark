package sh.hadi.bark.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

@Database(entities = arrayOf(LogItem::class), version = 8, exportSchema = false)
@TypeConverters(LogConverters::class)
abstract class LogDatabase : RoomDatabase() {

    abstract fun logDao(): LogDao

    companion object {

        @Volatile
        private var INSTANCE: LogDatabase? = null

        fun getInstance(context: Context, databaseName: String): LogDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context, databaseName).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context, databaseName: String) =
                Room.databaseBuilder(context.applicationContext,
                        LogDatabase::class.java, "$databaseName.db")
                        .fallbackToDestructiveMigration()
                        .build()
    }
}
