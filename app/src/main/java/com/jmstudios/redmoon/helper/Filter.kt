package com.jmstudios.redmoon.helper

import android.graphics.Color

// private val MAX_DIM = 100f
// private val MIN_ALPHA = 0x00f
// private val MAX_ALPHA = 0.75f
// private val MAX_DARKEN = 0.75f

const val DIM_MAX_ALPHA = 0.9f
//private const val INTENSITY_MAX_ALPHA  = 0.75f
//private const val ALPHA_ADD_MULTIPLIER = 0.75f

fun getColorTemperature(color: Int): Int = 500 + color * 30

fun rgbFromColor(color: Int): Int {
    val colorTemperature = getColorTemperature(color)
    val alpha = 255 // alpha is managed separately

    // After: http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
    val temp = colorTemperature.toDouble() / 100.0f

    var red: Double
    if (temp <= 66)
        red = 255.0
    else {
        red = temp - 60
        red = 329.698727446 * Math.pow(red, -0.1332047592)
        if (red < 0) red = 0.0
        if (red > 255) red = 255.0
    }

    var green: Double
    if (temp <= 66) {
        green = temp
        green = 99.4708025861 * Math.log(green) - 161.1195681661
        if (green < 0) green = 0.0
        if (green > 255) green = 255.0
    } else {
        green = temp - 60
        green = 288.1221695283 * Math.pow(green, -0.0755148492)
        if (green < 0) green = 0.0
        if (green > 255) green = 255.0
    }

    var blue: Double
    if (temp >= 66)
        blue = 255.0
    else {
        if (temp < 19)
            blue = 0.0
        else {
            blue = temp - 10
            blue = 138.5177312231 * Math.log(blue) - 305.0447927307
            if (blue < 0) blue = 0.0
            if (blue > 255) blue = 255.0
        }
    }

    return Color.argb(alpha, red.toInt(), green.toInt(), blue.toInt())
}

fun getIntensityColor(intensityLevel: Int, color: Int): Int {
    val argb = rgbFromColor(color)
    val red   = Color.red  (argb).toFloat()
    val green = Color.green(argb).toFloat()
    val blue  = Color.blue (argb).toFloat()
    val intensity = 1.0f - intensityLevel.toFloat() / 100.0f

    return Color.argb(255,
                      (red +   (255.0f - red  ) * intensity).toInt(),
                      (green + (255.0f - green) * intensity).toInt(),
                      (blue +  (255.0f - blue ) * intensity).toInt())
}