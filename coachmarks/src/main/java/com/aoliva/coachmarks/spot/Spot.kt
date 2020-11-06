package com.aoliva.coachmarks.spot

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

internal abstract class Spot(
    val rectF: RectF,
    val animate: Boolean,
    val animationVelocity: Int,
    centerCoordinates: IntArray
) {

    internal var state: SpotState = SpotState.ON_ANIMATION
    internal var direction: Int = 0
    internal var currentRect: RectF? = null
    internal var targetViewRect: RectF? = null

    internal abstract val rounded: Float
    internal abstract val width: Float
    internal abstract val height: Float

    internal var centerCoordinates: IntArray
        private set

    init {
        this.centerCoordinates = centerCoordinates
    }

    abstract fun draw(
        canvas: Canvas,
        paint: Paint
    ): Boolean

    abstract fun drawIdle(canvas: Canvas, paint: Paint)

    protected fun calculateCurrentRect(): RectF {
        return if (direction == COLLAPSE) {
            rectF
        } else {
            val y = rectF.bottom - (rectF.bottom - rectF.top) / 2
            val x = rectF.right - (rectF.right - rectF.left) / 2
            RectF(x, y, x, y)
        }
    }

    enum class SpotState {
        ON_ANIMATION,
        IDLE
    }

    companion object {
        const val EXPAND = 1
        const val COLLAPSE = 2
    }
}
