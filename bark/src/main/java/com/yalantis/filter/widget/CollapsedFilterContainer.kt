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
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.RelativeLayout
import com.yalantis.filter.listener.CollapseListener
import kotlinx.android.synthetic.main.log_list_filter_collapsed_container.view.*
import sh.hadi.bark.R

/**
 * Created by galata on 20.09.16.
 */
class CollapsedFilterContainer : RelativeLayout {

    internal var listener: CollapseListener? = null

    private var mStartX = 0f
    private var mStartY = 0f

    var containerBackground = Color.WHITE
        set(value) {
            field = value
            relative_container.setBackgroundColor(value)
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        LayoutInflater.from(context).inflate(R.layout.log_list_filter_collapsed_container, this, true)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val isEmpty = collapsedFilter.childCount == 0
        val containsEvent = ev.x >= collapsedFilter.x && ev.x <= collapsedFilter.x + collapsedFilter.measuredWidth

        return isEmpty || !containsEvent
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = event.x
                mStartY = event.y
            }
            MotionEvent.ACTION_UP -> {
                if (!collapsedFilter.isBusy && isClick(mStartX, mStartY, event.x, event.y)) {
                    listener?.toggle()
                    mStartX = 0f
                    mStartY = 0f
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!collapsedFilter.isBusy && Math.abs(mStartX - event.x) < 20 && event.y - mStartY > 20) {
                    listener?.expand()
                    mStartX = 0f
                    mStartY = 0f
                } else if (!collapsedFilter.isBusy && Math.abs(mStartX - event.x) < 20 && event.y - mStartY < -20) {
                    listener?.collapse()
                    mStartX = 0f
                    mStartY = 0f
                }
            }
        }

        return true
    }
}