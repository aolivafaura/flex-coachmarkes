package com.aoliva.coachmarks.spot

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
import com.aoliva.coachmarks.Coachmark
import com.aoliva.coachmarks.R
import com.aoliva.coachmarks.TAG
import com.aoliva.coachmarks.spot.Spot.Companion.COLLAPSE
import com.aoliva.coachmarks.spot.Spot.Companion.EXPAND

internal class SpotView : AppCompatImageView {

    private val paint = Paint(ANTI_ALIAS_FLAG)
    private val potterDuffClear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val potterDuffAdd = PorterDuffXfermode(PorterDuff.Mode.ADD)
    private val backgroundColor = R.color.black_70

    private val spots: MutableList<Spot> = mutableListOf()
    private val spotsIndices: HashMap<Spot, Int> = hashMapOf()

    private var currentSpot: Spot? = null
    private var stateListener: (Coachmark.CoachMarkState, Int) -> Unit =
        { _, _ -> }
    private var targetViewClickListener: ((Int) -> Unit) = {}

    internal var onOverlayInteracted: (() -> Unit)? = null
    internal var allowInteractions = false

    constructor(context: Context, stateListener: (Coachmark.CoachMarkState, Int) -> Unit, targetViewClickListener: ((Int) -> Unit)) : super(
        context
    ) {
        this.stateListener = stateListener
        this.targetViewClickListener = targetViewClickListener
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet
    ) : super(context, attrs)

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        // Capture touch events
        setupTouchListener()
    }

    fun addSpot(spot: Spot, index: Int) {
        spots.add(spot)
        spotsIndices[spot] = index
        currentSpot = spots.last()
    }

    internal fun removeLastSpot() {
        if (spots.isNotEmpty()) {
            val lastSpot = spots.last()
            lastSpot.takeIf { it.animate }?.apply {
                currentRect = rectF
                direction = COLLAPSE
                state = Spot.SpotState.ON_ANIMATION
            } ?: run {
                spots.removeLast()
            }
        }
    }

    internal fun destroyLastSpot() {
        if (spots.isNotEmpty()) {
            spots.removeLast()
        }
    }

    internal fun startSequence() = invalidate()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.xfermode = potterDuffAdd
        paint.color = ContextCompat.getColor(context, backgroundColor)
        canvas.drawPaint(paint)

        paint.style = Paint.Style.FILL
        paint.xfermode = potterDuffClear

        var shouldInvalidate = false
        var shouldDelete: Spot? = null

        for (spot in spots) {
            if (refreshSpotIfNecessary(spot, canvas)) {
                shouldInvalidate = true
            } else if (spot.state == Spot.SpotState.ON_ANIMATION) {
                val index = spotsIndices[spot] ?: -1
                when (spot.direction) {
                    EXPAND -> {
                        stateListener(Coachmark.CoachMarkState.OPENED, index)
                        spot.state = Spot.SpotState.IDLE
                    }
                    COLLAPSE -> {
                        stateListener(Coachmark.CoachMarkState.CLOSED, index)
                        shouldDelete = spot
                    }
                }
            }
        }

        if (shouldInvalidate) {
            handler.postDelayed({ invalidate() }, 1)
        }
        shouldDelete?.let { spots.remove(it) }
    }

    private fun refreshSpotIfNecessary(spot: Spot, canvas: Canvas): Boolean {
        return if (spot.animate && spot.state == Spot.SpotState.ON_ANIMATION) {
            spot.draw(canvas, paint)
        } else {
            spot.drawIdle(canvas, paint)
            false
        }
    }

    /**
     * Component touch listener
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        setOnTouchListener { _, event ->
            onOverlayInteracted?.invoke()
            if (allowInteractions) {
                false
            } else {
                val isValidEvent =
                    isEventInsideSpot(event) && currentSpot?.state == Spot.SpotState.IDLE
                if (isValidEvent) {
                    currentSpot?.let {
                        targetViewClickListener.invoke(spotsIndices[currentSpot] ?: -1)
                    }
                    event.action != MotionEvent.ACTION_DOWN
                } else {
                    true
                }
            }
        }
    }

    private fun isEventInsideSpot(event: MotionEvent): Boolean {
        return currentSpot?.targetViewRect?.contains(event.x, event.y) == true
    }
}
