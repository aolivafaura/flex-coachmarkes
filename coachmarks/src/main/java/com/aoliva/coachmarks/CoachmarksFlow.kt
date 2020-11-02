package com.aoliva.coachmarks

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.aoliva.coachmarks.spot.CircleSpot
import com.aoliva.coachmarks.spot.RectangleSpot
import com.aoliva.coachmarks.spot.Spot
import com.aoliva.coachmarks.spot.Spot.Companion.EXPAND
import com.aoliva.coachmarks.spot.SpotView
import com.aoliva.coachmarks.util.dpToPixels
import com.aoliva.coachmarks.util.getDisplayWidhtPx
import com.aoliva.coachmarks.util.pixelsToDp

class CoachmarksFlow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // VARIABLES -----------------------------------------------------------------------------------
    private var spotView: SpotView? = null

    private var steps: List<Coachmark<View>>? = null
    private var currentStep = 0

    private val relatedViewId = R.id.view_id

    var dismissListener: OnCoackmarkDismissedListener? = null
    var initialDelay = 200L
    var animate = true
    var animationVelocity = 8

    /**
     * Notifies when coachmark view is dismissed
     */
    interface OnCoackmarkDismissedListener {
        fun onCoachmarkDismissed()
    }

    private fun initView(builder: Builder): CoachmarksFlow {
        steps = builder.steps
        initialDelay = builder.initialDelay
        animate = builder.animate
        if (builder.animate && builder.animationVelocity > 0) {
            animationVelocity = builder.animationVelocity
        }

        return this
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        Log.v(TAG, "Attached to window")
        steps?.let {
            drawStep(steps!![currentStep])
        } ?: Log.v(TAG, "Please set desired steps before invoke show method")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.v(TAG, "Detached from window")


    }

    // PUBLIC METHODS ------------------------------------------------------------------------------
    /**
     * Steps will be executed in list order
     *
     * @param steps
     */
    fun <T : View> setSteps(steps: List<Coachmark<T>>) {
        this.steps = steps as List<Coachmark<View>>
    }

    /**
     * @return `true` whether there is another step left, `false` if isn't
     */
    fun hasNextStep(): Boolean = currentStep < steps!!.size - 1

    /**
     * Advance to next step
     */
    fun goNextStep() {
        if (!hasNextStep()) {
            close()
        } else {
            drawStep(steps!![++currentStep])
        }
    }

    /**
     * Close view
     */
    fun close() {
        fadeOut {
            (parent as ViewGroup).removeView(this)
            dismissListener?.onCoachmarkDismissed()
        }
    }

    fun getCurrentStepView() {
        steps?.let {
            steps!![currentStep]
        } ?: Log.v(TAG, "There is no steps defined")
    }

    fun show() {
        Handler(Looper.getMainLooper()).postDelayed({
            val vg = getActivity()?.window?.decorView?.rootView as ViewGroup
            val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )

            layoutParams = params
            visibility = View.INVISIBLE
            vg.addView(this@CoachmarksFlow)
            fadeIn(1)
        }, initialDelay)
    }

    // PRIVATE METHODS -----------------------------------------------------------------------------
    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    private fun drawStep(item: Coachmark<View>) {
        removeView(findViewById(relatedViewId))

        val focusView: View? =
            if (item.target != null) item.target
            else findOnViewForId(null, item.targetId)


        if (focusView == null) {
            Log.w(
                "CUSTOM COACH MARK",
                "There is no view detected with given Id: " + item.targetId
            )
            close()
            return
        }

        val spotWidth =
            when {
                item.sizePercentage > 0 -> {
                    pixelsToDp(
                        context,
                        (focusView.width * (item.sizePercentage / 100)).toInt()
                    ).toInt()
                }
                else -> pixelsToDp(context, focusView.width).toInt()
            }

        val center = calculateCenter(focusView)
        val radius = dpToPixels(context, spotWidth / 2)

        val spot = when (item.shape) {
            Coachmark.Shape.CIRCLE -> {
                drawSpot(center, radius)
            }
            Coachmark.Shape.RECTANGLE -> {
                val height = when {
                    item.sizePercentage > 0 -> {
                        pixelsToDp(
                            context,
                            (focusView.height * (item.sizePercentage / 100)).toInt()
                        ).toInt()
                    }
                    else -> pixelsToDp(context, focusView.height).toInt()
                }
                drawSpot(center, height, radius, item.cornerRadius)
            }
        }
        assignOriginalTargetRect(spot, focusView)
        val anchorPoint = calculateAnchorPoint(center, item, spot)
        calculateRelatedViewMaxWidth(center, item, spotWidth)
        drawRelatedView(item, anchorPoint)
    }

    private fun assignOriginalTargetRect(item: Spot, focusView: View?) {
        val coordinates = IntArray(2)
        focusView!!.getLocationInWindow(coordinates)

        item.targetViewRect = RectF(
            coordinates[0].toFloat(),
            coordinates[1].toFloat(),
            (coordinates[0] + focusView.width).toFloat(),
            (coordinates[1] + focusView.height).toFloat()
        )
    }

    private fun calculateCenter(focusView: View?): IntArray {
        val coordinates = IntArray(2)
        focusView!!.getLocationInWindow(coordinates)

        coordinates[0] = coordinates[0] + focusView.width / 2
        coordinates[1] = coordinates[1] + focusView.height / 2

        return coordinates
    }

    private fun calculateAnchorPoint(
        center: IntArray,
        item: Coachmark<View>,
        spot: Spot
    ): IntArray {
        val anchorPoint = intArrayOf(center[0], center[1])

        when (item.position) {
            Coachmark.Position.BOTTOM -> anchorPoint[1] = anchorPoint[1] + spot.height.toInt()
            Coachmark.Position.TOP -> anchorPoint[1] = anchorPoint[1] - spot.height.toInt()
            Coachmark.Position.LEFT -> anchorPoint[0] = anchorPoint[0] - spot.width.toInt()
            Coachmark.Position.RIGHT -> anchorPoint[0] = anchorPoint[0] + spot.width.toInt()
        }

        val padding = item.paddings

        if (padding[0] > 0) {
            anchorPoint[1] = anchorPoint[1] + dpToPixels(context, padding[0])
        }
        if (padding[1] > 0) {
            anchorPoint[0] = anchorPoint[0] + dpToPixels(context, padding[1])
        }
        if (padding[2] > 0) {
            anchorPoint[0] = anchorPoint[0] - dpToPixels(context, padding[2])
        }
        if (padding[3] > 0) {
            anchorPoint[1] = anchorPoint[1] - dpToPixels(context, padding[3])
        }

        return anchorPoint
    }

    private fun drawSpot(coordinates: IntArray, height: Int, width: Int, cornerRadius: Int): Spot {

        if (spotView == null) {
            spotView = SpotView(context)
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            addView(spotView, params)
        }

        val topCoordinate = coordinates[1] - height
        val bottomCoordinate = coordinates[1] + height
        val leftCoordinate = coordinates[0] - width
        val rightCoordinate = coordinates[0] + width

        val rect = RectF(
            leftCoordinate.toFloat(),
            topCoordinate.toFloat(),
            rightCoordinate.toFloat(),
            bottomCoordinate.toFloat()
        )

        val spot = RectangleSpot(
            rect,
            height.toFloat(),
            width.toFloat(),
            cornerRadius.toFloat(),
            animate,
            animationVelocity
        )
        spot.direction = EXPAND

        spotView?.removeLastSpot()
        spotView?.addSpot(spot)
        spotView?.startSequence()

        return spot
    }

    private fun drawSpot(coordinates: IntArray, radius: Int): Spot {

        if (spotView == null) {
            spotView = SpotView(context)
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            addView(spotView, params)
        }

        val topCoordinate = coordinates[1] - radius
        val bottomCoordinate = coordinates[1] + radius
        val leftCoordinate = coordinates[0] - radius
        val rightCoordinate = coordinates[0] + radius

        val rect = RectF(
            leftCoordinate.toFloat(), topCoordinate.toFloat(), rightCoordinate.toFloat(),
            bottomCoordinate.toFloat()
        )

        val spot = CircleSpot(rect, radius.toFloat(), animate, animationVelocity)
        spot.direction = EXPAND

        spotView?.removeLastSpot()
        spotView?.addSpot(spot)
        spotView?.startSequence()

        return spot
    }

    private fun drawRelatedView(item: Coachmark<View>, anchorPoint: IntArray) {
        val contentLayout = ConstraintLayout(context)
        addView(
            contentLayout, RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val relatedView = item.relatedSpotView
        relatedView?.id = relatedViewId
        relatedView?.visibility = View.INVISIBLE

        val maxWidth = dpToPixels(context, item.maxWidth)

        relatedView?.parent?.let {
            (relatedView.parent as ViewGroup).removeView(relatedView)
        }

        contentLayout.addView(
            relatedView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
        )

        relatedView?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (maxWidth > 0 && relatedView.width > maxWidth) {
                    requestLayout()
                    relatedView.layoutParams.width = maxWidth
                }

                relatedView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                setConstraints(item, contentLayout, anchorPoint)
            }
        })
    }

    private fun setConstraints(
        item: Coachmark<View>, contentLayout: ConstraintLayout,
        anchorPoint: IntArray
    ) {

        val verticalGuideId = R.id.vertical_guide
        val horizontalGuideId = R.id.horizontal_guide

        val constraintSet = ConstraintSet()
        constraintSet.clone(contentLayout)
        constraintSet.create(horizontalGuideId, ConstraintSet.HORIZONTAL_GUIDELINE)
        constraintSet.create(verticalGuideId, ConstraintSet.VERTICAL_GUIDELINE)

        constraintSet.setGuidelineBegin(horizontalGuideId, anchorPoint[1])
        constraintSet.setGuidelineBegin(verticalGuideId, anchorPoint[0])
        constraintSet.applyTo(contentLayout)

        when (item.position) {
            Coachmark.Position.TOP -> {
                constraintSet.connect(
                    relatedViewId, ConstraintSet.BOTTOM, horizontalGuideId,
                    ConstraintSet.TOP, 0
                )
                constraintSet.setVerticalBias(relatedViewId, 100f)
            }
            Coachmark.Position.BOTTOM -> {
                constraintSet.connect(
                    relatedViewId, ConstraintSet.TOP, horizontalGuideId,
                    ConstraintSet.BOTTOM
                )
                constraintSet.setVerticalBias(relatedViewId, 0f)
            }
            Coachmark.Position.LEFT -> {
                constraintSet.connect(
                    relatedViewId, ConstraintSet.RIGHT, verticalGuideId,
                    ConstraintSet.LEFT
                )
                constraintSet.setHorizontalBias(relatedViewId, 100f)
            }
            Coachmark.Position.RIGHT -> {
                constraintSet.connect(
                    relatedViewId, ConstraintSet.LEFT, verticalGuideId,
                    ConstraintSet.RIGHT
                )
                constraintSet.setHorizontalBias(relatedViewId, 0f)
            }
        }

        when (item.alignment) {
            Coachmark.Alignment.CENTER ->
                if (item.position == Coachmark.Position.TOP || item.position == Coachmark.Position.BOTTOM) {
                    constraintSet.connect(
                        relatedViewId, ConstraintSet.LEFT, verticalGuideId,
                        ConstraintSet.LEFT
                    )
                    constraintSet.connect(
                        relatedViewId, ConstraintSet.RIGHT, verticalGuideId,
                        ConstraintSet.RIGHT
                    )
                } else {
                    constraintSet.connect(
                        relatedViewId, ConstraintSet.TOP, horizontalGuideId,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        relatedViewId, ConstraintSet.BOTTOM,
                        horizontalGuideId, ConstraintSet.BOTTOM
                    )
                }
            Coachmark.Alignment.TOP -> {
                constraintSet.connect(
                    relatedViewId, ConstraintSet.BOTTOM, horizontalGuideId,
                    ConstraintSet.TOP
                )
                constraintSet.setVerticalBias(relatedViewId, 100f)
            }
            Coachmark.Alignment.BOTTOM -> {
                constraintSet.connect(
                    relatedViewId, ConstraintSet.TOP, horizontalGuideId,
                    ConstraintSet.BOTTOM
                )
                constraintSet.setVerticalBias(relatedViewId, 0f)
            }
            Coachmark.Alignment.LEFT -> {
                constraintSet.connect(
                    relatedViewId, ConstraintSet.RIGHT, verticalGuideId,
                    ConstraintSet.LEFT
                )
                constraintSet.setVerticalBias(relatedViewId, 100f)
            }
            Coachmark.Alignment.RIGHT -> constraintSet.connect(
                relatedViewId, ConstraintSet.LEFT, verticalGuideId,
                ConstraintSet.RIGHT
            )
        }

        constraintSet.applyTo(contentLayout)
        contentLayout.post { item.relatedSpotView?.visibility = View.VISIBLE }
    }

    private fun calculateRelatedViewMaxWidth(
        center: IntArray,
        item: Coachmark<View>,
        spotWidth: Int
    ) {

        val screenWidth = getDisplayWidhtPx(context)
        val margin = dpToPixels(context, 24)

        var width = screenWidth

        when (item.position) {
            Coachmark.Position.TOP, Coachmark.Position.BOTTOM -> if (item.alignment == Coachmark.Alignment.CENTER) {
                width -= margin * 2
            } else {
                width -= margin
                width -= dpToPixels(context, spotWidth / 2)

                if (item.alignment == Coachmark.Alignment.LEFT) {
                    if (item.paddings[2] != 0) {
                        width -= dpToPixels(context, item.paddings[2])
                    }
                    width -= (screenWidth - center[0])
                } else if (item.alignment == Coachmark.Alignment.RIGHT) {
                    if (item.paddings[1] != 0) {
                        width -= dpToPixels(context, item.paddings[1])
                    }
                    width -= center[0]
                }
            }
            Coachmark.Position.LEFT, Coachmark.Position.RIGHT -> {
                width -= margin
                if (item.position == Coachmark.Position.LEFT) {
                    if (item.paddings[2] != 0) {
                        width -= dpToPixels(context, item.paddings[2])
                    }
                    width -= screenWidth - (center[0] - dpToPixels(
                        context,
                        spotWidth / 2
                    ).toFloat()).toInt()
                } else if (item.position == Coachmark.Position.RIGHT) {
                    if (item.paddings[1] != 0) {
                        width -= dpToPixels(context, item.paddings[1])
                    }
                    width -= dpToPixels(context, spotWidth) / 2
                    width -= center[0]
                }
            }
        }

        item.maxWidth = pixelsToDp(context, width).toInt()
    }

    private fun findOnViewForId(viewGroup: ViewGroup?, resId: Int): View? {
        var localViewGroup = viewGroup

        if (localViewGroup == null) {
            localViewGroup = parent as ViewGroup
        }

        val childCount = localViewGroup.childCount
        var auxView: View

        for (i in 0 until childCount) {
            auxView = localViewGroup.getChildAt(i)

            if (auxView.id == resId) {
                return auxView
            } else if (auxView is ViewGroup) {
                val view = findOnViewForId(auxView, resId)
                if (view != null) {
                    return view
                }
            }
        }
        return null
    }

    class Builder(private val context: Context) {
        var steps = mutableListOf<Coachmark<View>>()
        var initialDelay = 0L
        var animate = true
        var animationVelocity = 0

        fun <TYPE : View> steps(listSteps: List<Coachmark<TYPE>>) =
            apply { steps.addAll(listSteps as List<Coachmark<View>>) }

        fun <TYPE : View> nextStep(step: Coachmark<TYPE>) =
            apply { steps.add(step as Coachmark<View>) }

        fun initialDelay(delay: Long) = apply { this.initialDelay = delay }
        fun withAnimation(animate: Boolean) = apply { this.animate = animate }
        fun animationVelocity(animationVelocity: Int) =
            apply { this.animationVelocity = animationVelocity }

        fun build() = CoachmarksFlow(context).initView(this)
    }

    companion object {
        fun with(context: Context): Builder = Builder(context)
    }
}
