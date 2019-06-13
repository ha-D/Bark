package sh.hadi.bark.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface LogDao {
    @Query("SELECT * FROM logs")
    fun getLogs(): LiveData<List<LogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLog(log: LogItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBatchLog(log: List<LogItem>)

    @Query("DELETE FROM logs")
    fun deleteAlLogs()
}