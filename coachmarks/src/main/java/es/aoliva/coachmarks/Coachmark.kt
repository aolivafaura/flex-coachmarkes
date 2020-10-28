package es.aoliva.coachmarks

import android.view.View
import androidx.annotation.IdRes

class Coachmark<T : View> {

    // ---------------------------------------------------------------------------------------------
    // VARIABLES
    // ---------------------------------------------------------------------------------------------

    var sizePercentage: Double = -1.0

    @IdRes
    var targetId: Int = 0
    var target: View? = null
    lateinit var position: Position
    lateinit var alignment: Alignment
    var relatedSpotView: T? = null
    internal var maxWidth = -1
    val paddings = intArrayOf(0, 0, 0, 0)
    var shape: Shape = Shape.CIRCLE

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
     * Defined paddings will be applied on related spot view
     *
     * @param top    top padding
     * @param left   left padding
     * @param right  right padding
     * @param bottom bottom padding
     */
    fun setPaddings(top: Int, left: Int, right: Int, bottom: Int) {

        this.paddings[0] = top
        this.paddings[1] = left
        this.paddings[2] = right
        this.paddings[3] = bottom
    }

    class Builder<TYPE : View>(private val relatedSpotView: TYPE) {
        private var aligment = Alignment.TOP
        private var position = Position.TOP
        private var shape = Shape.CIRCLE
        private var sizePercentage: Double = 100.0

        private var targetView: View? = null
        private var targetViewId: Int? = null

        fun withView(targetView: View) = apply {
            this.targetView = targetView
        }

        fun withViewId(@IdRes targetViewId: Int) = apply {
            this.targetViewId = targetViewId
        }

        fun sizePercentage(sizePercentage: Double) = apply { this.sizePercentage = sizePercentage }
        fun alignment(alignment: Alignment) = apply { this.aligment = alignment }
        fun position(position: Position) = apply { this.position = position }
        fun shape(shape: Shape) = apply { this.shape = shape }
        fun build(): Coachmark<TYPE> = when {
            (targetView != null && targetViewId != null) -> {
                throw RuntimeException("Enter only ")
            }
            (targetView != null) -> {
                Coachmark(targetView!!, relatedSpotView, position, aligment, shape).apply {
                    sizePercentage = this@Builder.sizePercentage
                }
            }
            (targetViewId != null) -> {
                Coachmark(targetViewId!!, relatedSpotView, position, aligment, shape).apply {
                    sizePercentage = this@Builder.sizePercentage
                }
            }
            else -> {
                throw RuntimeException("")
            }
        }
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
