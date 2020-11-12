package com.aoliva.flexiblecoachmarks

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.aoliva.coachmarks.CoachMarksFlow
import com.aoliva.coachmarks.Coachmark


class MainActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        setContentView(R.layout.activity_main)
        createFirstFlow()
    }

    private fun createFirstFlow() {
        findViewById<View>(R.id.button).setOnClickListener {
            val relatedButton = layoutInflater.inflate(R.layout.view_simple_layout, null)
            val relatedButtonReplace: View = Button(this).apply { text = "Replace me!" }
            val options1 = Coachmark.RelatedViewOptions(
                Coachmark.Connection(
                    Coachmark.ConnectionSide.TOP,
                    Coachmark.AnchorView.TARGET,
                    Coachmark.ConnectionSide.BOTTOM,
                    2
                ),
                Coachmark.Connection(
                    Coachmark.ConnectionSide.END,
                    Coachmark.AnchorView.TARGET,
                    Coachmark.ConnectionSide.END,
                    1
                )

            )
            val coachmark1: Coachmark = Coachmark.Builder(relatedButton, options1)
                .withViewId(R.id.button8)
                .sizePercentage(120.0)
                .shape(Coachmark.Shape.RECTANGLE)
                .build()

            val options2 =
                Coachmark.RelatedViewOptions(
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.BOTTOM,
                        Coachmark.AnchorView.PARENT,
                        Coachmark.ConnectionSide.BOTTOM,
                        0
                    ),
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.END,
                        Coachmark.AnchorView.PARENT,
                        Coachmark.ConnectionSide.END,
                        0
                    )
                )
            val coachmark2: Coachmark = Coachmark.Builder(relatedButton, options2)
                .withViewId(R.id.button2)
                .sizePercentage(100.0)
                .shape(Coachmark.Shape.RECTANGLE)
                .cornerRadius(10)
                .build()

            val options3 =
                Coachmark.RelatedViewOptions(
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.BOTTOM,
                        Coachmark.AnchorView.PARENT,
                        Coachmark.ConnectionSide.BOTTOM,
                        0
                    ),
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.END,
                        Coachmark.AnchorView.PARENT,
                        Coachmark.ConnectionSide.END,
                        0
                    )
                )
            val coachmark3: Coachmark = Coachmark.Builder(relatedButtonReplace, options3)
                .withViewId(R.id.button7)
                .sizePercentage(130.0)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val options4 =
                Coachmark.RelatedViewOptions(
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.BOTTOM,
                        Coachmark.AnchorView.PARENT,
                        Coachmark.ConnectionSide.BOTTOM,
                        0
                    ),
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.END,
                        Coachmark.AnchorView.PARENT,
                        Coachmark.ConnectionSide.END,
                        0
                    )
                )
            val coachmark4: Coachmark = Coachmark.Builder(relatedButton, options4)
                .withViewId(R.id.button6)
                .sizePercentage(50.0)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val coachmarksFlow = CoachMarksFlow.with(this)
                .steps(listOf(coachmark1, coachmark4))
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
                val options = Coachmark.RelatedViewOptions(
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.BOTTOM,
                        Coachmark.AnchorView.TARGET,
                        Coachmark.ConnectionSide.BOTTOM,
                        0
                    ),
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.TOP,
                        Coachmark.AnchorView.TARGET,
                        Coachmark.ConnectionSide.TOP,
                        0
                    ),
                    Coachmark.Connection(
                        Coachmark.ConnectionSide.START,
                        Coachmark.AnchorView.TARGET,
                        Coachmark.ConnectionSide.END,
                        0
                    )
                )
                coachmarksFlow.getCurrentStepView()
                    ?.replaceRelatedView(relatedButton, options, true)
            }
            relatedButton.findViewById<TextView>(R.id.textView).text = "Next coachmark"
        }
    }
}
