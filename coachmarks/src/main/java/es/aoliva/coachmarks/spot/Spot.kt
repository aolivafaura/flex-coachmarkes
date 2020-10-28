package es.aoliva.coachmarks.spot

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

internal abstract class Spot(val rectF: RectF, val animate: Boolean, val animationVelocity: Int) {

    internal var direction: Int = 0
    internal var currentRect: RectF? = null

    abstract val rounded: Float
    abstract val width: Float
    abstract val height: Float

    abstract fun draw(canvas: Canvas, paint: Paint): Boolean

    companion object {
        const val EXPAND = 1
        const val COLLAPSE = 2
    }
}
