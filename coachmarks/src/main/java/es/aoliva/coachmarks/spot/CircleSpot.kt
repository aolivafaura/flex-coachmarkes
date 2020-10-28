package es.aoliva.coachmarks.spot

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

internal class CircleSpot(rectF: RectF, private val radius: Float, animate: Boolean) :
    Spot(rectF, animate) {

    override val rounded = radius
    override val width = radius
    override val height = radius

    override fun draw(canvas: Canvas, paint: Paint): Boolean {
        if (currentRect == null) {
            currentRect = calculateCurrentRect()
            canvas.drawRoundRect(currentRect!!, radius, radius, paint)
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

    private fun calculatePixelsToExpand(): Int {
        // This is a circle, so check just one side is all right
        return if ((currentRect!!.right + SpotView.PIXELS_PER_FRAME) <= rectF.right) SpotView.PIXELS_PER_FRAME
        else (rectF.right - currentRect!!.right).toInt()
    }

    private fun calculatePixelsToCollapse(): Int {
        // This is a circle, so check just one side is all right
        return if (currentRect!!.right - currentRect!!.left >= SpotView.PIXELS_PER_FRAME) SpotView.PIXELS_PER_FRAME
        else (currentRect!!.right - currentRect!!.left).toInt()
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