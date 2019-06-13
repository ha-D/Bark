package sh.hadi.bark

import android.content.Context
import org.json.JSONObject
import sh.hadi.bark.Constants.DEFAULT_DB_NAME
import sh.hadi.bark.db.LogDao
import sh.hadi.bark.db.LogDatabase
import sh.hadi.bark.db.LogItem
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class Bark private constructor(context: Context, name: String) {
    private var logDao: LogDao = LogDatabase.getInstance(context, name).logDao()

    var internalPackageName: String? = null
    internal var colorCustomizer: ((LogItem) -> LogColor?)? = null
    internal var stacktraceModifier: ((String) -> String)? = null

    private val queue = LinkedBlockingQueue<LogItem>(1000)

    init {
        IOThread().start()
    }

    fun log(level: BarkLevel, tags: Collection<String> = emptyList(), message: String = "",
            error: Throwable? = null, data: Map<String, String>? = null,
            timestamp: Date = Calendar.getInstance().time) {
        queue.offer(
            LogItem(
                level = level,
                tags = tags.joinToString(","),
                message = message,
                timestamp = timestamp,
                exception = errorToString(error),
                data = serializeData(data)
            )
        )
    }

    fun bulk(): Bulk = Bulk()

    inner class Bulk {
        private val logs = mutableListOf<LogItem>()

        fun log(level: BarkLevel, tags: Collection<String> = emptyList(), message: String = "",
                error: Throwable? = null, data: Map<String, String>? = null,
                timestamp: Date = Calendar.getInstance().time): Bulk {
            logs.add(
                LogItem(
                    level = level,
                    tags = tags.joinToString(","),
                    message = message,
                    timestamp = timestamp,
                    exception = errorToString(error),
                    data = serializeData(data)
                )
            )
            return this
        }

        fun execute() {
            queue.addAll(logs)
            logs.clear()
        }
    }

    fun customizeLogColor(customizer: (LogItem) -> LogColor?) {
        colorCustomizer = customizer
    }

    fun modifyStacktrace(modifier: (String) -> String) {
        stacktraceModifier = modifier
    }

    inner class IOThread : Thread() {
        override fun run() {
            val logList = mutableListOf<LogItem>()
            while (true) {
                queue.drainTo(logList)
                if (logList.size == 1) {
                    logDao.insertLog(logList[0])
                } else if (logList.size > 1) {
                    logDao.insertBatchLog(logList)
                }
                logList.clear()
            }
        }
    }

    private fun errorToString(throwable: Throwable?): String {
        return if (throwable != null) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            sw.toString()
        } else { "" }
    }

    private fun serializeData(data: Map<String, String>?): String = data?.let { JSONObject(it).toString() } ?: ""

    companion object {
        private val barkMap = mutableMapOf<String, Bark>()

        fun getInstance(context: Context, name: String = DEFAULT_DB_NAME): Bark {
            return barkMap[name] ?: Bark(context, name).apply { barkMap[name] = this }
        }

    }
}