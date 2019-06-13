package sh.hadi.bark.ui

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import com.yalantis.filter.adapter.FilterAdapter
import com.yalantis.filter.widget.FilterItem

class LogFilterAdapter(private val context: Context, items: List<LogFilter>) : FilterAdapter<LogFilter>(items) {

    override fun createView(position: Int, item: LogFilter): FilterItem {
        val filterItem = FilterItem(context)

        if (item is LevelFilter) {
            filterItem.strokeColor = getMainColor(item.level)
            filterItem.textColor = Color.BLACK
            filterItem.checkedTextColor = ContextCompat.getColor(context, android.R.color.white)
            filterItem.color = ContextCompat.getColor(context, android.R.color.white)
            filterItem.checkedColor = getMainColor(item.level)
            filterItem.text = item.getText()
            if (item.enabledByDefault) {
                filterItem.select()
            } else {
                filterItem.deselect()
            }
        } else {
            filterItem.strokeColor = Color.parseColor("#66bb6a")
            filterItem.textColor = Color.BLACK
            filterItem.checkedTextColor = ContextCompat.getColor(context, android.R.color.white)
            filterItem.color = ContextCompat.getColor(context, android.R.color.white)
            filterItem.checkedColor = Color.parseColor("#66bb6a")
            filterItem.text = item.getText()
            filterItem.select()
        }




        return filterItem
    }

}