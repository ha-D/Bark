package sh.hadi.bark.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import sh.hadi.bark.BarkLevel
import java.util.*

@Entity(tableName = "logs")
data class LogItem(
    @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Int = 0,
    @ColumnInfo(name = "level")
        val level: BarkLevel,
    @ColumnInfo(name = "tag")
        val tags: String,
    @ColumnInfo(name = "message")
        val message: String,
    @ColumnInfo(name = "timestamp")
        val timestamp: Date,
    @ColumnInfo(name = "exception")
        val exception: String,
    @ColumnInfo(name = "data")
        val data: String
)