package com.aoliva.flexiblecoachmarks

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.aoliva.coachmarks.Coachmark
import com.aoliva.coachmarks.CoachmarksFlow


class MainActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createFirstFlow()
    }

    private fun createFirstFlow() {
        findViewById<View>(R.id.button).setOnClickListener {
            val relatedButton = Button(this)
            val coachmark1: Coachmark<Button> = Coachmark.Builder(relatedButton)
                .withViewId(R.id.button)
                .sizePercentage(100.0)
                .alignment(Coachmark.Alignment.RIGHT)
                .position(Coachmark.Position.BOTTOM)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val coachmark2: Coachmark<Button> = Coachmark.Builder(relatedButton)
                .withViewId(R.id.button2)
                .sizePercentage(100.0)
                .alignment(Coachmark.Alignment.LEFT)
                .position(Coachmark.Position.BOTTOM)
                .shape(Coachmark.Shape.RECTANGLE)
                .cornerRadius(10)
                .build()

            val coachmark3: Coachmark<Button> = Coachmark.Builder(relatedButton)
                .withViewId(R.id.button5)
                .sizePercentage(100.0)
                .alignment(Coachmark.Alignment.TOP)
                .position(Coachmark.Position.RIGHT)
                .padding(50, 0, 100, 0)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val coachmark4: Coachmark<Button> = Coachmark.Builder(relatedButton)
                .withViewId(R.id.button6)
                .sizePercentage(100.0)
                .alignment(Coachmark.Alignment.TOP)
                .position(Coachmark.Position.LEFT)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val coachmarksFlow = CoachmarksFlow.with(this)
                .steps(listOf(coachmark1, coachmark2, coachmark3, coachmark4))
//                .animationVelocity(10)
                .initialDelay(1000)
//                .withAnimation(false)
                .build().let { flow ->
                    flow.coachMarkListener = object : CoachmarksFlow.CoachMarkListener {
                        override fun onCoachmarkFlowDismissed() {
                            Log.d("FlexibleCoachmarkDemo", "Coachmark dismissed")
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
                    }
                    flow.show()
                    flow
                }

            relatedButton.setOnClickListener {
                coachmarksFlow.goNextStep()
            }
            relatedButton.text = "Next coachmark"
        }
    }
}
