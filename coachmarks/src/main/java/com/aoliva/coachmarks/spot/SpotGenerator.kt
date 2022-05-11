package com.aoliva.coachmarks.spot

import android.graphics.RectF
import android.view.View
import com.aoliva.coachmarks.CoachMarksFlow
import com.aoliva.coachmarks.Coachmark
import com.aoliva.coachmarks.extensions.percentage
import com.aoliva.coachmarks.extensions.toPx

internal object SpotGenerator {

    internal fun getSpot(
        coachMark: Coachmark,
        focusView: View,
        animate: Boolean,
        animationVelocity: CoachMarksFlow.AnimationVelocity
    ): Spot {
        val centerCoordinates = calculateCenter(focusView)

        val spotWidth = when {
            coachMark.sizePercentage > 0 -> focusView.width.percentage(coachMark.sizePercentage)
            else -> focusView.width
        }
        return when (coachMark.shape) {
            Coachmark.Shape.CIRCLE -> {
                val radius = (spotWidth / 2)
                generateSpot(centerCoordinates, radius, animate, animationVelocity)
            }
            Coachmark.Shape.RECTANGLE -> {
                val spotHeight = when {
                    coachMark.sizePercentage == 100.0 -> focusView.height
                    coachMark.sizePercentage > 0 -> focusView.height.percentage(coachMark.sizePercentage)
                    else -> focusView.height
                }
                generateSpot(
                    centerCoordinates,
                    spotHeight / 2,
                    spotWidth / 2,
                    coachMark.cornerRadius,
                    animate,
                    animationVelocity
                )
            }
        }.apply {
            assignOriginalTargetRect(this, focusView)
        }
    }

    private fun calculateCenter(focusView: View): IntArray {
        val coordinates = IntArray(2)
        focusView.getLocationInWindow(coordinates)

        coordinates[0] = coordinates[0] + focusView.width / 2
        coordinates[1] = coordinates[1] + focusView.height / 2

        return coordinates
    }

    private fun generateSpot(
        centerCoordinates: IntArray,
        height: Int,
        width: Int,
        cornerRadius: Int,
        animate: Boolean,
        animationVelocity: CoachMarksFlow.AnimationVelocity
    ): Spot {
        val rect = getRect(centerCoordinates, width, height)
        val velocity = calculateVelocity(animationVelocity, width)

        return RectangleSpot(
            rect,
            height.toFloat(),
            width.toFloat(),
            cornerRadius.toFloat(),
            animate,
            velocity,
            centerCoordinates
        ).apply {
            direction = Spot.EXPAND
        }
    }

    private fun generateSpot(
        centerCoordinates: IntArray,
        radius: Int,
        animate: Boolean,
        animationVelocity: CoachMarksFlow.AnimationVelocity
    ): Spot {
        val rect = getRect(centerCoordinates, radius, radius)
        val velocity = calculateVelocity(animationVelocity, radius * 2)

        return CircleSpot(rect, radius.toFloat(), animate, velocity, centerCoordinates)
            .apply { direction = Spot.EXPAND }
    }

    private fun getRect(coordinates: IntArray, width: Int, height: Int): RectF {
        val topCoordinate = coordinates[1] - height
        val bottomCoordinate = coordinates[1] + height
        val leftCoordinate = coordinates[0] - width
        val rightCoordinate = coordinates[0] + width

        return RectF(
            leftCoordinate.toFloat(),
            topCoordinate.toFloat(),
            rightCoordinate.toFloat(),
            bottomCoordinate.toFloat()
        )
    }

    private fun calculateVelocity(
        animationVelocity: CoachMarksFlow.AnimationVelocity,
        width: Int
    ): Int {
        val velocity = width.toPx * 8 / animationVelocity.milliseconds
        return if (velocity > 0) velocity.toInt() else 1
    }

    private fun assignOriginalTargetRect(item: Spot, focusView: View) {
        val coordinates = IntArray(2)
        focusView.getLocationInWindow(coordinates)

        item.targetViewRect = RectF(
            coordinates[0].toFloat(),
            coordinates[1].toFloat(),
            (coordinates[0] + focusView.width).toFloat(),
            (coordinates[1] + focusView.height).toFloat()
        )
    }
}