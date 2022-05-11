package com.aoliva.coachmarks.extensions

import android.content.Context
import androidx.core.text.TextUtilsCompat

internal fun Context.localeDirection() =
    TextUtilsCompat.getLayoutDirectionFromLocale(resources.configuration.locale)
