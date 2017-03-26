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
package com.jmstudios.redmoon.preference

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.ScreenFilterService

abstract class SeekBarPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), SeekBar.OnSeekBarChangeListener {

    companion object : Logger()

    lateinit var mSeekBar: SeekBar
    protected var mProgress: Int = 0
    lateinit protected var mView: View

    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    abstract val DEFAULT_VALUE: Int
    abstract val colorFilter: PorterDuffColorFilter
    abstract val progress: Int
    abstract val suffix: String

    init {
        layoutResource = R.layout.preference_seekbar
    }

    fun setProgress(progress: Int) {
        mSeekBar.progress = progress
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInteger(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            mProgress = getPersistedInt(DEFAULT_VALUE)
        } else {
            mProgress = (defaultValue as Int?) ?: DEFAULT_VALUE
            persistInt(mProgress)
        }
    }

    override fun onBindView(view: View) {
        Log.i("onBindView")
        super.onBindView(view)
        mView = view
        mSeekBar = view.findViewById(R.id.seekbar) as SeekBar
        mSeekBar.progress = mProgress
        mSeekBar.setOnSeekBarChangeListener(this)
        updateMoonIcon()
        updateProgressText()
    }

    //region OnSeekBarChangedListener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        Log.i("onSeekbarProgressChanged, storing value")
        mProgress = progress
        persistInt(mProgress)
        updateMoonIcon()
        updateProgressText()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        Log.d("Touch down on a seek bar")
        ScreenFilterService.moveToState(ScreenFilterService.Command.SHOW_PREVIEW)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        Log.d("Released a seek bar")
        ScreenFilterService.moveToState(ScreenFilterService.Command.HIDE_PREVIEW)
    }
    //end region

    fun updateMoonIcon() {
        if (isEnabled) {
            val moonIcon = mView.findViewById(R.id.moon_icon) as ImageView
            moonIcon.colorFilter = colorFilter
        }
    }

    fun updateProgressText() {
        val progressView = mView.findViewById(R.id.seekbar_value) as TextView
        progressView.text = String.format("%d%s", progress, suffix)
    }

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
}

class ColorSeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    // TODO: Get the default value from the XML and handle it in the parent class
    companion object : Logger()

    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    override val DEFAULT_VALUE = Profile.DEFAULT_COLOR
    override val suffix = "K"
        
    override val colorFilter: PorterDuffColorFilter
        get() {
            val color = rgbFromColor(mProgress)
            return PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }

    override val progress: Int
        get() = getColorTemperature(mProgress)
}

class IntensitySeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {

    // TODO: Get the default value from the XML and handle it in the parent class
    companion object : Logger()

    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    override val DEFAULT_VALUE = Profile.DEFAULT_INTENSITY
    override val suffix = "%"

    override val colorFilter: PorterDuffColorFilter
        get() {
            val color = getIntensityColor(mProgress, Config.color)
            return PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }

    override val progress: Int
        get() = mProgress

    private fun getIntensityColor(intensityLevel: Int, color: Int): Int {
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
}

class DimSeekBarPreference(context: Context, attrs: AttributeSet) : SeekBarPreference(context, attrs) {
    companion object : Logger()

    val DIM_MAX_ALPHA = 0.9f

    // Changes to DEFAULT_VALUE should be reflected in preferences.xml
    override val DEFAULT_VALUE = Profile.DEFAULT_DIM_LEVEL
    override val suffix = "%"

    override val colorFilter: PorterDuffColorFilter
        get() {
            val lightness = 102 + ((100 - mProgress).toFloat() * (2.55f * 0.6f)).toInt()
            val color = Color.rgb(lightness, lightness, lightness)
            return PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }

    override val progress: Int
        get() = (mProgress.toFloat() * DIM_MAX_ALPHA).toInt()
}