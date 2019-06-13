package sh.hadi.bark.ui

import android.graphics.Color
import sh.hadi.bark.BarkLevel

fun getMainColor(level: BarkLevel) =
        when (level) {
            BarkLevel.TRACE -> Color.parseColor("#a6a7a8")
            BarkLevel.DEBUG -> Color.parseColor("#6c757d")
            BarkLevel.INFO -> Color.parseColor("#17a2b8")
            BarkLevel.WARN -> Color.parseColor("#f2ac15")
            BarkLevel.ERROR -> Color.parseColor("#dc3545")
            BarkLevel.WTF -> Color.parseColor("#750e0f")
        }

fun getSecondaryColor(level: BarkLevel) =
        when (level) {
            BarkLevel.TRACE -> Color.parseColor("#b7b8b9")
            BarkLevel.DEBUG -> Color.parseColor("#747e86")
            BarkLevel.INFO -> Color.parseColor("#1fc8e3")
            BarkLevel.WARN -> Color.parseColor("#ffb339")
            BarkLevel.ERROR -> Color.parseColor("#e4606d")
            BarkLevel.WTF -> Color.parseColor("#931213")
        }

fun getTextColor(level: BarkLevel) =
        when (level) {
            BarkLevel.TRACE -> Color.WHITE
            BarkLevel.DEBUG -> Color.WHITE
            BarkLevel.INFO -> Color.WHITE
            BarkLevel.WARN -> Color.WHITE
            BarkLevel.ERROR -> Color.WHITE
            BarkLevel.WTF -> Color.WHITE
        }