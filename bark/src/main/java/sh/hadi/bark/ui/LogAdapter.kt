package sh.hadi.bark.ui

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONObject
import sh.hadi.bark.Bark
import sh.hadi.bark.R
import sh.hadi.bark.db.LogItem
import java.text.SimpleDateFormat
import java.util.*

class LogAdapter(barkInstanceName: String, var list: ArrayList<LogItem>, val context: Context, val listener: RecyclerViewClickListener)
    : RecyclerView.Adapter<LogAdapter.ViewHolder>() {
    private val dataAdapterCache = mutableMapOf<Int, LogDataAdapter>()
    private val stackTraceCache = mutableMapOf<Int, String>()
    private val barkInstance = Bark.getInstance(context, barkInstanceName)

    var timeFormat: Int = BarkActivity.TIME_FORMAT_EPOCH
        set(value) {
            dataAdapterCache.values.forEach {
                it.timeFormat = value
                it.notifyDataSetChanged()
            }
        }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list.get(position)
        holder.message.text = item.message
        holder.tag.text = item.tags.replace(",", "   ")
        holder.timestamp.text = convertDateTime(item.timestamp)
        holder.logIdView.text = item.id.toString()

        if (item.data.isBlank() || item.data == "[]") {
            holder.dataListView.visibility = View.GONE
        } else {
            var dataAdapter = dataAdapterCache[item.id]
            if (dataAdapter == null) {
                val dataMap = JSONObject(item.data)
                dataAdapter = LogDataAdapter(dataMap.keys().asSequence().map { Pair(it, dataMap[it]) }.toList(), item, timeFormat)
                dataAdapterCache[item.id] = dataAdapter
            }

            holder.dataListView.adapter = dataAdapter
            holder.dataListView.visibility = View.VISIBLE
            holder.dataListView.invalidate()
        }

        var stackTrace = stackTraceCache[item.id]
        if (stackTrace == null) {
            stackTrace = filterStacktrace(item.exception)
            stackTraceCache[item.id] = stackTrace
        }
        holder.stackTrace.text = stackTrace
        holder.stackTrace.visibility = if (holder?.stackTrace?.text.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.view.setBackgroundColor(getMainColor(item.level))

        holder.stackTrace.setBackgroundColor(getSecondaryColor(item.level))
        holder.dataListView.setBackgroundColor(getSecondaryColor(item.level))
        val foreground = getTextColor(item.level)
        holder.message.setTextColor(foreground)
        holder.tag.setTextColor(foreground)
        holder.timestamp.setTextColor(foreground)
        holder.logIdView.setTextColor(foreground)
        holder.stackTrace.setTextColor(foreground)
        (holder.dataListView.adapter as LogDataAdapter).foregroundColor = foreground

        barkInstance.colorCustomizer?.let { customizer ->
            customizer(item)?.let { colors ->
                holder.view.setBackgroundColor(colors.backgroundColor)
                holder.dataListView.setBackgroundColor(colors.innerBackgroundColor)
                holder.stackTrace.setBackgroundColor(colors.innerBackgroundColor)

                holder.message.setTextColor(colors.foregroundColor)
                holder.tag.setTextColor(colors.foregroundColor)
                holder.timestamp.setTextColor(colors.foregroundColor)
                holder.logIdView.setTextColor(colors.foregroundColor)
                holder.stackTrace.setTextColor(colors.innerForegroundColor)
                (holder.dataListView.adapter as LogDataAdapter).foregroundColor = colors.innerForegroundColor
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.log_list_item_log, parent, false)
        return ViewHolder(itemView, listener)
    }



    inner class ViewHolder(val view: View, clickListener: RecyclerViewClickListener) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var message: TextView
        var tag: TextView
        var timestamp: TextView
        var dataListView: RecyclerView
        var stackTrace: TextView
        var logIdView: TextView
        private var viewClickListener: RecyclerViewClickListener? = null


        init {
            message = view.findViewById(R.id.message)
            tag = view.findViewById(R.id.tag)
            timestamp = view.findViewById(R.id.timestamp)
            dataListView = view.findViewById(R.id.dataList)
            stackTrace = view.findViewById(R.id.stackTrace)
            logIdView = view.findViewById(R.id.logId)
            stackTrace.setHorizontallyScrolling(true)
            stackTrace.movementMethod = ScrollingMovementMethod()
            val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            dataListView.layoutManager = layoutManager
            viewClickListener = clickListener
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            viewClickListener?.onClick(v, adapterPosition)
        }
    }

    private fun filterStacktrace(raw: String): String {
        val modifiedStacktrace = barkInstance.stacktraceModifier?.let { it(raw) } ?: raw
        val packageName = barkInstance.internalPackageName ?: return modifiedStacktrace
        var addEtc = true
        return modifiedStacktrace.lines().map {
            if (!it.trim().startsWith("at") || it.contains(packageName)) {
                addEtc = true
                it
            } else if (addEtc) {
                addEtc = false
                "   ..."
            } else {
                ""
            }
        }.filter { it.isNotBlank() }
                .joinToString("\n")
    }

    companion object {
        const val EPOCH_TIME_REGEX_STR = "1\\d{12}"
        val EPOCH_TIME_REGEX = Regex(EPOCH_TIME_REGEX_STR)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")

        fun convertDateTime(date: Date): String? {
            return dateFormat.format(date)
        }

        fun convertDateTime(time: Long): String? {
            return dateFormat.format(Date(time))
        }
    }
}