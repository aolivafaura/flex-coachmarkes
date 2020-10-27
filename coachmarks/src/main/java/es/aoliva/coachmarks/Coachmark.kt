package es.aoliva.coachmarks

import android.view.View
import androidx.annotation.IdRes

class Coachmark<T : View> {

    // ---------------------------------------------------------------------------------------------
    // CONSTANTS
    // ---------------------------------------------------------------------------------------------

    companion object {

        /**
         * Indicates that related view will have its top aligned with the spot position chosen
         */
        const val ALIGNMENT_TOP = 1
        /**
         * Indicates that related view will have its bottom aligned with the spot position chosen
         */
        const val ALIGNMENT_BOTTOM = 2
        /**
         * Indicates that related view will have its left side aligned with the spot position chosen
         */
        const val ALIGNMENT_LEFT = 3
        /**
         * Indicates that related view will have its right side aligned with the spot position chosen
         */
        const val ALIGNMENT_RIGHT = 4
        /**
         * Indicates that related view will have its center aligned with the spot position chosen
         */
        const val ALIGNMENT_CENTER = 5
        /**
         * Indicates that related view will be aligned on the top of spot
         */
        const val POSITION_TOP = 6
        /**
         * Indicates that related view will be aligned on the bottom of spot
         */
        const val POSITION_BOTTOM = 7
        /**
         * Indicates that related view will be aligned on the left side of spot
         */
        const val POSITION_LEFT = 8
        /**
         * Indicates that related view will be aligned on the right side of spot
         */
        const val POSITION_RIGHT = 9
    }

    // ---------------------------------------------------------------------------------------------
    // VARIABLES
    // ---------------------------------------------------------------------------------------------

    var spotDiameterDp = -1
    var spotDiameterPercentage: Double = -1.0
    @IdRes
    var targetId: Int = 0
    var target: View? = null
    var position: Int = 0
    var alignment: Int = 0
    var relatedSpotView: T? = null
    internal var maxWidth = -1
    val paddings = intArrayOf(0, 0, 0, 0)

    /**
     * @param targetId        Desired view id to be spotted
     * @param relatedSpotView View to be shown with the coachmark
     * @param position        Position of the view respect to the mark
     * @param alignment       Alignment of the view respect to the position
     */
    constructor(@IdRes targetId: Int, relatedSpotView: T, position: Int,
                alignment: Int) {

        this.targetId = targetId
        this.position = position
        this.alignment = alignment
        this.relatedSpotView = relatedSpotView
    }

    /**
     * @param target          Desired view to be spotted
     * @param relatedSpotView View to be shown with the coachmark
     * @param position        Position of the view respect to the mark
     * @param alignment       Alignment of the view respect to the position
     */
    constructor(target: View, relatedSpotView: T, position: Int,
                alignment: Int) {

        this.target = target
        this.position = position
        this.alignment = alignment
        this.relatedSpotView = relatedSpotView
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
}
