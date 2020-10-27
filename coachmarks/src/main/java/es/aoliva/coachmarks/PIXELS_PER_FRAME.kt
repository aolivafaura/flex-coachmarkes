package es.aoliva.coachmarks

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import es.aoliva.coachmarks.Spot.Companion.COLLAPSE
import es.aoliva.coachmarks.Spot.Companion.EXPAND

private const val PIXELS_PER_FRAME = 5

internal class SpotView : AppCompatImageView {

    private val paint = Paint(ANTI_ALIAS_FLAG)
    private val potterDuffClear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val potterDuffAdd = PorterDuffXfermode(PorterDuff.Mode.ADD)
    private val backgroundColor = R.color.black_70

    private val spots: MutableList<Spot> = mutableListOf()

    constructor(context: Context) : super(context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // Capture touch events
        setupTouchListener()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // Capture touch events
        setupTouchListener()
    }

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // Capture touch events
        setupTouchListener()
    }

    fun addSpot(spot: Spot) {
        spots.add(spot)
    }

    fun removeSpot(spot: Spot) {
        val localSpot = spots[(spots.indexOf(spot))]
        if (spot.animate) {
            localSpot.currentRect = spot.rectF
            localSpot.direction = COLLAPSE
        } else {
            spots.remove(localSpot)
        }
    }

    fun removeLastSpot() {
        if (spots.isNotEmpty()) {
            removeSpot(spots.last())
        }
    }

    fun startSequence() {
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        paint.xfermode = potterDuffAdd
        paint.color = ContextCompat.getColor(context, backgroundColor)
        canvas.drawPaint(paint)

        paint.style = Paint.Style.FILL
        paint.xfermode = potterDuffClear

        var shouldInvalidate = false

        for (spot in spots) {
            if (refreshSpotIfNecessary(spot, canvas)) {
                shouldInvalidate = true
            }
        }

        if (shouldInvalidate) {
            handler.postDelayed({ invalidate() }, 1)
        }
    }

    private fun refreshSpotIfNecessary(spot: Spot, canvas: Canvas): Boolean {
        if (spot.animate) {
            if (spot.currentRect == null) {
                spot.currentRect = calculateCurrentRect(spot)
                canvas.drawRoundRect(spot.currentRect!!, spot.radius, spot.radius, paint)
                return true
            }

            when (spot.direction) {
                EXPAND -> {
                    if (spot.currentRect!!.left <= spot.rectF.left) {
                        canvas.drawRoundRect(spot.rectF, spot.radius, spot.radius, paint)
                        return false
                    }
                    val pixelsToExpand = calculatePixelsToExpand(spot)
                    spot.currentRect!!.left = spot.currentRect!!.left - pixelsToExpand
                    spot.currentRect!!.right = spot.currentRect!!.right + pixelsToExpand
                    spot.currentRect!!.top = spot.currentRect!!.top - pixelsToExpand
                    spot.currentRect!!.bottom = spot.currentRect!!.bottom + pixelsToExpand

                    canvas.drawRoundRect(spot.currentRect!!, spot.radius, spot.radius, paint)
                    return true
                }
                COLLAPSE -> {
                    if (spot.currentRect!!.left >= spot.currentRect!!.right) {
                        return false
                    }
                    val pixelsToCollapse = calculatePixelsToCollapse(spot)
                    spot.currentRect!!.left = spot.currentRect!!.left + pixelsToCollapse
                    spot.currentRect!!.right = spot.currentRect!!.right - pixelsToCollapse
                    spot.currentRect!!.top = spot.currentRect!!.top + pixelsToCollapse
                    spot.currentRect!!.bottom = spot.currentRect!!.bottom - pixelsToCollapse

                    canvas.drawRoundRect(spot.currentRect!!, spot.radius, spot.radius, paint)
                    return true
                }
                else -> return false
            }
        } else {
            canvas.drawRoundRect(spot.rectF, spot.radius, spot.radius, paint)
            return false
        }
    }

    private fun calculatePixelsToExpand(spot: Spot): Int {
        // This is a circle, so check just one side is all right
        return if ((spot.currentRect!!.right + PIXELS_PER_FRAME) <= spot.rectF.right) PIXELS_PER_FRAME
        else (spot.rectF.right - spot.currentRect!!.right).toInt()
    }

    private fun calculatePixelsToCollapse(spot: Spot): Int {
        // This is a circle, so check just one side is all right
        return if (spot.currentRect!!.right - spot.currentRect!!.left >= PIXELS_PER_FRAME) PIXELS_PER_FRAME
        else (spot.currentRect!!.right - spot.currentRect!!.left).toInt()
    }

    private fun calculateCurrentRect(spot: Spot): RectF {
        return if (spot.direction == COLLAPSE) {
            spot.rectF
        } else {
            val y = spot.rectF.bottom - (spot.rectF.bottom - spot.rectF.top) / 2
            val x = spot.rectF.right - (spot.rectF.right - spot.rectF.left) / 2
            RectF(x, y, x, y)
        }
    }

    /**
     * Component touch listener
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        setOnTouchListener { _, _ -> true }
    }
}