package es.aoliva.coachmarks

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

internal fun View.fadeIn() {
    fadeIn(300)
}

internal fun View.fadeIn(duration: Long) {
    fade(duration, 0f, 1f, null)
}

internal fun View.fadeOut() {
    fadeOut(300)
}

internal fun View.fadeOut(listener: () -> Unit) {
    fadeOut(300, listener)
}

internal fun View.fadeOut(duration: Long) {
    fade(duration, 1f, 0f, null)
}

internal fun View.fadeOut(duration: Long, listener: () -> Unit) {
    fade(duration, 1f, 0f, listener)
}

internal fun View.fade(duration: Long, fromAlpha: Float, toAlpha: Float, listener: (() -> Unit)?) {
    animation = AlphaAnimation(fromAlpha, toAlpha)
    animation.duration = duration
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }
        override fun onAnimationStart(animation: Animation?) {
        }
        override fun onAnimationEnd(animation: Animation?) {
            visibility = if (toAlpha == 0f) View.INVISIBLE else View.VISIBLE
            listener?.invoke()
        }
    })
    startAnimation(animation)
}