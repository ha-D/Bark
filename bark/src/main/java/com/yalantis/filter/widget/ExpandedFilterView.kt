/*
The MIT License (MIT)

Copyright © 2016 Yalantis, https://yalantis.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.yalantis.filter.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.yalantis.filter.listener.CollapseListener
import com.yalantis.filter.model.Coord
import sh.hadi.bark.R
import java.util.*

/**
 * Created by galata on 30.08.16.
 */
class ExpandedFilterView : ViewGroup {

    private var mPrevItem: View? = null
    private var mPrevX: Int? = null
    private var mPrevY: Int? = null
    private var mPrevHeight = 0
    private var mStartX = 0f
    private var mStartY = 0f

    internal var listener: CollapseListener? = null
    internal var margin: Int = dpToPx(getDimen(R.dimen.margin))
    internal val filters: LinkedHashMap<FilterItem, Coord> = LinkedHashMap()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes)

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        if (!filters.isEmpty()) {
            for (i in 0..childCount - 1) {
                val child: View = getChildAt(i)
                val coord: Coord? = filters[child]

                if (coord != null) {
                    child.layout(coord.x, coord.y, coord.x + child.measuredWidth, coord.y + child.measuredHeight)
                }
            }
        }
    }

    private fun canPlaceOnTheSameLine(filterItem: View): Boolean {
        if (mPrevItem != null) {
            val occupiedWidth: Int = mPrevX!! + mPrevItem!!.measuredWidth + margin + filterItem.measuredWidth

            return occupiedWidth <= measuredWidth
        }

        return false
    }

    private fun calculateDesiredHeight(): Int {
        var height: Int = mPrevHeight

        if (filters.isEmpty()) {
            for (i in 0..childCount - 1) {
                val child: FilterItem = getChildAt(i) as FilterItem

                child.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

                if (mPrevItem == null) {
                    mPrevX = margin
                    mPrevY = margin
                    height = child.measuredHeight + margin
                } else if (canPlaceOnTheSameLine(child)) {
                    mPrevX = mPrevX!! + mPrevItem!!.measuredWidth + margin / 2
                } else {
                    mPrevX = margin
                    mPrevY = mPrevY!! + mPrevItem!!.measuredHeight + margin / 2
                    height += child.measuredHeight + margin / 2
                }

                mPrevItem = child

                if (filters.size < childCount) {
                    filters.put(child, Coord(mPrevX!!, mPrevY!!))
                }
            }
            height = if (height > 0) height + margin else 0
            mPrevHeight = height
        }

        return mPrevHeight
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(calculateSize(widthMeasureSpec, LayoutParams.MATCH_PARENT),
                calculateSize(heightMeasureSpec, calculateDesiredHeight()))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = event.x
                mStartY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.y - mStartY < -20) {
                    listener?.collapse()
                    mStartX = 0f
                    mStartY = 0f
                }
            }
        }

        return true
    }
}
