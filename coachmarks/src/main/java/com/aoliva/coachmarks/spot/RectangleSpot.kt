package com.aoliva.coachmarks.spot

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.ceil

internal class RectangleSpot(
    rectF: RectF,
    override val height: Float,
    override val width: Float,
    override val rounded: Float,
    animate: Boolean,
    animationVelocity: Int
) : Spot(rectF, animate, animationVelocity) {

    private var pixelsPerFrameX: Int = 0
    private var pixelsPerFrameY: Int = 0

    override fun draw(canvas: Canvas, paint: Paint): Boolean {
        if (currentRect == null) {
            currentRect = calculateCurrentRect()
            when {
                height > width -> {
                    pixelsPerFrameX = calculatePixelsPerFrameProportion()
                    pixelsPerFrameY = animationVelocity
                }
                width > height -> {
                    pixelsPerFrameY = calculatePixelsPerFrameProportion()
                    pixelsPerFrameX = animationVelocity
                }
                else -> {
                    pixelsPerFrameX = animationVelocity
                    pixelsPerFrameY = animationVelocity
                }
            }
            canvas.drawRoundRect(currentRect!!, rounded, rounded, paint)
            return true
        }

        when (direction) {
            EXPAND -> {
                if (currentRect!!.left <= rectF.left) {
                    canvas.drawRoundRect(rectF, rounded, rounded, paint)
                    return false
                }
                val pixelsToExpandX = calculatePixelsToExpandX()
                val pixelsToExpandY = calculatePixelsToExpandY()
                currentRect!!.left = currentRect!!.left - pixelsToExpandX
                currentRect!!.right = currentRect!!.right + pixelsToExpandX
                currentRect!!.top = currentRect!!.top - pixelsToExpandY
                currentRect!!.bottom = currentRect!!.bottom + pixelsToExpandY

                canvas.drawRoundRect(currentRect!!, rounded, rounded, paint)
                return true
            }
            COLLAPSE -> {
                if (currentRect!!.left >= currentRect!!.right) {
                    return false
                }
                val pixelsToCollapseX = calculatePixelsToCollapseX()
                val pixelsToCollapseY = calculatePixelsToCollapseY()
                currentRect!!.left = currentRect!!.left + pixelsToCollapseX
                currentRect!!.right = currentRect!!.right - pixelsToCollapseX
                currentRect!!.top = currentRect!!.top + pixelsToCollapseY
                currentRect!!.bottom = currentRect!!.bottom - pixelsToCollapseY

                canvas.drawRoundRect(currentRect!!, rounded, rounded, paint)
                return true
            }
            else -> return false
        }
    }

    private fun calculatePixelsToExpandX(): Int {
        return if ((currentRect!!.right + pixelsPerFrameX) <= rectF.right) pixelsPerFrameX
        else (rectF.right - currentRect!!.right).toInt()
    }

    private fun calculatePixelsToExpandY(): Int {
        return if ((currentRect!!.bottom + pixelsPerFrameY) <= rectF.bottom) pixelsPerFrameY
        else (rectF.bottom - currentRect!!.bottom).toInt()
    }

    private fun calculatePixelsToCollapseX(): Int {
        return if (currentRect!!.right - currentRect!!.left >= pixelsPerFrameX) pixelsPerFrameX
        else (currentRect!!.right - currentRect!!.left).toInt()
    }

    private fun calculatePixelsToCollapseY(): Int {
        return if (currentRect!!.bottom - currentRect!!.top >= pixelsPerFrameY) pixelsPerFrameY
        else (currentRect!!.bottom - currentRect!!.top).toInt()
    }

    private fun calculatePixelsPerFrameProportion(): Int {
        val maxLengthAxis = maxOf(height, width)
        val minLengthAxis = minOf(height, width)

        val proportion = minLengthAxis / maxLengthAxis
        return ceil(animationVelocity * proportion).toInt()
    }

    private fun calculateCurrentRect(): RectF {
        return if (direction == COLLAPSE) {
            rectF
        } else {
            val y = rectF.bottom - (rectF.bottom - rectF.top) / 2
            val x = rectF.right - (rectF.right - rectF.left) / 2
            RectF(x, y, x, y)
        }
    }
}
