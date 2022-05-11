package com.aoliva.flexiblecoachmarks

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.aoliva.coachmarks.CoachMarksFlow
import com.aoliva.coachmarks.Coachmark

class RegularUsageActivity : AppCompatActivity() {

    private lateinit var coachMarksFlow: CoachMarksFlow

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_regular_usage)

        configureListeners()
    }

    private fun configureListeners() {
        findViewById<AppCompatButton>(R.id.start).setOnClickListener {
            initFlow()
        }
    }

    private fun initFlow() {
        val firstCoachmark = getFirstCoachmark()
        val secondCoachmark = getSecondCoachmark()

        coachMarksFlow = CoachMarksFlow.with(this)
            .steps(listOf(firstCoachmark, secondCoachmark))
            .animationVelocity(CoachMarksFlow.AnimationVelocity.NORMAL)
            .build()

        setUpListener(coachMarksFlow)

        coachMarksFlow.show()
    }

    private fun getFirstCoachmark(): Coachmark {
        val relatedView = layoutInflater.inflate(R.layout.view_related_description1, null)
        val relatedViewReplacement =
            layoutInflater.inflate(R.layout.view_related_description2, null)

        val options = Coachmark.RelatedViewOptions(
            Coachmark.Connection(
                relatedViewConnection = Coachmark.ConnectionSide.TOP,
                anchorView = Coachmark.AnchorView.TARGET,
                anchorViewConnection = Coachmark.ConnectionSide.BOTTOM,
                margin = 12
            ),
            Coachmark.Connection(
                relatedViewConnection = Coachmark.ConnectionSide.START,
                anchorView = Coachmark.AnchorView.TARGET,
                anchorViewConnection = Coachmark.ConnectionSide.START,
                margin = 0
            ),
            Coachmark.Connection(
                relatedViewConnection = Coachmark.ConnectionSide.END,
                anchorView = Coachmark.AnchorView.TARGET,
                anchorViewConnection = Coachmark.ConnectionSide.END,
                margin = 0
            )
        )

        relatedView.findViewById<Button>(R.id.button).setOnClickListener {
            relatedViewReplacement.findViewById<Button>(R.id.button).setOnClickListener {
                coachMarksFlow.goNextStep()
            }
            val replacementViewOptions = Coachmark.RelatedViewOptions(
                Coachmark.Connection(
                    relatedViewConnection = Coachmark.ConnectionSide.START,
                    anchorView = Coachmark.AnchorView.TARGET,
                    anchorViewConnection = Coachmark.ConnectionSide.END,
                    margin = 10
                )
            )
            coachMarksFlow.getCurrentStepView()
                ?.replaceRelatedView(relatedViewReplacement, replacementViewOptions)
        }

        return Coachmark.Builder(relatedView, options)
            .withViewId(R.id.first)
            .shape(Coachmark.Shape.CIRCLE)
            .build()
    }

    private fun getSecondCoachmark(): Coachmark {
        val relatedView = layoutInflater.inflate(R.layout.view_related_description1, null)
        val options = Coachmark.RelatedViewOptions(
            Coachmark.Connection(
                relatedViewConnection = Coachmark.ConnectionSide.BOTTOM,
                anchorView = Coachmark.AnchorView.TARGET,
                anchorViewConnection = Coachmark.ConnectionSide.TOP,
                margin = -12
            ),
            Coachmark.Connection(
                relatedViewConnection = Coachmark.ConnectionSide.START,
                anchorView = Coachmark.AnchorView.TARGET,
                anchorViewConnection = Coachmark.ConnectionSide.START,
                margin = 0
            )
        )

        relatedView.findViewById<Button>(R.id.button).setOnClickListener {
            coachMarksFlow.close()
        }

        return Coachmark.Builder(relatedView, options)
            .withViewId(R.id.second)
            .shape(Coachmark.Shape.RECTANGLE)
            .build()
    }

    private fun setUpListener(coachMarksFlow: CoachMarksFlow) {
        coachMarksFlow.coachMarkListener = object : CoachMarksFlow.CoachMarkListener {
            override fun onCoachmarkFlowClosed(closeAction: CoachMarksFlow.CoachmarkCloseAction) {
                Log.d("CM", "Flow closed")
            }

            override fun onCoachmarkFlowShowed() {
                Log.d("CM", "Flow showed")
            }

            override fun onChangedCoachMarkState(
                state: Coachmark.CoachMarkState,
                coachMarkIndex: Int
            ) {
                Log.d("CM", "Step $coachMarkIndex state changed: $state")
            }

            override fun onTargetViewClick(coachMarkIndex: Int) {
                Log.d("CM", "Target view click")
            }
        }
    }
}