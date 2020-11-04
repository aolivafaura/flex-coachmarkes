package com.aoliva.coachmarks.extensions

import android.content.res.Resources

/**
 * Get the status bar height from [Resources]
 * @return The height size of the status bar.
 */
val Resources.statusBarHeight: Int
    get() {
        val idStatusBarHeight: Int = getIdentifier("status_bar_height", "dimen", "android")
        return getDimensionPixelSize(idStatusBarHeight)
    }
