package sh.hadi.bark.ui

import com.yalantis.filter.model.FilterModel
import sh.hadi.bark.BarkLevel

interface LogFilter : FilterModel

class LevelFilter(val level: BarkLevel, val enabledByDefault: Boolean = true) : LogFilter {
    override fun getText(): String = level.name
}

class TagFilter(val tag: String) : LogFilter {
    override fun getText(): String = tag
}