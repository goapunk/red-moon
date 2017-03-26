/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This file is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jmstudios.redmoon.helper

import android.graphics.Color

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.util.getString

import org.json.JSONObject

/**
 *
 * Color temperature slider of the screen filter.
 *
 * Value between 0 and 100, inclusive, where 0 is doesn't darken, and 100
 * is the maximum allowed dim level determined by the system, but is
 * guaranteed to never be fully opaque.
 *
 * Value between 0 and 100, inclusive, where 0 doesn't color the filter, and
 * 100 is the maximum allowed intensity determined by the system, but is
 * guaranteed to never be fully opaque.
 */

data class Profile(
        val name:            String  = NAME_CUSTOM,
        val color:           Int     = DEFAULT_COLOR,
        val intensity:       Int     = DEFAULT_INTENSITY,
        val dimLevel:        Int     = DEFAULT_DIM_LEVEL,
        val lowerBrightness: Boolean = false) {

    override fun toString() = JSONObject().run {
        put(KEY_COLOR, color)
        put(KEY_INTENSITY, intensity)
        put(KEY_DIM, dimLevel)
        put(KEY_LOWER_BRIGHTNESS, lowerBrightness)
        put(KEY_NAME, name)
        toString()
    }

    val filterColor: Int
        get() {
            val rgbColor = rgbFromColor(color)
            val intensityColor = Color.argb(floatToColorBits(intensity.toFloat() / 100.0f),
                                            Color.red(rgbColor),
                                            Color.green(rgbColor),
                                            Color.blue(rgbColor))
            val dimColor = Color.argb(floatToColorBits(dimLevel.toFloat() / 100.0f), 0, 0, 0)
            return addColors(dimColor, intensityColor)
        }

    private fun addColors(color1: Int, color2: Int): Int {
        var alpha1 = colorBitsToFloat(Color.alpha(color1))
        var alpha2 = colorBitsToFloat(Color.alpha(color2))
        val red1 = colorBitsToFloat(Color.red(color1))
        val red2 = colorBitsToFloat(Color.red(color2))
        val green1 = colorBitsToFloat(Color.green(color1))
        val green2 = colorBitsToFloat(Color.green(color2))
        val blue1 = colorBitsToFloat(Color.blue(color1))
        val blue2 = colorBitsToFloat(Color.blue(color2))

        // See: http://stackoverflow.com/a/10782314

        // Alpha changed to allow more control
        val fAlpha = alpha2 * INTENSITY_MAX_ALPHA + (DIM_MAX_ALPHA - alpha2 * INTENSITY_MAX_ALPHA) * alpha1
        alpha1 *= ALPHA_ADD_MULTIPLIER
        alpha2 *= ALPHA_ADD_MULTIPLIER

        val alpha = floatToColorBits(fAlpha)
        val red   = floatToColorBits((red1   * alpha1 + red2   * alpha2 * (1.0f - alpha1)) / fAlpha)
        val green = floatToColorBits((green1 * alpha1 + green2 * alpha2 * (1.0f - alpha1)) / fAlpha)
        val blue  = floatToColorBits((blue1  * alpha1 + blue2  * alpha2 * (1.0f - alpha1)) / fAlpha)

        return Color.argb(alpha, red, green, blue)
    }

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_COLOR = "color"
        private const val KEY_INTENSITY = "intensity"
        private const val KEY_DIM = "dim"
        private const val KEY_LOWER_BRIGHTNESS = "lower-brightness"

        private const val MIN_DIM_LEVEL = 0
        private const val MIN_INTENSITY = 0

        private val NAME_CUSTOM = getString(R.string.standard_profiles_array_0)

        const val DEFAULT_DIM_LEVEL = MIN_DIM_LEVEL
        const val DEFAULT_INTENSITY = MIN_INTENSITY
        const val DEFAULT_COLOR     = 10

        private const val INTENSITY_MAX_ALPHA  = 0.75f
        private const val ALPHA_ADD_MULTIPLIER = 0.75f

        private fun colorBitsToFloat(bits:  Int): Float = bits.toFloat() / 255.0f
        private fun floatToColorBits(color: Float): Int = (color * 255.0f).toInt()

        internal fun parse(entry: String): Profile = JSONObject(entry).run {
            val name      = optString(KEY_NAME)
            val color     = optInt(KEY_COLOR)
            val intensity = optInt(KEY_INTENSITY)
            val dim       = optInt(KEY_DIM)
            val lowerBrightness = optBoolean(KEY_LOWER_BRIGHTNESS)
            Profile(name, color, intensity, dim, lowerBrightness)
        }
    }
}
