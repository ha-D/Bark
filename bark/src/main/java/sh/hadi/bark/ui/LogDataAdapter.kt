package sh.hadi.bark.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import sh.hadi.bark.R
import sh.hadi.bark.db.LogItem
import sh.hadi.bark.ui.BarkActivity.Companion.TIME_FORMAT_DATE
import sh.hadi.bark.ui.BarkActivity.Companion.TIME_FORMAT_EPOCH
import sh.hadi.bark.ui.BarkActivity.Companion.TIME_FORMAT_SINCE
import java.util.regex.Pattern

class LogDataAdapter(val list: List<Pair<String, Any?>>, val log: LogItem, var timeFormat: Int) : RecyclerView.Adapter<LogDataAdapter.ViewHolder>() {
    private var expanded: Boolean = false
    var foregroundColor: Int? = null

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.key.text = item.first
        val value = item.second

        var text = when (value) {
            is Map<*, *> -> JSONObject(value).toString(2)
            is List<*> -> JSONArray(value).toString(2)
            else -> {
                val valueStr = value.toString()
                val valueStrTrimmed = valueStr.trimStart()
                when {
                    valueStrTrimmed.startsWith("{") -> try {
                        JSONObject(valueStrTrimmed).toString(2)
                    } catch (ex: Exception) {
                        value.toString()
                    }
                    valueStrTrimmed.startsWith("[") -> try {
                        JSONArray(valueStrTrimmed).toString(2)
                    } catch (ex: Exception) {
                        value.toString()
                    }
                    else -> valueStr
                }
            }
        }

        if (timeFormat != TIME_FORMAT_EPOCH) {
            val p = Pattern.compile(LogAdapter.EPOCH_TIME_REGEX_STR)
            val m = p.matcher(text)
            val sb = StringBuffer()
            while (m.find()) {
                m.appendReplacement(sb, when (timeFormat) {
                    TIME_FORMAT_DATE -> LogAdapter.convertDateTime(m.group().toLong())
                    TIME_FORMAT_SINCE -> timeAgo(m.group().toLong())
                    else -> m.group()
                })
            }
            m.appendTail(sb)
            text = sb.toString()
        }

        holder.setData(text)

        val foreground = foregroundColor ?: getTextColor(log.level)
        holder.key.setTextColor(foreground)
        holder.value.setTextColor(foreground)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.log_list_item_log_data, parent, false)
        return ViewHolder(itemView)
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var key: TextView = view.findViewById(R.id.key)
        var value: TextView = view.findViewById(R.id.value)
        var expandIcon: View = view.findViewById(R.id.expand_icon)

        private var realData: String? = null

        init {
            view.setOnClickListener { toggle() }
        }

        fun setData(newData: String) {
            realData = newData
            refresh()
        }

        private fun refresh() {
            if (realData != null && !expanded && realData?.count { it == '\n' } ?: 0 > 3) {
                value.text = realData?.split("\n")?.subList(0, 3)?.joinToString("\n") + "\n..."
                expandIcon.visibility = View.VISIBLE
            } else {
                value.text = realData ?: ""
                expandIcon.visibility = View.GONE
            }
        }

        fun toggle() {
            expanded = !expanded
            refresh()
        }

        override fun onClick(v: View?) {}
    }

    companion object {
        private fun timeAgo(time: Long): String {
            val now = System.currentTimeMillis()
            val timeAgo = now - time
            return when {
                timeAgo < 1000 -> "$timeAgo millis ago"
                timeAgo < 1000 * 60 -> "${(timeAgo / 1000).toInt()} seconds ago"
                timeAgo < 1000 * 60 * 60 -> "${(timeAgo / (1000 * 60)).toInt()} minutes ago"
                timeAgo < 1000 * 60 * 60 * 24 -> "${(timeAgo / (1000 * 60 * 60)).toInt()} hours ago"
                else -> "${(timeAgo / (1000 * 60 * 60 * 24)).toInt()} days ago"
            }
        }
    }
}
