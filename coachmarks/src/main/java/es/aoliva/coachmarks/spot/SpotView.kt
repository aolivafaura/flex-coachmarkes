package es.aoliva.coachmarks.spot

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import es.aoliva.coachmarks.R
import es.aoliva.coachmarks.spot.Spot.Companion.COLLAPSE

internal class SpotView : AppCompatImageView {

    private val paint = Paint(ANTI_ALIAS_FLAG)
    private val potterDuffClear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val potterDuffAdd = PorterDuffXfermode(PorterDuff.Mode.ADD)
    private val backgroundColor = R.color.black_70

    private val spots: MutableList<Spot> = mutableListOf()

    private var currentSpot: Spot? = null

    private var state: Int = IDLE

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

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
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
            state = ON_ANIMATION
            handler.postDelayed({ invalidate() }, 1)
        } else {
            state = IDLE
            currentSpot = spots.last()
        }
    }

    private fun refreshSpotIfNecessary(spot: Spot, canvas: Canvas): Boolean {
        return if (spot.animate) {
            spot.draw(canvas, paint)
        } else {
            canvas.drawRoundRect(spot.rectF, spot.rounded, spot.rounded, paint)
            false
        }
    }

    /**
     * Component touch listener
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        setOnTouchListener { _, event ->
            val isValidEvent = isEventInsideSpot(event) && state == IDLE
            if (isValidEvent) {
                event.action != MotionEvent.ACTION_DOWN
            } else {
                true
            }
        }
    }

    private fun isEventInsideSpot(event: MotionEvent): Boolean {
        return currentSpot!!.rectF.contains(event.x, event.y)
    }

    companion object {
        internal const val PIXELS_PER_FRAME = 5

        private const val IDLE = 0
        private const val ON_ANIMATION = 1
    }
}