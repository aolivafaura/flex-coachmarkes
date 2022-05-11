package com.aoliva.flexiblecoachmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.aoliva.coachmarks.CoachMarksFlow
import com.aoliva.coachmarks.Coachmark
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PassingWindowActivity : AppCompatActivity() {

    private var coachMarksFlow: CoachMarksFlow? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_window)

        configureListeners()
    }

    private fun configureListeners() {
        findViewById<AppCompatButton>(R.id.button).setOnClickListener {
            showBottomSheetDialog()
        }
    }

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = InnerBottomSheetFragment()
        bottomSheetDialog.show(supportFragmentManager, "inner_dialog")
        bottomSheetDialog.clickListener = { row ->
            when (row) {
                1 -> {
                    if (coachMarksFlow?.getCurrentStepView() == null) {
                        initFlow()
                    }
                }
                2 -> {
                    if (coachMarksFlow?.getCurrentStepView() == null) {
                        initFlow((bottomSheetDialog as DialogFragment).dialog?.window!!)
                    }
                }
            }
        }
    }

    private fun initFlow(window: Window? = null) {
        val firstCoachmark = getCoachmark()

        coachMarksFlow = CoachMarksFlow.with(this)
            .apply {
                window?.let { this.window(window) }
            }
            .steps(listOf(firstCoachmark))
            .animationVelocity(CoachMarksFlow.AnimationVelocity.NORMAL)
            .build()

        coachMarksFlow?.show()

        coachMarksFlow?.coachMarkListener = object : CoachMarksFlow.CoachMarkListener {
            override fun onCoachmarkFlowClosed(closeAction: CoachMarksFlow.CoachmarkCloseAction) {
                coachMarksFlow = null
            }

            override fun onCoachmarkFlowShowed() {
                // Do nothing
            }

            override fun onChangedCoachMarkState(
                state: Coachmark.CoachMarkState,
                coachMarkIndex: Int
            ) {
                // Do nothing
            }

            override fun onTargetViewClick(coachMarkIndex: Int) {
                // Do nothing
            }

        }
    }

    private fun getCoachmark(): Coachmark {
        val relatedView = layoutInflater.inflate(R.layout.view_related_description3, null)

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
            ),
            Coachmark.Connection(
                relatedViewConnection = Coachmark.ConnectionSide.END,
                anchorView = Coachmark.AnchorView.TARGET,
                anchorViewConnection = Coachmark.ConnectionSide.END,
                margin = 0
            )
        )

        return Coachmark.Builder(relatedView, options)
            .withViewId(R.id.first_row)
            .shape(Coachmark.Shape.RECTANGLE)
            .cornerRadius(8)
            .sizePercentage(105.0)
            .build()
    }

    class InnerBottomSheetFragment : BottomSheetDialogFragment() {

        var clickListener: ((Int) -> Unit)? = null

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(
                R.layout.bottom_sheet_dialog_content, container,
                false
            ).apply {
                findViewById<TextView>(R.id.first_row).setOnClickListener {
                    clickListener?.invoke(1)
                }
                findViewById<TextView>(R.id.second_row).setOnClickListener {
                    clickListener?.invoke(2)
                }
            }
        }
    }
}