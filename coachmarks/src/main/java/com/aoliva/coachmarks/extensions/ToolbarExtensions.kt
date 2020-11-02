package com.aoliva.coachmarks.extensions

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar

fun Toolbar.getHomeBackView(): View? {
    for (i in 0 until (this as ViewGroup).childCount) {
        val aux = (this as ViewGroup).getChildAt(i)
        if (aux is ImageButton) {
            if (aux.drawable == (this.navigationIcon)) {
                return aux
            }
        }
    }

    return null
}