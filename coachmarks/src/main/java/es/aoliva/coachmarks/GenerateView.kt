package es.aoliva.coachmarks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

/**
 * Created by antonio on 11/25/17.
 */

class GenerateView(context: Context, layoutId: Int) {

    private var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var root: ViewGroup

    init {
        root = layoutInflater.inflate(layoutId, null) as ViewGroup
    }

    /**
     *
     */
    fun withText(viewId: Int, stringRes: Int): GenerateView {
        val textView = root.findViewById<TextView>(viewId) ?: throw IllegalArgumentException("Provided textview id not found: $viewId")

        textView.setText(stringRes)
        return this
    }

    /**
     *
     */
    fun withButton(viewId: Int, listener: View.OnClickListener) = withButton(viewId, null, listener)

    /**
     *
     */
    fun withButton(viewId: Int, stringRes: Int?, listener: View.OnClickListener): GenerateView {
        val button = root.findViewById<Button>(viewId) ?: throw IllegalArgumentException("Provided button id not found: $viewId")

        button.setOnClickListener(listener)
        stringRes?.let {
            button.setText(stringRes)
        }
        return this
    }

    /**
     *
     */
    fun withClickListener(viewId: Int, listener: View.OnClickListener): GenerateView {
        root.findViewById<View>(viewId)?.setOnClickListener(listener) ?: throw IllegalArgumentException("Provided view id not found: $viewId")
        return this
    }

    /**
     *
     */
    fun generate() = root
}