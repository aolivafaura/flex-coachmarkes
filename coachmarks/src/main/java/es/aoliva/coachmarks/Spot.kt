package es.aoliva.coachmarks

import android.graphics.RectF

internal data class Spot(val rectF: RectF, val radius: Float, val animate: Boolean) {

    internal var direction: Int = 0
    internal var currentRect: RectF? = null

    companion object {
        const val EXPAND = 1
        const val COLLAPSE = 2
    }
}