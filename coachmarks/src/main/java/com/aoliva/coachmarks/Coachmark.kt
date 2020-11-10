package com.aoliva.coachmarks

import android.view.View
import androidx.annotation.IdRes
import com.aoliva.coachmarks.extensions.toPx

class Coachmark<T : View> {

    // ---------------------------------------------------------------------------------------------
    // VARIABLES
    // ---------------------------------------------------------------------------------------------

    internal var sizePercentage: Double = -1.0

    @IdRes
    internal var targetId: Int = 0
    internal var target: View? = null
    internal var relatedSpotView: T? = null
    internal var maxWidth = -1
    internal var connections: List<Connection>
    internal var shape: Shape = Shape.CIRCLE
    internal var cornerRadius: Int = 0

    internal var onRelatedSpotViewChanged: ((Boolean) -> Unit)? = null

    private constructor(
        @IdRes targetId: Int,
        relatedSpotView: T,
        type: Shape = Shape.CIRCLE,
        connections: List<Connection>
    ) {
        this.targetId = targetId
        this.relatedSpotView = relatedSpotView
        this.shape = type
        this.connections = connections
    }

    private constructor(
        target: View,
        relatedSpotView: T,
        type: Shape = Shape.CIRCLE,
        connections: List<Connection>
    ) {

        this.target = target
        this.relatedSpotView = relatedSpotView
        this.shape = type
        this.connections = connections
    }

    /**
     * @param [newRelatedSpotView] View used to replace the current one
     * @param [relatedViewOptions] Options for the new added related view. If not defined, current related view options will be used
     * @param [animate] Default `true`
     */
    @JvmOverloads
    fun replaceRelatedView(
        newRelatedSpotView: T,
        relatedViewOptions: RelatedViewOptions = RelatedViewOptions(listOf<Connection>()),
        animate: Boolean = true
    ) {
        relatedSpotView = newRelatedSpotView
        relatedViewOptions.let { relatedOptions ->
            this.connections = relatedOptions.connections
        }
        onRelatedSpotViewChanged?.invoke(animate)
    }

    class Builder<TYPE : View> @JvmOverloads constructor(
        private val relatedSpotView: TYPE,
        private val relatedViewOptions: RelatedViewOptions = RelatedViewOptions(emptyList())
    ) {

        private var shape = Shape.CIRCLE
        private var sizePercentage: Double = 100.0
        private var cornerRadius = 0

        private var targetView: View? = null
        private var targetViewId: Int? = null

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
                Coachmark(
                    targetView!!,
                    relatedSpotView,
                    shape,
                    relatedViewOptions.connections
                ).apply {
                    sizePercentage = this@Builder.sizePercentage
                    cornerRadius = this@Builder.cornerRadius
                }
            }
            (targetViewId != null) -> {
                Coachmark(
                    targetViewId!!,
                    relatedSpotView,
                    shape,
                    relatedViewOptions.connections
                ).apply {
                    sizePercentage = this@Builder.sizePercentage
                    cornerRadius = this@Builder.cornerRadius
                }
            }
            else -> {
                throw RuntimeException("Call one of these methods \"withView\" or \"withViewId\"")
            }
        }
    }

    data class RelatedViewOptions(val connections: List<Connection>)

    data class Connection(
        val relatedViewConnection: ConnectionEdge,
        val anchorView: AnchorView,
        val anchorViewConnection: ConnectionEdge,
        val margin: Int
    )

    enum class ConnectionEdge {
        TOP, BOTTOM, START, END
    }

    enum class AnchorView {
        PARENT, TARGET
    }

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
