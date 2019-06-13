package sh.hadi.bark.db

import android.arch.persistence.room.TypeConverter
import sh.hadi.bark.BarkLevel
import java.util.*

class LogConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun logLevelToInt(level: BarkLevel?) = level?.ordinal

    @TypeConverter
    fun intToLogLevel(level: Int?) = BarkLevel.values()[level ?: 0]
}