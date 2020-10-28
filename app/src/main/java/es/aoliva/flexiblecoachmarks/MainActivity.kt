package es.aoliva.flexiblecoachmarks

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import es.aoliva.coachmarks.Coachmark
import es.aoliva.coachmarks.FlexibleCoachmark
import es.aoliva.coachmarks.GenerateView


class MainActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button).setOnClickListener {
            val coachmark = FlexibleCoachmark(this@MainActivity)
            val relatedButton = Button(this@MainActivity)
            relatedButton.setOnClickListener {
                coachmark.nextStep()
            }
            relatedButton.setText("Next coachmark")
            val c1: Coachmark<Button> = Coachmark<Button>(R.id.button, relatedButton, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_RIGHT, Coachmark.Type.RECTANGLE)
            c1.spotDiameterPercentage = 150.0
            val c2: Coachmark<Button> = Coachmark<Button>(R.id.button2, relatedButton, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_LEFT)
            c2.spotDiameterPercentage = 100.0
            val c3: Coachmark<Button> = Coachmark<Button>(R.id.button5, relatedButton, Coachmark.POSITION_RIGHT, Coachmark.ALIGNMENT_TOP)
            c3.spotDiameterPercentage = 100.0
            c3.setPaddings(0,20,5,10)
            val buttonList: MutableList<Coachmark<Button>> = ArrayList()
            buttonList.add(c1)
            buttonList.add(c2)
            buttonList.add(c3)
            coachmark.setSteps(buttonList)
            coachmark.dismissListener = object : FlexibleCoachmark.OnCoackmarkDismissedListener {
                override fun onCoachmarkDismissed() {
                    Log.d("FlexibleCoachmarkDemo", "Coachmark dismissed")
                }
            }
            coachmark.initialDelay = 1000
            coachmark.show()
        }
        findViewById<View>(R.id.button2).setOnClickListener {
            val coachmark = FlexibleCoachmark(this@MainActivity)
            val viewg: ViewGroup = GenerateView(this@MainActivity, R.layout.view_simple_layout)
                    .withButton(R.id.button9, R.string.title_activity_main) {
                        coachmark.nextStep()
                    }
                    .withText(R.id.textView, R.string.title_activity_main)
                    .generate()
            val c1: Coachmark<ViewGroup> = Coachmark<ViewGroup>(R.id.button, viewg, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_RIGHT)
            c1.spotDiameterDp = 100
            val c2: Coachmark<ViewGroup> = Coachmark<ViewGroup>(R.id.button, viewg, Coachmark.POSITION_BOTTOM, Coachmark.ALIGNMENT_RIGHT)
            c2.spotDiameterPercentage = 200.0
            val c3: Coachmark<ViewGroup> = Coachmark<ViewGroup>(R.id.button5, viewg, Coachmark.POSITION_RIGHT, Coachmark.ALIGNMENT_TOP)
            c3.spotDiameterPercentage = 50.0
            val buttonList: MutableList<Coachmark<ViewGroup>> = ArrayList()
            buttonList.add(c1)
            buttonList.add(c2)
            buttonList.add(c3)
            coachmark.setSteps<ViewGroup>(buttonList)
            coachmark.dismissListener = object : FlexibleCoachmark.OnCoackmarkDismissedListener {
                override fun onCoachmarkDismissed() {
                    Log.d("FlexibleCoachmarkDemo", "Coachmark dismissed")
                }
            }
            coachmark.initialDelay = 1000
            coachmark.show()
        }

        findViewById<View>(R.id.button5).setOnClickListener {
            Log.d("FlexibleCoachmarkDemo", "Button 5 click")
        }
    }
}