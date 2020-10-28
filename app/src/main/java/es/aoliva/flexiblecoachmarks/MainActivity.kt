package es.aoliva.flexiblecoachmarks

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import es.aoliva.coachmarks.Coachmark
import es.aoliva.coachmarks.CoachmarksFlow


class MainActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createFirstFlow()
//        createSecondflow()
    }

    private fun createFirstFlow() {
        findViewById<View>(R.id.button).setOnClickListener {
            val relatedButton = Button(this)
            val coachMark1: Coachmark<Button> = Coachmark.Builder(relatedButton)
                .withViewId(R.id.button)
                .sizePercentage(100.0)
                .alignment(Coachmark.Alignment.RIGHT)
                .position(Coachmark.Position.BOTTOM)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val coachMark2: Coachmark<Button> = Coachmark.Builder(relatedButton)
                .withViewId(R.id.button2)
                .sizePercentage(100.0)
                .alignment(Coachmark.Alignment.LEFT)
                .position(Coachmark.Position.BOTTOM)
                .shape(Coachmark.Shape.RECTANGLE)
                .cornerRadius(10)
                .build()

            val coachMark3: Coachmark<Button> = Coachmark.Builder(relatedButton)
                .withViewId(R.id.button5)
                .sizePercentage(100.0)
                .alignment(Coachmark.Alignment.TOP)
                .position(Coachmark.Position.RIGHT)
                .padding(50, 0, 100, 0)
                .shape(Coachmark.Shape.CIRCLE)
                .build()

            val coachmarksFlow = CoachmarksFlow.with(this)
                .steps(listOf(coachMark1, coachMark2, coachMark3))
//                .animationVelocity(10)
                .initialDelay(1000)
                .build().apply {
                    dismissListener = object : CoachmarksFlow.OnCoackmarkDismissedListener {
                        override fun onCoachmarkDismissed() {
                            Log.d("FlexibleCoachmarkDemo", "Coachmark dismissed")
                        }
                    }
                    show()
                }

            relatedButton.setOnClickListener {
                coachmarksFlow.goNextStep()
            }
            relatedButton.text = "Next coachmark"
        }
    }
//
//    private fun createSecondflow() {
//        findViewById<View>(R.id.button2).setOnClickListener {
//            val coachmark = CoachmarksFlow(this@MainActivity)
//            val viewg: ViewGroup = GenerateView(this@MainActivity, R.layout.view_simple_layout)
//                .withButton(R.id.button9, R.string.title_activity_main,
//                    View.OnClickListener { coachmark.goNextStep() })
//                .withText(R.id.textView, R.string.title_activity_main)
//                .generate()
//            val c1: Coachmark<ViewGroup> = Coachmark(
//                R.id.button,
//                viewg,
//                Coachmark.POSITION_BOTTOM,
//                Coachmark.ALIGNMENT_RIGHT
//            )
//            c1.spotDiameterDp = 100
//            val c2: Coachmark<ViewGroup> = Coachmark(
//                R.id.button,
//                viewg,
//                Coachmark.POSITION_BOTTOM,
//                Coachmark.ALIGNMENT_RIGHT
//            )
//            c2.sizePercentage = 200.0
//            val c3: Coachmark<ViewGroup> = Coachmark(
//                R.id.button5,
//                viewg,
//                Coachmark.POSITION_RIGHT,
//                Coachmark.ALIGNMENT_TOP
//            )
//            c3.sizePercentage = 50.0
//            val buttonList: MutableList<Coachmark<ViewGroup>> = ArrayList()
//            buttonList.add(c1)
//            buttonList.add(c2)
//            buttonList.add(c3)
//            coachmark.setSteps(buttonList)
//            coachmark.dismissListener = object : CoachmarksFlow.OnCoackmarkDismissedListener {
//                override fun onCoachmarkDismissed() {
//                    Log.d("FlexibleCoachmarkDemo", "Coachmark dismissed")
//                }
//            }
//            coachmark.initialDelay = 1000
//            coachmark.show()
//        }
//
//        findViewById<View>(R.id.button5).setOnClickListener {
//            Log.d("FlexibleCoachmarkDemo", "Button 5 click")
//        }
//    }
}
