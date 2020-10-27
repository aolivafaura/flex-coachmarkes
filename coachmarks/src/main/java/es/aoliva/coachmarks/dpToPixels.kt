package es.aoliva.coachmarks

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Created by antoniojoseolivafaura on 10/11/2017.
 */

internal fun dpToPixels(context: Context, dp: Int) = Math.round(dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))

internal fun pixelsToDp(context: Context, px: Int) = px / (context.resources.displayMetrics.densityDpi / 160f)

internal fun getDisplayWidhtPx(context: Context): Int = getDisplayMetrics(context).widthPixels

internal fun getDisplayMetrics(context: Context): DisplayMetrics {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics
}