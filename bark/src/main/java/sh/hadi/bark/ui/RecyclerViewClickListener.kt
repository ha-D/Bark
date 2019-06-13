package sh.hadi.bark.ui

import android.view.View


interface RecyclerViewClickListener {

    fun onClick(view: View, position: Int)
}