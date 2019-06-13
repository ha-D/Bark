package sh.hadi.bark.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import sh.hadi.bark.BarkCommands
import sh.hadi.bark.R
import java.util.*

class LogHistoryAdapter(var list: ArrayList<Int>, val barkCommands: BarkCommands?, val onClick: ((Int) -> Unit)) : RecyclerView.Adapter<LogHistoryAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val limit = 20
        val itemId = list[position]
        val commandName = barkCommands?.getCommandName(itemId) ?: ""
        val text = "${position + 1}. $commandName"
        holder.textView.text = if (text.length < limit) text else text.substring(0, limit - 3) + "..."
        holder.itemId = itemId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.log_command_history_item, parent, false)
        return ViewHolder(itemView)
    }


    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var textView: TextView = view.findViewById(android.R.id.text1)
        var itemId: Int? = null

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            itemId?.let { onClick?.invoke(it) }
        }
    }
}