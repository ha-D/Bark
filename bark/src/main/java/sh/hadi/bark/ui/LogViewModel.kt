package sh.hadi.bark.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import sh.hadi.bark.db.LogDao
import sh.hadi.bark.db.LogItem

class LogViewModel(private val dataSource: LogDao) : ViewModel() {

    fun addLog(log: LogItem) = Thread { dataSource.insertLog(log) }.start()

    fun getLogs(): LiveData<List<LogItem>> = dataSource.getLogs()

    fun clearLogs() = Thread{ dataSource.deleteAlLogs() }.start()
}