package com.aoliva.coachmarks

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import com.aoliva.coachmarks.extensions.*
import com.aoliva.coachmarks.spot.Spot
import com.aoliva.coachmarks.spot.SpotGenerator
import com.aoliva.coachmarks.spot.SpotView
import kotlin.properties.Delegates

class CoachMarksFlow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // VARIABLES -----------------------------------------------------------------------------------
    private var spotView: SpotView? = null

    private var steps: List<Coachmark>? = null
    private var currentStep = 0

    private var relatedViewId: Int = 0
    private var currentContentLayoutId: Int = 0

    private var initialDelay = 200L
    private var animate = true
    private var animationVelocity: AnimationVelocity = AnimationVelocity.NORMAL
    private var allowOverlaidViewsInteractions = false

    private var currentFocusView: View? = null
    private var currentLayoutChangeListener: OnLayoutChangeListener? = null

    private var isRtl by Delegates.notNull<Boolean>()

    private var closeView: View? = null
    private var closeViewPosition: CloseViewPosition = CloseViewPosition.TOP_END
    private var window: Window? = null

    var coachMarkListener: CoachMarkListener? = null

    private fun initView(builder: Builder): CoachMarksFlow {
        steps = builder.steps
        initialDelay = builder.initialDelay
        animate = builder.animate
        animationVelocity = builder.animationVelocity
        allowOverlaidViewsInteractions = builder.allowOverlaidInteractions
        window = builder.window ?: getActivity()?.window
        isRtl =
            builder.forceRtl ||
                    window?.decorView?.layoutDirection == View.LAYOUT_DIRECTION_RTL ||
                    context.localeDirection() == ViewCompat.LAYOUT_DIRECTION_RTL
        closeView = builder.closeView
        builder.closeViewPosition?.let { position -> closeViewPosition = position }

        return this
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        Log.v(TAG, "Attached to window")
        steps?.let {
            drawStep(it[currentStep])
        } ?: Log.v(TAG, "Please set desired steps before invoke show method")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.v(TAG, "Detached from window")

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
            removeCoachMarkFlow(CoachmarkCloseAction.FLOW_ENDED)
        } else {
            drawStep(steps?.get(++currentStep) ?: throw RuntimeException("No more steps"))
        }
    }

    /**
     * Close view
     */
    fun close() {
        currentStep++
        removeCoachMarkFlow(CoachmarkCloseAction.DISMISSED)
    }

    private fun removeCoachMarkFlow(closeAction: CoachmarkCloseAction) {
        if (parent == null) {
            return
        }

        currentFocusView?.takeIf { currentLayoutChangeListener != null }
            ?.removeOnLayoutChangeListener(
                currentLayoutChangeListener
            )
        fadeOut {
            (parent as? ViewGroup)?.removeView(this)
            coachMarkListener?.onCoachmarkFlowClosed(closeAction)
        }
    }

    /**
     * @return current coachmark or `null` if there is no more coachmarks to show
     */
    fun getCurrentStepView(): Coachmark? {
        return steps?.getOrNull(currentStep) ?: run {
            Log.v(TAG, "There is no steps defined")
            null
        }
    }

    /**
     * Shows the coachmark flow
     */
    fun show() {
        Handler(Looper.getMainLooper()).postDelayed({
            val vg = window?.decorView?.rootView as ViewGroup
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

    private fun drawStep(item: Coachmark) {
        if (parent == null) {
            return
        }

        currentFocusView?.takeIf { currentLayoutChangeListener != null }
            ?.removeOnLayoutChangeListener(
                currentLayoutChangeListener
            )
        removeView(findViewById(relatedViewId))

        initSpotViewIfNecessary()

        val focusView: View? =
            if (item.target != null) item.target
            else (parent as? ViewGroup)?.findViewById(item.targetId)

        if (focusView == null) {
            Log.w(TAG, "There is no view detected with given Id: " + item.targetId)
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
        item: Coachmark,
        spot: Spot,
        focusView: View,
        animate: Boolean = false
    ) {
        if (parent == null) {
            return
        }

        findViewById<ViewGroup>(currentContentLayoutId)?.let {
            (it.parent as? ViewGroup)?.removeView(it)
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

        setRelatedViewConstraints(item, contentLayout, spot, focusView)
    }

    private fun assignViewRelatedId(relatedView: View) {
        relatedView.id = View.generateViewId()
        relatedViewId = relatedView.id
    }

    private fun addCloseButton() {
        val closeViewToUse = closeView?.let { closeView ->
            closeView.id = View.generateViewId()
            closeView
        } ?: run {
            ImageView(context).apply {
                id = View.generateViewId()
                setImageResource(R.drawable.ic_close_white)
            }
        }
        closeViewToUse.setOnClickListener { removeCoachMarkFlow(CoachmarkCloseAction.CLOSE_BUTTON) }
        addView(closeViewToUse, getCloseViewLayoutParams())
    }

    private fun getCloseViewLayoutParams(): LayoutParams {
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        when (closeViewPosition) {
            CloseViewPosition.TOP_END -> {
                layoutParams.topMargin = MARGIN_CLOSE_BUTTON.toPx + resources.statusBarHeight
                layoutParams.marginEnd = MARGIN_CLOSE_BUTTON.toPx
                layoutParams.addRule(ALIGN_PARENT_END)
            }
            CloseViewPosition.BOTTOM_END -> {
                val navigationBarHeight = getNavigationBarHeight()
                layoutParams.bottomMargin = MARGIN_CLOSE_BUTTON.toPx + navigationBarHeight
                layoutParams.marginEnd = MARGIN_CLOSE_BUTTON.toPx
                layoutParams.addRule(ALIGN_PARENT_END)
                layoutParams.addRule(ALIGN_PARENT_BOTTOM)
            }
            CloseViewPosition.BOTTOM_START -> {
                val navigationBarHeight = getNavigationBarHeight()
                layoutParams.bottomMargin = MARGIN_CLOSE_BUTTON.toPx + navigationBarHeight
                layoutParams.marginStart = MARGIN_CLOSE_BUTTON.toPx
                layoutParams.addRule(ALIGN_PARENT_START)
                layoutParams.addRule(ALIGN_PARENT_BOTTOM)
            }
            CloseViewPosition.TOP_START -> {
                layoutParams.topMargin = MARGIN_CLOSE_BUTTON.toPx + resources.statusBarHeight
                layoutParams.marginStart = MARGIN_CLOSE_BUTTON.toPx
                layoutParams.addRule(ALIGN_PARENT_START)
            }
        }
        return layoutParams
    }

    private fun getNavigationBarHeight(): Int {
        val orientation = context.resources.configuration.orientation
        val id = resources.getIdentifier(
            if (orientation == Configuration.ORIENTATION_PORTRAIT) "navigation_bar_height" else "navigation_bar_height_landscape",
            "dimen", "android"
        )
        return if (id > 0) {
            resources.getDimensionPixelSize(id)
        } else 0
    }

    private fun setRelatedViewConstraints(
        item: Coachmark,
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

        contentLayout.addView(view)

        val constraintSet = ConstraintSet()
        constraintSet.clone(contentLayout)

        val screenWidthPixels = Resources.getSystem().displayMetrics.widthPixels
        val xMargin = if (isRtl) {
            (screenWidthPixels - coordinates[0] - spot.width * 2).toInt() + (wDifference / 2)
        } else {
            coordinates[0] - (wDifference / 2)
        }
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
            xMargin
        )

        for (connection in item.connections) {
            val startSide = when (connection.relatedViewConnection) {
                Coachmark.ConnectionSide.TOP -> ConstraintSet.TOP
                Coachmark.ConnectionSide.BOTTOM -> ConstraintSet.BOTTOM
                Coachmark.ConnectionSide.START -> ConstraintSet.START
                Coachmark.ConnectionSide.END -> ConstraintSet.END
            }
            val targetId = when (connection.anchorView) {
                Coachmark.AnchorView.PARENT -> ConstraintSet.PARENT_ID
                Coachmark.AnchorView.TARGET -> view.id
            }
            val endSide = when (connection.anchorViewConnection) {
                Coachmark.ConnectionSide.TOP -> ConstraintSet.TOP
                Coachmark.ConnectionSide.BOTTOM -> ConstraintSet.BOTTOM
                Coachmark.ConnectionSide.START -> ConstraintSet.START
                Coachmark.ConnectionSide.END -> ConstraintSet.END
            }

            if (connection.margin != 0 && targetId != ConstraintSet.PARENT_ID) {
                val id = View.generateViewId()
                val guideType = when (connection.anchorViewConnection) {
                    Coachmark.ConnectionSide.TOP, Coachmark.ConnectionSide.BOTTOM -> {
                        ConstraintSet.HORIZONTAL_GUIDELINE
                    }
                    else -> {
                        ConstraintSet.VERTICAL_GUIDELINE
                    }
                }
                constraintSet.create(id, guideType)
                val thePoint = when (connection.anchorViewConnection) {
                    Coachmark.ConnectionSide.TOP -> {
                        coordinates[1] - (hDifference / 2) + connection.margin.toPx
                    }
                    Coachmark.ConnectionSide.BOTTOM -> {
                        coordinates[1] - (hDifference / 2) + (spot.height.toInt() * 2) + connection.margin.toPx
                    }
                    Coachmark.ConnectionSide.START -> {
                        if (isRtl.not()) {
                            coordinates[0] - (wDifference / 2) + connection.margin.toPx
                        } else {
                            screenWidthPixels - (screenWidthPixels - coordinates[0]) - (wDifference / 2) - connection.margin.toPx + spot.width.toInt() * 2
                        }
                    }
                    Coachmark.ConnectionSide.END -> {
                        if (isRtl.not()) {
                            coordinates[0] - (wDifference / 2) + (spot.width.toInt() * 2) + connection.margin.toPx
                        } else {
                            screenWidthPixels - (screenWidthPixels - coordinates[0]) - (wDifference / 2) - connection.margin.toPx
                        }
                    }
                }

                val finalPoint =
                    if (isRtl.not() ||
                        connection.anchorViewConnection == Coachmark.ConnectionSide.TOP ||
                        connection.anchorViewConnection == Coachmark.ConnectionSide.BOTTOM
                    ) {
                        thePoint
                    } else {
                        screenWidthPixels - thePoint
                    }
                constraintSet.setGuidelineBegin(id, finalPoint)
                constraintSet.applyTo(contentLayout)
                constraintSet.connect(relatedViewId, startSide, id, endSide, 0)
            } else {
                constraintSet.connect(
                    relatedViewId,
                    startSide,
                    view.id,
                    endSide,
                    0
                )
            }
        }

        constraintSet.applyTo(contentLayout)
        contentLayout.post { item.relatedSpotView?.visibility = View.VISIBLE }
    }

    /**
     * Notifies coachmark flow events
     */
    interface CoachMarkListener {
        /**
         * Called when the flow is closed
         */
        fun onCoachmarkFlowClosed(closeAction: CoachmarkCloseAction)

        /**
         * Called when the flow starts
         */
        fun onCoachmarkFlowShowed()

        /**
         * Called when the state of the flow changes
         */
        fun onChangedCoachMarkState(state: Coachmark.CoachMarkState, coachMarkIndex: Int)

        /**
         * Called when the highlighted view is clicked by the user
         */
        fun onTargetViewClick(coachMarkIndex: Int)
    }

    enum class CloseViewPosition {
        TOP_START,
        TOP_END,
        BOTTOM_START,
        BOTTOM_END
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

    class Builder(private val context: Context) {

        internal var steps = mutableListOf<Coachmark>()
        internal var initialDelay = 0L
        internal var animate = true
        internal var animationVelocity: AnimationVelocity = AnimationVelocity.NORMAL
        internal var allowOverlaidInteractions = false
        internal var forceRtl = false
        internal var closeView: View? = null
        internal var closeViewPosition: CloseViewPosition? = null
        internal var window: Window? = null

        /**
         * List of coachmarks to show. They will be shown following the list order
         */
        fun steps(listSteps: List<Coachmark>) = apply { steps.addAll(listSteps) }

        /**
         * Delay to show start the flow in milliseconds
         */
        fun initialDelay(delay: Long) = apply { this.initialDelay = delay }

        /**
         * Whether the view highlight is animated or not. Set to `true` by default
         */
        fun withAnimation(animate: Boolean) = apply { this.animate = animate }

        /**
         * Velocity of the animation.
         */
        fun animationVelocity(animationVelocity: AnimationVelocity) =
            apply { this.animationVelocity = animationVelocity }

        /**
         * Set to `true` to allow the interaction with the overlaid views
         */
        fun allowOverlaidViewsInteractions(allow: Boolean) =
            apply { this.allowOverlaidInteractions = allow }

        /**
         * The library supports natively RTL. In case you're forcing your layouts to RTL
         * programatically, set this flag to `true`. The default value is `false`
         */
        fun forceRTL(force: Boolean) = apply { this.forceRtl = force }

        /**
         * Custom close view. If not passed, an X will be shown.
         */
        fun closeView(closeView: View) =
            apply {
                this.closeView = closeView
            }

        /**
         * Positioning of the close view in the screen
         */
        fun closeViewPosition(position: CloseViewPosition = CloseViewPosition.TOP_END) =
            apply { this.closeViewPosition = position }

        /**
         * Pass your target window in case you are targeting a view inside a modal dialog.
         * By default, the root view window is used.
         */
        fun window(window: Window) = apply { this.window = window }

        /**
         * Builds the flow
         */
        fun build() = CoachMarksFlow(context).initView(this)
    }

    companion object {

        fun with(context: Context): Builder = Builder(context)
        private const val MARGIN_CLOSE_BUTTON = 16
    }
}
