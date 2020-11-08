package com.aoliva.flexiblecoachmarks

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.aoliva.coachmarks.Coachmark
import com.aoliva.coachmarks.CoachMarksFlow


class MainActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createFirstFlow()
    }

    private fun createFirstFlow() {
        findViewById<View>(R.id.button).setOnClickListener {
            val relatedButton = Button(this)
            val relatedButtonReplace = Button(this).apply { text = "Replace me!" }
            val options1 = Coachmark.RelatedViewOptions(
                Coachmark.Position.BOTTOM,
                Coachmark.Alignment.RIGHT,
                intArrayOf(10, -20)
            )
            val coachmark1: Coachmark<Button> = Coachmark.Builder(relatedButton, options1)
                .withViewId(R.id.button)
                .sizePercentage(100.0)
                .shape(Coachmark.Shape.RECTANGLE)
                .build()

            val options2 =
                Coachmark.RelatedViewOptions(Coachmark.Position.BOTTOM, Coachmark.Alignment.LEFT)
            val coachmark2: Coachmark<Button> = Coachmark.Builder(relatedButton, options2)
                .withViewId(R.id.button2)
                .sizePercentage(100.0)
                .shape(Coachmark.Shape.RECTANGLE)
                .cornerRadius(10)
                .build()

            val options3 =
                Coachmark.RelatedViewOptions(Coachmark.Position.TOP, Coachmark.Alignment.CENTER)
            val coachmark3: Coachmark<Button> = Coachmark.Builder(relatedButtonReplace, options3)
                .withViewId(R.id.button7)
                .sizePercentage(130.0)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val options4 =
                Coachmark.RelatedViewOptions(Coachmark.Position.LEFT, Coachmark.Alignment.TOP)
            val coachmark4: Coachmark<Button> = Coachmark.Builder(relatedButton, options4)
                .withViewId(R.id.button6)
                .sizePercentage(50.0)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val coachmarksFlow = CoachMarksFlow.with(this)
                .steps(listOf(coachmark1, coachmark2, coachmark3, coachmark4))
                .animationVelocity(CoachMarksFlow.AnimationVelocity.NORMAL)
//                .initialDelay(1000)
                .allowOverlaidViewsInteractions(false)
//                .withAnimation(false)
                .build().let { flow ->
                    flow.coachMarkListener = object : CoachMarksFlow.CoachMarkListener {
                        override fun onCoachmarkFlowClosed(closeAction: CoachMarksFlow.CoachmarkCloseAction) {
                            Log.d("FlexibleCoachmarkDemo", "Coachmark dismissed: $closeAction")
                        }

                        override fun onCoachmarkFlowShowed() {
                            Log.d(
                                "FlexibleCoachmarkDemo",
                                "Coachmark showed"
                            )
                        }

                        override fun onChangedCoachMarkState(
                            state: Coachmark.CoachMarkState,
                            coachMarkIndex: Int
                        ) {
                            Log.d(
                                "FlexibleCoachmarkDemo",
                                "Coachmark $coachMarkIndex is $state"
                            )
                        }

                        override fun onTargetViewClick(coachMarkIndex: Int) {
                            Log.d("FlexibleCoachmarkDemo", "Target view click: $coachMarkIndex")
                        }
                    }
                    flow.show()
                    flow
                }

            relatedButton.setOnClickListener {
                coachmarksFlow.goNextStep()
            }

            relatedButtonReplace.setOnClickListener {
                val options = Coachmark.RelatedViewOptions(Coachmark.Position.BOTTOM, Coachmark.Alignment.LEFT)
                coachmarksFlow.getCurrentStepView()?.replaceRelatedView(relatedButton, options, true)
            }
            relatedButton.text = "Next coachmark"
        }
    }
}
