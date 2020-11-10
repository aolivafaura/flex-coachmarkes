package com.aoliva.coachmarks

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.aoliva.coachmarks.extensions.*
import com.aoliva.coachmarks.spot.Spot
import com.aoliva.coachmarks.spot.SpotGenerator
import com.aoliva.coachmarks.spot.SpotView

class CoachMarksFlow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // VARIABLES -----------------------------------------------------------------------------------
    private var spotView: SpotView? = null

    var steps: List<Coachmark<View>>? = null
        private set
    private var currentStep = 0

    private var relatedViewId: Int = 0
    private var currentContentLayoutId: Int = 0

    var coachMarkListener: CoachMarkListener? = null
    private var initialDelay = 200L
    private var animate = true
    private var animationVelocity: AnimationVelocity = AnimationVelocity.NORMAL
    private var allowOverlaidViewsInteractions = false

    private var currentFocusView: View? = null
    private var currentLayoutChangeListener: OnLayoutChangeListener? = null

    /**
     * Notifies when coachmark view is dismissed
     */
    interface CoachMarkListener {
        fun onCoachmarkFlowClosed(closeAction: CoachmarkCloseAction)
        fun onCoachmarkFlowShowed()
        fun onChangedCoachMarkState(state: Coachmark.CoachMarkState, coachMarkIndex: Int)
        fun onTargetViewClick(coachMarkIndex: Int)
    }

    private fun initView(builder: Builder): CoachMarksFlow {
        steps = builder.steps
        initialDelay = builder.initialDelay
        animate = builder.animate
        animationVelocity = builder.animationVelocity
        allowOverlaidViewsInteractions = builder.allowOverlaidInteractions

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
     * @return `true` whether there is another step left, `false` if isn't
     */
    fun hasNextStep(): Boolean = currentStep < steps!!.size - 1

    /**
     * Advance to next step
     */
    fun goNextStep() {
        if (!hasNextStep()) {
            removeCoachMarkFlow(CoachmarkCloseAction.FLOW_ENDED)
        } else {
            drawStep(steps!![++currentStep])
        }
    }

    /**
     * Close view
     */
    fun close() {
        removeCoachMarkFlow(CoachmarkCloseAction.DISMISSED)
    }

    private fun removeCoachMarkFlow(closeAction: CoachmarkCloseAction) {
        currentFocusView?.takeIf { currentLayoutChangeListener != null }
            ?.removeOnLayoutChangeListener(
                currentLayoutChangeListener
            )
        fadeOut {
            (parent as ViewGroup).removeView(this)
            coachMarkListener?.onCoachmarkFlowClosed(closeAction)
        }
    }

    fun getCurrentStepView(): Coachmark<View>? {
        return steps?.let {
            steps!![currentStep]
        } ?: run {
            Log.v(TAG, "There is no steps defined")
            null
        }
    }

    fun show() {
        Handler(Looper.getMainLooper()).postDelayed({
            val vg = getActivity()?.window?.decorView?.rootView as ViewGroup
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )

            layoutParams = params
            visibility = View.INVISIBLE
            coachMarkListener?.onCoachmarkFlowShowed()
            vg.addView(this@CoachMarksFlow)
            addCloseButton()
            fadeIn(1)
        }, initialDelay)
    }

    // PRIVATE METHODS -----------------------------------------------------------------------------
    private fun drawStep(item: Coachmark<View>) {
        currentFocusView?.takeIf { currentLayoutChangeListener != null }
            ?.removeOnLayoutChangeListener(
                currentLayoutChangeListener
            )
        removeView(findViewById(relatedViewId))

        initSpotViewIfNecessary()

        val focusView: View? =
            if (item.target != null) item.target
            else (parent as ViewGroup).findViewById(item.targetId)

        if (focusView == null) {
            Log.w(
                "CUSTOM COACH MARK",
                "There is no view detected with given Id: " + item.targetId
            )
            removeCoachMarkFlow(CoachmarkCloseAction.DISMISSED)
            return
        }

        val spot = SpotGenerator.getSpot(item, focusView, animate, animationVelocity)
        initSequence(spot)

        drawRelatedView(item, spot, focusView)

        val layoutChangeListener =
            OnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (oldTop != top || oldBottom != bottom || oldRight != right || oldLeft != left) {
                    if (steps?.get(currentStep) == item) {
                        Log.v(TAG, "Redrawing step $currentStep....")
                        animate = false
                        spotView?.destroyLastSpot()
                        drawStep(item)
                        animate = true
                    }
                }
            }
        focusView.addOnLayoutChangeListener(layoutChangeListener)

        currentFocusView = focusView
        currentLayoutChangeListener = layoutChangeListener

        item.onRelatedSpotViewChanged = { animate ->
            if (steps!![currentStep] == item) {
                Log.v(TAG, "Replacing related view...")
                val currentRelatedView = this@CoachMarksFlow.findViewById<View>(relatedViewId)

                if (animate) {
                    currentRelatedView.fadeOut {
                        this@CoachMarksFlow.removeView(currentRelatedView)
                        drawRelatedView(item, spot, focusView, true)
                    }
                } else {
                    this@CoachMarksFlow.removeView(currentRelatedView)
                    drawRelatedView(item, spot, focusView)
                }
            }
        }
    }

    private fun initSpotViewIfNecessary() {
        if (spotView == null) {
            spotView = SpotView(context,
                { state, index ->
                    coachMarkListener?.onChangedCoachMarkState(state, index)
                },
                { index ->
                    coachMarkListener?.onTargetViewClick(index)
                }
            ).apply {
                allowInteractions = allowOverlaidViewsInteractions
                this.onOverlayInteracted = {
                    if (this@CoachMarksFlow.allowOverlaidViewsInteractions) {
                        removeCoachMarkFlow(CoachmarkCloseAction.OVERLAY)
                    }
                }
            }
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            addView(spotView, params)
        }
    }

    private fun initSequence(spot: Spot) {
        callListeners()
        spotView?.removeLastSpot()
        spotView?.addSpot(spot, currentStep)
        spotView?.startSequence()
    }

    private fun callListeners() {
        if (currentStep != 0) {
            coachMarkListener?.onChangedCoachMarkState(
                if (animate) Coachmark.CoachMarkState.CLOSING
                else Coachmark.CoachMarkState.CLOSED,
                currentStep - 1
            )
        }
        if (animate) {
            coachMarkListener?.onChangedCoachMarkState(
                Coachmark.CoachMarkState.OPENING,
                currentStep
            )
        }
    }

    private fun drawRelatedView(
        item: Coachmark<View>,
        spot: Spot,
        focusView: View,
        animate: Boolean = false
    ) {
        findViewById<ViewGroup>(currentContentLayoutId)?.let {
            (it.parent as ViewGroup).removeView(it)
            it.removeAllViews()
        }

        val relatedView = item.relatedSpotView
        requireNotNull(relatedView) { "Related view cannot be null!" }

        val contentLayout = ConstraintLayout(context).apply {
            id = View.generateViewId()
            currentContentLayoutId = id
        }
        addView(
            contentLayout, LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        relatedView.visibility = View.INVISIBLE
        assignViewRelatedId(relatedView)

        contentLayout.addView(
            relatedView, ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
        )

        if (animate) {
            relatedView.fadeIn()
        }

        relatedView.viewTreeObserver?.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (item.maxWidth > 0 && relatedView.width > item.maxWidth) {
                    requestLayout()
                    relatedView.layoutParams.width = item.maxWidth
                }

                relatedView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                setRelatedViewConstraints(item, contentLayout, spot, focusView)
            }
        })
    }

    private fun assignViewRelatedId(relatedView: View) {
        relatedView.id = View.generateViewId()
        relatedViewId = relatedView.id
    }

    private fun addCloseButton() {
        val imageView = ImageView(context).apply {
            id = View.generateViewId()
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = MARGIN_CLOSE_BUTTON.toPx + resources.statusBarHeight
                marginEnd = MARGIN_CLOSE_BUTTON.toPx
                addRule(ALIGN_PARENT_END)
            }
            setImageResource(R.drawable.ic_close_white)
            setOnClickListener { removeCoachMarkFlow(CoachmarkCloseAction.CLOSE_BUTTON) }
        }
        addView(imageView)
    }

    private fun setRelatedViewConstraints(
        item: Coachmark<View>,
        contentLayout: ConstraintLayout,
        spot: Spot,
        focusView: View
    ) {
        val coordinates = IntArray(2)
        focusView.getLocationInWindow(coordinates)

        val wDifference = ((spot.width * 2) - focusView.width).toInt()
        val hDifference = ((spot.height * 2) - focusView.height).toInt()

        val view = View(context).apply {
            id = View.generateViewId()
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                width = focusView.width + wDifference
                height = focusView.height + hDifference
            }
        }
        //view.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))

        contentLayout.addView(view)

        val constraintSet = ConstraintSet()
        constraintSet.clone(contentLayout)

        constraintSet.connect(
            view.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            coordinates[1] - (hDifference / 2)
        )
        constraintSet.connect(
            view.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START,
            coordinates[0] - (wDifference / 2)
        )

        for (connection in item.connections) {
            val startSide = when (connection.relatedViewConnection) {
                Coachmark.ConnectionEdge.TOP -> ConstraintSet.TOP
                Coachmark.ConnectionEdge.BOTTOM -> ConstraintSet.BOTTOM
                Coachmark.ConnectionEdge.START -> ConstraintSet.START
                Coachmark.ConnectionEdge.END -> ConstraintSet.END
            }
            val targetId = when (connection.anchorView) {
                Coachmark.AnchorView.PARENT -> ConstraintSet.PARENT_ID
                Coachmark.AnchorView.TARGET -> view.id
            }
            val endSide = when (connection.anchorViewConnection) {
                Coachmark.ConnectionEdge.TOP -> ConstraintSet.TOP
                Coachmark.ConnectionEdge.BOTTOM -> ConstraintSet.BOTTOM
                Coachmark.ConnectionEdge.START -> ConstraintSet.START
                Coachmark.ConnectionEdge.END -> ConstraintSet.END
            }

            if (connection.margin != 0 && targetId != ConstraintSet.PARENT_ID) {
                val id = View.generateViewId()
                val guideType = when (connection.anchorViewConnection) {
                    Coachmark.ConnectionEdge.TOP, Coachmark.ConnectionEdge.BOTTOM -> {
                        ConstraintSet.HORIZONTAL_GUIDELINE
                    }
                    else -> {
                        ConstraintSet.VERTICAL_GUIDELINE
                    }
                }
                constraintSet.create(id, guideType)
                val aCoords = IntArray(2)
                view.getLocationInWindow(aCoords)
                val thePoint = when (connection.anchorViewConnection) {
                    Coachmark.ConnectionEdge.TOP -> {
                        coordinates[1] - (hDifference / 2) + connection.margin
                    }
                    Coachmark.ConnectionEdge.BOTTOM -> {
                        coordinates[1] - (hDifference / 2) + view.height + connection.margin
                    }
                    Coachmark.ConnectionEdge.START -> {
                        coordinates[0] - (wDifference / 2) + connection.margin
                    }
                    Coachmark.ConnectionEdge.END -> {
                        coordinates[0] - (wDifference / 2) + view.width + connection.margin
                    }
                }
                constraintSet.setGuidelineBegin(id, thePoint)
                constraintSet.applyTo(contentLayout)
                constraintSet.connect(relatedViewId, startSide, id, endSide, 0)
            } else {
                constraintSet.connect(
                    relatedViewId,
                    startSide,
                    targetId,
                    endSide,
                    connection.margin
                )
            }
        }

        constraintSet.applyTo(contentLayout)
        contentLayout.post { item.relatedSpotView?.visibility = View.VISIBLE }
    }

    class Builder(private val context: Context) {

        internal var steps = mutableListOf<Coachmark<View>>()
        internal var initialDelay = 0L
        internal var animate = true
        internal var animationVelocity: AnimationVelocity = AnimationVelocity.NORMAL
        internal var allowOverlaidInteractions = false

        fun <TYPE : View> steps(listSteps: List<Coachmark<TYPE>>) =
            apply { steps.addAll(listSteps as List<Coachmark<View>>) }

        fun <TYPE : View> nextStep(step: Coachmark<TYPE>) =
            apply { steps.add(step as Coachmark<View>) }

        fun initialDelay(delay: Long) = apply { this.initialDelay = delay }
        fun withAnimation(animate: Boolean) = apply { this.animate = animate }
        fun animationVelocity(animationVelocity: AnimationVelocity) =
            apply { this.animationVelocity = animationVelocity }

        fun allowOverlaidViewsInteractions(allow: Boolean) =
            apply { this.allowOverlaidInteractions = allow }

        fun build() = CoachMarksFlow(context).initView(this)
    }

    enum class AnimationVelocity(val milliseconds: Long) {
        TURTLE(2000),
        SLOWEST(1500),
        SLOW(1000),
        NORMAL(500),
        FAST(300),
        FASTEST(150),
        LIGHT_SPEED(50)
    }

    enum class CoachmarkCloseAction {
        CLOSE_BUTTON,
        OVERLAY,
        FLOW_ENDED,
        DISMISSED
    }

    companion object {

        fun with(context: Context): Builder = Builder(context)
        private const val MARGIN_CLOSE_BUTTON = 16
    }
}
