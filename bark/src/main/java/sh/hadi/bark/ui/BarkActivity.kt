package sh.hadi.bark.ui

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.yalantis.filter.listener.FilterListener
import com.yalantis.filter.widget.Filter
import kotlinx.android.synthetic.main.log_list_content.*
import sh.hadi.bark.Constants.DEFAULT_DB_NAME
import sh.hadi.bark.BarkCommands
import sh.hadi.bark.BarkLevel
import sh.hadi.bark.R
import sh.hadi.bark.Prompt
import sh.hadi.bark.db.LogDatabase
import sh.hadi.bark.db.LogItem
import java.lang.Math.abs


open class BarkActivity(private val barkInstanceName: String = DEFAULT_DB_NAME) : AppCompatActivity(), RecyclerViewClickListener {
    private lateinit var viewModel: LogViewModel
    private var logList = ArrayList<LogItem>()
    private var filteredList = ArrayList<LogItem>()
    private var adapter: LogAdapter? = null
    private val sharedPreferences: SharedPreferences by lazy {  getSharedPreferences("bark_settings", Context.MODE_PRIVATE) }
    private var levelFilter: Filter<LogFilter>? = null
    private val levelFilters by lazy { createLevelFilters() }
    private var tagFilter: Filter<LogFilter>? = null
    private val filterListener = LogFilterListener()
    private val seenTags = mutableSetOf<String>()
    private val disabledLevelFilters: MutableSet<LevelFilter> by lazy { levelFilters.filter { !it.enabledByDefault }.toMutableSet() }
    private val disabledTagFilters: MutableSet<TagFilter> = mutableSetOf()
    private var searchQuery: String = ""
    private val filterCache = mutableMapOf<Int, Boolean>()

    private var clearLogsId: Int? = null
    private var goToItemId: Int? = null
    private var clearAppData: Int? = null
    private var logsPaused: Boolean = false

    private var commandHistoryList = ArrayList<Int>()
    private var commandHistoryAdapter: LogHistoryAdapter? = null

    private var notSeenLogs = 0

    private var timeFormat: Int = TIME_FORMAT_EPOCH

    protected open var barkCommands: BarkCommands? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_list_content)

        viewModel = LogViewModel(LogDatabase.getInstance(this, barkInstanceName).logDao())
        adapter = LogAdapter(barkInstanceName, filteredList, this, this)

        val recyclerViewLayout = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = recyclerViewLayout

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val isScrollAtEnd = !recyclerView.canScrollVertically(5)
                if (isScrollAtEnd) fabScrollToBottom.hide() else fabScrollToBottom.show()
                fabScrollToBottomBadge.visibility =
                    if (isScrollAtEnd || fabScrollToBottomBadge.text == "0") View.INVISIBLE else View.VISIBLE
                if (isScrollAtEnd) {
                    fabScrollToBottomBadge.text = ""
                }
            }
        })

        commandHistoryAdapter = LogHistoryAdapter(commandHistoryList, barkCommands) { executeDebugCommand(it) }
        val historyLayoutManager = LinearLayoutManager(applicationContext)
        historyView.layoutManager = historyLayoutManager
        historyView.adapter = commandHistoryAdapter

        levelFilter = findViewById(R.id.level_filter)
        levelFilter?.adapter = LogFilterAdapter(this, levelFilters)
        levelFilter?.listener = filterListener
        levelFilter?.build()
        levelFilter?.expand()
        levelFilter?.visibility = View.INVISIBLE

        tagFilter = findViewById(R.id.tag_filter)
        tagFilter?.adapter = LogFilterAdapter(this, emptyList())
        tagFilter?.listener = filterListener
        tagFilter?.build()
        tagFilter?.expand()
        tagFilter?.visibility = View.INVISIBLE

        fabPause.setOnClickListener {
            logsPaused = !logsPaused
            fabPause.setImageResource(if (logsPaused) R.drawable.ic_play else R.drawable.ic_pause)
            fabPause.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(if (logsPaused) R.color.icon_play else R.color.icon_pause))
        }

        fabScrollToBottom.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.icon_scroll_down))
        fabScrollToBottom.setOnClickListener {
            recyclerView.scrollToPosition(filteredList.size - 1)
            fabScrollToBottom.hide()
            fabScrollToBottomBadge.visibility = View.INVISIBLE
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.getLogs()
            .observe(this, Observer { t ->
                if (logsPaused) {
                    return@Observer
                }

                val oldItemCount = filteredList.size
                logList.clear()
                filteredList.clear()

                val scrollToEnd =
                    recyclerView.scrollState != RecyclerView.SCROLL_STATE_DRAGGING && !recyclerView.canScrollVertically(
                        5
                    )

                t?.forEach {
                    logList.add(it)

                    if (applyFilters(it)) {
                        filteredList.add(it)
                    }

                    it.tags.split(",").forEach { tag ->
                        if (tag.isNotBlank() && tag !in seenTags) {
                            seenTags.add(tag)
                            tagFilter?.addFilter(TagFilter(tag))
                        }
                    }
                }

                adapter?.notifyDataSetChanged()

                notSeenLogs =
                    if (oldItemCount > filteredList.size) 0 else notSeenLogs + filteredList.size - oldItemCount

                if (scrollToEnd) {
                    recyclerView.scrollToPosition(filteredList.size - 1)
                    notSeenLogs = 0
                }

                if (scrollToEnd) fabScrollToBottom.hide() else fabScrollToBottom.show()
                fabScrollToBottomBadge.text = (notSeenLogs).toString()
                fabScrollToBottomBadge.visibility =
                    if (scrollToEnd || notSeenLogs == 0) View.INVISIBLE else View.VISIBLE
            })
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (goToItemId == null) {
            goToItemId = View.generateViewId()
            menu?.add(0, goToItemId ?: 0, Menu.NONE, "Go To")
        }

        if (clearLogsId == null) {
            clearLogsId = View.generateViewId()
            menu?.add(0, clearLogsId ?: 0, Menu.NONE, "Clear Logs")
        }

        if (clearAppData == null) {
            clearAppData = View.generateViewId()
            menu?.add(0, clearAppData ?: 0, Menu.NONE, "Clear Application Data")
        }

        menu?.let { barkCommands?.populateMenu(menu) }

        menu?.let {
            val searchViewMenuItem = menu.findItem(R.id.search_filter)
            val searchView = searchViewMenuItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    onSearchFilterChange(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    onSearchFilterChange(newText)
                    return true
                }
            })
        }

        super.onPrepareOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.level_filter -> {
                tagFilter?.visibility = View.INVISIBLE
                levelFilter?.visibility = when (levelFilter?.visibility) {
                    View.VISIBLE -> View.INVISIBLE
                    else -> View.VISIBLE
                }
            }
            R.id.tag_filter -> {
                levelFilter?.visibility = View.INVISIBLE
                tagFilter?.visibility = when (tagFilter?.visibility) {
                    View.VISIBLE -> View.INVISIBLE
                    else -> View.VISIBLE
                }
            }
            R.id.toggle_time -> {
                timeFormat = (timeFormat + 1) % 3
                adapter?.timeFormat = timeFormat
            }
            goToItemId -> {
                Prompt.showInputDialog(
                    this,
                    "Go To Log",
                    "Log ID",
                    null,
                    InputType.TYPE_CLASS_NUMBER
                ) { logIdStr: String ->
                    logIdStr.toIntOrNull()?.let { logId ->
                        var closestId: Int? = null
                        var closestIndex: Int? = null
                        filteredList.forEachIndexed { index, log ->
                            if (closestId == null || abs(log.id - logId) < abs(
                                    (closestId
                                        ?: 0) - logId
                                )
                            ) {
                                closestId = log.id
                                closestIndex = index
                            }
                        }
                        closestIndex?.let { recyclerView.scrollToPosition(it) }
                    }
                }
            }
            clearLogsId -> {
                Prompt.showConfirmDialog(
                    this,
                    "Clear Logs",
                    "Are you sure you want to clear the logs?",
                    "Yes",
                    "No"
                ) { confirm ->
                    if (confirm) {
                        viewModel.clearLogs()
                    }
                }
            }
            else -> {
                if (!executeDebugCommand(item.itemId)) {
                    return super.onOptionsItemSelected(item)
                }
            }
        }

        return true
    }

    private fun executeDebugCommand(commandId: Int): Boolean {
        if (barkCommands?.getCommandName(commandId) != null) {
            commandHistoryList.add(commandId)
        }
        commandHistoryAdapter?.notifyDataSetChanged()
        historyView.scrollToPosition(commandHistoryList.size - 1)
        return barkCommands?.handleMenuItem(commandId) ?: false
    }

    override fun onClick(view: View, position: Int) {
    }

    private fun onFiltersChanged() {
        filterCache.clear()
        filteredList.clear()
        filteredList.addAll(logList.filter { applyFilters(it) })
        adapter?.notifyDataSetChanged()
        runOnUiThread { recyclerView.scrollToPosition(filteredList.size - 1) }
    }

    private fun onSearchFilterChange(query: String) {
        this.searchQuery = query
        onFiltersChanged()
    }

    private fun createLevelFilters(): List<LevelFilter> {
        return listOf(
            LevelFilter(BarkLevel.TRACE, sharedPreferences.getBoolean("trace", false)),
            LevelFilter(BarkLevel.DEBUG, sharedPreferences.getBoolean("debug", true)),
            LevelFilter(BarkLevel.INFO, sharedPreferences.getBoolean("info", true)),
            LevelFilter(BarkLevel.WARN, sharedPreferences.getBoolean("warn", true)),
            LevelFilter(BarkLevel.ERROR, sharedPreferences.getBoolean("error", true)),
            LevelFilter(BarkLevel.WTF, sharedPreferences.getBoolean("wtf", true))
        )
    }

    private fun applyFilters(log: LogItem): Boolean {
        var allowed: Boolean? = filterCache[log.id]
        if (allowed == null) {
            allowed = filterLevel(log) && filterTag(log) && filterSearch(log)
            filterCache[log.id] = allowed
        }
        return allowed
    }

    private fun filterSearch(log: LogItem): Boolean {
        val searchQuery = this.searchQuery.toLowerCase()
        return searchQuery.isBlank() ||
                log.message.toLowerCase().contains(searchQuery) || log.data.toLowerCase().contains(searchQuery)
    }

    private fun filterLevel(log: LogItem): Boolean {
        return disabledLevelFilters.all {
            log.level != it.level
        }
    }

    private fun filterTag(log: LogItem): Boolean {
        return disabledTagFilters.all {
            !log.tags.split(",").contains(it.tag)
        }
    }

    inner class LogFilterListener : FilterListener<LogFilter> {
        override fun onFilterDeselected(item: LogFilter) {
            if (item is LevelFilter) {
                disabledLevelFilters.add(item)
                sharedPreferences.edit().putBoolean(item.level.name.toLowerCase(), false).apply()
            } else if (item is TagFilter) {
                disabledTagFilters.add(item)
            }
            onFiltersChanged()
        }

        override fun onFilterSelected(item: LogFilter) {
            if (item is LevelFilter) {
                disabledLevelFilters.remove(item)
                sharedPreferences.edit().putBoolean(item.level.name.toLowerCase(), true).apply()
            } else if (item is TagFilter) {
                disabledTagFilters.remove(item)
            }
            onFiltersChanged()
        }

        override fun onFiltersSelected(filters: ArrayList<LogFilter>) {}
        override fun onNothingSelected() {}
    }

    companion object {
        const val TIME_FORMAT_EPOCH = 0
        const val TIME_FORMAT_DATE = 1
        const val TIME_FORMAT_SINCE = 2
    }
}
