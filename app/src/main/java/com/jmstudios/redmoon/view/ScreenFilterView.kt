/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
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
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (c) 2015 Chris Nguyen
 *
 *     Permission to use, copy, modify, and/or distribute this software
 *     for any purpose with or without fee is hereby granted, provided
 *     that the above copyright notice and this permission notice appear
 *     in all copies.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 *     WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 *     WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE
 *     AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 *     CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 *     OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 *     NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 *     CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.jmstudios.redmoon.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.view.View

import com.jmstudios.redmoon.preference.ColorSeekBarPreference
import com.jmstudios.redmoon.preference.DimSeekBarPreference
import com.jmstudios.redmoon.preference.IntensitySeekBarPreference

class ScreenFilterView(context: Context) : View(context) {
    private var mDimAnimator: ValueAnimator? = null
    private var mIntensityAnimator: ValueAnimator? = null

    /**
     * Sets the dim level of the screen filter.
     *
     * @param dimLevel value between 0 and 100, inclusive, where 0 is doesn't darken, and 100 is the
     * *                 maximum allowed dim level determined by the system, but is guaranteed to
     * *                 never be fully opaque.
     */
    var filterDimLevel = DimSeekBarPreference.DEFAULT_VALUE
        set(value) {
            field = value
            invalidate()
            updateFilterColor()
        }
    /**
     * Sets the intensity of the screen filter.
     *
     * @param intensityLevel value between 0 and 100, inclusive, where 0 doesn't color the filter,
     * *                       and 100 is the maximum allowed intensity determined by the system, but
     * *                       is guaranteed to never be fully opaque.
     */
    var filterIntensityLevel = IntensitySeekBarPreference.DEFAULT_VALUE
        set(value) {
            field = value
            invalidate()
            updateFilterColor()
        }
    /**
     * Sets the progress of the color temperature slider of the screen filter.

     * @param colorTempProgress the progress of the color temperature slider.
     */
    var colorTempProgress = ColorSeekBarPreference.DEFAULT_VALUE
        set(value) {
            val colorTemperature = getColorTempFromProgress(value)
            mRgbColor = rgbFromColorTemperature(colorTemperature)
            invalidate()
            updateFilterColor()
        }
    private var mRgbColor = rgbFromColorTemperature(colorTempProgress)
    private var mFilterColor: Int = 0

    init {

        updateFilterColor()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(mFilterColor)
    }

/*
    fun animateShadesColor(toColor: Int) {
        cancelRunningAnimator(mColorAnimator)

        val fromColor = colorTempProgress

        mColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
        mColorAnimator!!.duration = FADE_DURATION_MS.toLong()
        mColorAnimator!!.addUpdateListener { valueAnimator ->
            colorTempProgress = valueAnimator.animatedValue as Int
        }
        mColorAnimator!!.start()
    }
*/

    fun cancelDimAnimator(){
        cancelRunningAnimator(mDimAnimator)
    }

    fun animateDimLevel(toDimLevel: Int, listener: Animator.AnimatorListener?) {
        cancelRunningAnimator(mDimAnimator)

        val fromDimLevel = filterDimLevel

        mDimAnimator = ValueAnimator.ofInt(fromDimLevel, toDimLevel)
        mDimAnimator!!.duration = FADE_DURATION_MS.toLong()
        mDimAnimator!!.addUpdateListener { valueAnimator -> filterDimLevel = valueAnimator.animatedValue as Int }

        if (listener != null) {
            mDimAnimator!!.addListener(listener)
        }

        mDimAnimator!!.start()
    }

    fun cancelIntensityAnimator(){
        cancelRunningAnimator(mIntensityAnimator)
    }

    fun animateIntensityLevel(toIntensityLevel: Int, listener: Animator.AnimatorListener?) {
        cancelRunningAnimator(mIntensityAnimator)

        val fromIntensityLevel = filterIntensityLevel

        mIntensityAnimator = ValueAnimator.ofInt(fromIntensityLevel, toIntensityLevel)
        mIntensityAnimator!!.duration = FADE_DURATION_MS.toLong()
        mIntensityAnimator!!.addUpdateListener { valueAnimator -> filterIntensityLevel = valueAnimator.animatedValue as Int }

        if (listener != null) {
            mIntensityAnimator!!.addListener(listener)
        }

        mIntensityAnimator!!.start()
    }

    private fun cancelRunningAnimator(animator: Animator?) {
        if (animator != null && animator.isRunning) {
            animator.cancel()
        }
    }

    private fun getFilterColor(rgbColor: Int, dimLevel: Int, intensityLevel: Int): Int {
        val intensityColor = Color.argb(floatToColorBits(intensityLevel.toFloat() / 100.0f),
                Color.red(rgbColor),
                Color.green(rgbColor),
                Color.blue(rgbColor))
        val dimColor = Color.argb(floatToColorBits(dimLevel.toFloat() / 100.0f), 0, 0, 0)
        return addColors(dimColor, intensityColor)
    }

    private fun updateFilterColor() {
        mFilterColor = getFilterColor(mRgbColor, filterDimLevel, filterIntensityLevel)
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
        val red = floatToColorBits((red1 * alpha1 + red2 * alpha2 * (1.0f - alpha1)) / fAlpha)
        val green = floatToColorBits((green1 * alpha1 + green2 * alpha2 * (1.0f - alpha1)) / fAlpha)
        val blue = floatToColorBits((blue1 * alpha1 + blue2 * alpha2 * (1.0f - alpha1)) / fAlpha)

        return Color.argb(alpha, red, green, blue)
    }

    companion object {
        const val MIN_DIM = 0
        const val MIN_INTENSITY = 0
        // private val MAX_DIM = 100f
        // private val MIN_ALPHA = 0x00f
        // private val MAX_ALPHA = 0.75f
        // private val MAX_DARKEN = 0.75f

        const val DIM_MAX_ALPHA = 0.9f
        private val INTENSITY_MAX_ALPHA = 0.75f
        private val ALPHA_ADD_MULTIPLIER = 0.75f

        const val FADE_DURATION_MS = 1000

        fun rgbFromColorProgress(colorTempProgress: Int): Int {
            val colorTemperature = getColorTempFromProgress(colorTempProgress)

            return rgbFromColorTemperature(colorTemperature)
        }

        fun getColorTempFromProgress(colorTempProgress: Int): Int {
            return 500 + colorTempProgress * 30
        }

        private fun rgbFromColorTemperature(colorTemperature: Int): Int {
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

        fun getIntensityColor(intensityLevel: Int, colorTempProgress: Int): Int {
            val rgbColor = rgbFromColorTemperature(getColorTempFromProgress(colorTempProgress))
            val red = Color.red(rgbColor).toFloat()
            val green = Color.green(rgbColor).toFloat()
            val blue = Color.blue(rgbColor).toFloat()
            val intensity = 1.0f - intensityLevel.toFloat() / 100.0f

            return Color.argb(255,
                    (red + (255.0f - red) * intensity).toInt(),
                    (green + (255.0f - green) * intensity).toInt(),
                    (blue + (255.0f - blue) * intensity).toInt())
        }

        private fun colorBitsToFloat(bits: Int): Float {
            return bits.toFloat() / 255.0f
        }

        private fun floatToColorBits(color: Float): Int {
            return (color * 255.0f).toInt()
        }
    }
}
