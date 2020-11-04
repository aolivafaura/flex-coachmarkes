package com.aoliva.coachmarks.extensions

import android.content.res.Resources

/**
 * Created by antoniojoseolivafaura on 10/11/2017.
 */
internal val Int.toDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

internal val Int.toPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
