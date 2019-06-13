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
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.log_list_filter_view_collapse.view.*
import sh.hadi.bark.R

/**
 * Created by galata on 20.09.16.
 */
class CollapseView : FrameLayout {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.log_list_filter_view_collapse, this, true)
    }

    internal fun setText(text: String) {
        buttonOk.text = text
    }

    internal fun setHasText(hasText: Boolean) {
        buttonOk.visibility = if (hasText) View.VISIBLE else GONE
    }

    internal fun rotateArrow(rotation: Float): Unit {
        imageArrow.rotation = rotation
    }

    internal fun turnIntoOkButton(ratio: Float) {
        if (buttonOk.visibility != View.VISIBLE) return
        scale(getIncreasingScale(ratio), getDecreasingScale(ratio))
    }

    internal fun turnIntoArrow(ratio: Float) {
        if (buttonOk.visibility != View.VISIBLE) return
        scale(getDecreasingScale(ratio), getIncreasingScale(ratio))
    }

    private fun getIncreasingScale(ratio: Float): Float = if (ratio < 0.5f) 0f else 2 * ratio - 1

    private fun getDecreasingScale(ratio: Float): Float = if (ratio > 0.5f) 0f else 1 - 2 * ratio

    private fun scale(okScale: Float, arrowScale: Float) {
        buttonOk.scaleX = okScale
        buttonOk.scaleY = okScale
        imageArrow.scaleX = arrowScale
        imageArrow.scaleY = arrowScale
    }

    override fun setOnClickListener(l: OnClickListener?) {
        buttonOk.setOnClickListener(l)
        imageArrow.setOnClickListener(l)
    }

}