package com.aoliva.coachmarks

import android.view.View
import androidx.annotation.IdRes

class Coachmark<T : View> {

    // ---------------------------------------------------------------------------------------------
    // VARIABLES
    // ---------------------------------------------------------------------------------------------

    internal var sizePercentage: Double = -1.0

    @IdRes
    internal var targetId: Int = 0
    internal var target: View? = null
    internal var position: Position
    internal var alignment: Alignment
    internal var relatedSpotView: T? = null
    internal var maxWidth = -1
    internal var deviations = intArrayOf(0, 0)
    internal var shape: Shape = Shape.CIRCLE
    internal var cornerRadius: Int = 0

    internal var onRelatedSpotViewChanged: ((Boolean) -> Unit)? = null

    /**
     * @param targetId        Desired view id to be spotted
     * @param relatedSpotView View to be shown with the coachmark
     * @param position        Position of the view respect to the mark
     * @param alignment       Alignment of the view respect to the position
     */
    constructor(
        @IdRes targetId: Int,
        relatedSpotView: T,
        position: Position,
        alignment: Alignment,
        type: Shape = Shape.CIRCLE
    ) {
        this.targetId = targetId
        this.position = position
        this.alignment = alignment
        this.relatedSpotView = relatedSpotView
        this.shape = type
    }

    /**
     * @param target          Desired view to be spotted
     * @param relatedSpotView View to be shown with the coachmark
     * @param position        Position of the view respect to the mark
     * @param alignment       Alignment of the view respect to the position
     */
    constructor(
        target: View,
        relatedSpotView: T,
        position: Position,
        alignment: Alignment,
        type: Shape = Shape.CIRCLE
    ) {

        this.target = target
        this.position = position
        this.alignment = alignment
        this.relatedSpotView = relatedSpotView
        this.shape = type
    }

    /**
     * @param [newRelatedSpotView] View used to replace the current one
     * @param [relatedViewOptions] Options for the new added related view. If not defined, current related view options will be used
     * @param [animate] Default `true`
     */
    @JvmOverloads
    fun replaceRelatedView(
        newRelatedSpotView: T,
        relatedViewOptions: RelatedViewOptions = RelatedViewOptions(position, alignment, deviations),
        animate: Boolean = true
    ) {
        relatedSpotView = newRelatedSpotView
        relatedViewOptions?.let {
            alignment = it.alignment
            position = it.position
        }
        onRelatedSpotViewChanged?.invoke(animate)
    }

    class Builder<TYPE : View> @JvmOverloads constructor(
        private val relatedSpotView: TYPE,
        relatedViewOptions: RelatedViewOptions = RelatedViewOptions(Position.TOP, Alignment.TOP)
    ) {
        private var aligment = Alignment.TOP
        private var position = Position.TOP
        private var shape = Shape.CIRCLE
        private var sizePercentage: Double = 100.0
        private var deviations = intArrayOf(0, 0)
        private var cornerRadius = 0

        private var targetView: View? = null
        private var targetViewId: Int? = null

        init {
            aligment = relatedViewOptions.alignment
            position = relatedViewOptions.position
            deviations = relatedViewOptions.deviations
        }

        /**
         * @param [targetView] The view to be highlighted
         */
        fun withView(targetView: View) = apply {
            this.targetView = targetView
        }

        /**
         * @param [targetViewId] Id of the view to be highlighted
         */
        fun withViewId(@IdRes targetViewId: Int) = apply {
            this.targetViewId = targetViewId
        }

        /**
         * @param [sizePercentage] If the defined shape is a [Shape.CIRCLE], circle diameter will be
         *  the percentage of the target view width. If the defined shape is a [Shape.RECTANGLE], the spot
         *  will take the percentage of target view width and height.
         */
        fun sizePercentage(sizePercentage: Double) = apply { this.sizePercentage = sizePercentage }

        /**
         * @param [shape] Shape of the spot to be drawn
         */
        fun shape(shape: Shape) = apply { this.shape = shape }

        /**
         * @param [cornerRadius] just will be taken into account for [Shape.RECTANGLE] spots.
         */
        fun cornerRadius(cornerRadius: Int) = apply { this.cornerRadius = cornerRadius }

        fun build(): Coachmark<TYPE> = when {
            (targetView != null && targetViewId != null) -> {
                throw RuntimeException("Call only one of these methods \"withView\" or \"withViewId\"")
            }
            (targetView != null) -> {
                Coachmark(targetView!!, relatedSpotView, position, aligment, shape).apply {
                    sizePercentage = this@Builder.sizePercentage
                    deviations = this@Builder.deviations
                    cornerRadius = this@Builder.cornerRadius
                }
            }
            (targetViewId != null) -> {
                Coachmark(targetViewId!!, relatedSpotView, position, aligment, shape).apply {
                    sizePercentage = this@Builder.sizePercentage
                    deviations = this@Builder.deviations
                    cornerRadius = this@Builder.cornerRadius
                }
            }
            else -> {
                throw RuntimeException("Call one of these methods \"withView\" or \"withViewId\"")
            }
        }
    }

    /**
     * @param [position] Position about related view
     * @param [alignment] Alignment about position
     * @param [deviations] First position is the deviation about X axis. Second position is the deviation about Y axis.
     */
    class RelatedViewOptions @JvmOverloads constructor(
        val position: Position,
        val alignment: Alignment,
        val deviations: IntArray = intArrayOf(0, 0)
    )

    enum class CoachMarkState {
        OPENING,
        OPENED,
        CLOSING,
        CLOSED
    }

    enum class Shape {
        CIRCLE, RECTANGLE
    }

    enum class Position {
        /**
         * Indicates that related view will be aligned on the top of spot
         */
        TOP,

        /**
         * Indicates that related view will be aligned on the bottom of spot
         */
        BOTTOM,

        /**
         * Indicates that related view will be aligned on the left side of spot
         */
        LEFT,

        /**
         * Indicates that related view will be aligned on the right side of spot
         */
        RIGHT
    }

    enum class Alignment {
        /**
         * Indicates that related view will have its top aligned with the spot position chosen
         */
        TOP,

        /**
         * Indicates that related view will have its bottom aligned with the spot position chosen
         */
        BOTTOM,

        /**
         * Indicates that related view will have its left side aligned with the spot position chosen
         */
        LEFT,

        /**
         * Indicates that related view will have its right side aligned with the spot position chosen
         */
        RIGHT,

        /**
         * Indicates that related view will have its center aligned with the spot position chosen
         */
        CENTER,

    }
}
