package com.aoliva.coachmarks

import android.view.View
import androidx.annotation.IdRes

class Coachmark {

    // ---------------------------------------------------------------------------------------------
    // VARIABLES
    // ---------------------------------------------------------------------------------------------

    internal var sizePercentage: Double = -1.0

    @IdRes
    internal var targetId: Int = 0
    internal var target: View? = null
    internal var relatedSpotView: View? = null
    internal var connections: List<Connection>
    internal var shape: Shape = Shape.CIRCLE
    internal var cornerRadius: Int = 0

    internal var onRelatedSpotViewChanged: ((Boolean) -> Unit)? = null

    private constructor(
        @IdRes targetId: Int,
        relatedSpotView: View,
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
        relatedSpotView: View,
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
        newRelatedSpotView: View,
        relatedViewOptions: RelatedViewOptions = RelatedViewOptions(),
        animate: Boolean = true
    ) {
        relatedSpotView = newRelatedSpotView
        relatedViewOptions.let { relatedOptions ->
            this.connections = relatedOptions.connections.toList()
        }
        onRelatedSpotViewChanged?.invoke(animate)
    }

    class Builder<TYPE : View> @JvmOverloads constructor(
        private val relatedSpotView: TYPE,
        private val relatedViewOptions: RelatedViewOptions = RelatedViewOptions()
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

        fun build(): Coachmark = when {
            (targetView != null && targetViewId != null) -> {
                throw RuntimeException("Call only one of these methods \"withView\" or \"withViewId\"")
            }
            (targetView != null) -> {
                Coachmark(
                    targetView!!,
                    relatedSpotView,
                    shape,
                    relatedViewOptions.connections.toList()
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
                    relatedViewOptions.connections.toList()
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

    class RelatedViewOptions(vararg val connections: Connection)

    /**
     * @param relatedViewConnection Side of the related view to connect with given anchor view
     * @param anchorView Make reference to the view that is acts as a reference for the related view connection
     * @param anchorViewConnection Side where the anchor view is going to be connected with the related view
     * @param margin Margin in DP
     */
    data class Connection(
        val relatedViewConnection: ConnectionSide,
        val anchorView: AnchorView,
        val anchorViewConnection: ConnectionSide,
        val margin: Int
    )

    enum class ConnectionSide {
        TOP, BOTTOM, START, END
    }

    enum class AnchorView {
        /**
         * Parent of the view related view, generally the user screen
         */
        PARENT,

        /**
         * Spot target view
         */
        TARGET
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
}
