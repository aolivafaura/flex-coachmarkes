package com.aoliva.coachmarks.spot

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

internal class CircleSpot(
    rectF: RectF,
    private val radius: Float,
    animate: Boolean,
    animationVelocity: Int,
    centerCoordinates: IntArray
) : Spot(rectF, animate, animationVelocity, centerCoordinates) {

    override val rounded = radius
    override val width = radius
    override val height = radius

    override fun draw(
        canvas: Canvas,
        paint: Paint
    ): Boolean {
        if (currentRect == null) {
            drawFirstSpot(canvas, paint)
            return true
        }
        when (direction) {
            EXPAND -> {
                if (currentRect!!.left <= rectF.left) {
                    canvas.drawRoundRect(rectF, radius, radius, paint)
                    return false
                }
                val pixelsToExpand = calculatePixelsToExpand()
                currentRect!!.left = currentRect!!.left - pixelsToExpand
                currentRect!!.right = currentRect!!.right + pixelsToExpand
                currentRect!!.top = currentRect!!.top - pixelsToExpand
                currentRect!!.bottom = currentRect!!.bottom + pixelsToExpand

                canvas.drawRoundRect(currentRect!!, radius, radius, paint)
                return true
            }
            COLLAPSE -> {
                if (currentRect!!.left >= currentRect!!.right) {
                    return false
                }
                val pixelsToCollapse = calculatePixelsToCollapse()
                currentRect!!.left = currentRect!!.left + pixelsToCollapse
                currentRect!!.right = currentRect!!.right - pixelsToCollapse
                currentRect!!.top = currentRect!!.top + pixelsToCollapse
                currentRect!!.bottom = currentRect!!.bottom - pixelsToCollapse

                canvas.drawRoundRect(currentRect!!, radius, radius, paint)
                return true
            }
            else -> return false
        }
    }

    override fun drawIdle(canvas: Canvas, paint: Paint) {
        canvas.drawRoundRect(rectF, radius, radius, paint)
    }

    private fun drawFirstSpot(canvas: Canvas, paint: Paint) {
        currentRect = calculateCurrentRect()
        canvas.drawRoundRect(currentRect!!, radius, radius, paint)
    }

    private fun calculatePixelsToExpand(): Int {
        // This is a circle, so check just one side is all right
        return when {
            (currentRect!!.right + animationVelocity) <= rectF.right -> animationVelocity
            else -> (rectF.right - currentRect!!.right).toInt()
        }
    }

    private fun calculatePixelsToCollapse(): Int {
        // This is a circle, so check just one side is all right
        return when {
            currentRect!!.right - currentRect!!.left >= animationVelocity -> animationVelocity
            else -> (currentRect!!.right - currentRect!!.left).toInt()
        }
    }
}
