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
package com.jmstudios.redmoon.model

import android.preference.PreferenceManager

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.preference.ColorSeekBarPreference
import com.jmstudios.redmoon.preference.DimSeekBarPreference
import com.jmstudios.redmoon.preference.IntensitySeekBarPreference
import com.jmstudios.redmoon.util.*

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import java.util.Calendar
import java.util.TimeZone

/**
 * This singleton provides allows easy access to the shared preferences
 */
object Config {
    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)

    private fun getBooleanPref(resId: Int, default: Boolean): Boolean {
        return sharedPrefs.getBoolean(getString(resId), default)
    }

    private fun putBooleanPref(resId: Int, v: Boolean) {
        sharedPrefs.edit().putBoolean(getString(resId), v).apply()
    }

    private fun getIntPref(resId: Int, default: Int): Int {
        return sharedPrefs.getInt(getString(resId), default)
    }

    private fun putIntPref(resId: Int, v: Int) {
        sharedPrefs.edit().putInt(getString(resId), v).apply()
    }

    private fun getLongPref(resId: Int, default: Long): Long {
        return sharedPrefs.getLong(getString(resId), default)
    }

    private fun putLongPref(resId: Int, v: Long) {
        sharedPrefs.edit().putLong(getString(resId), v).apply()
    }

    private fun getStringPref(resId: Int, default: String): String {
        return sharedPrefs.getString(getString(resId), default)
    }

    private fun putStringPref(resId: Int, v: String) {
        sharedPrefs.edit().putString(getString(resId), v).apply()
    }

    //region preferences
    var filterIsOn: Boolean
        get()   = getBooleanPref(R.string.pref_key_filter_is_on, false)
        set(on) = putBooleanPref(R.string.pref_key_filter_is_on, on)
    
    var amountProfiles: Int
        get()  = getIntPref(R.string.pref_key_num_profiles, 3)
        set(n) = putIntPref(R.string.pref_key_num_profiles, n)

    var profile: Int
        get()  = getIntPref(R.string.pref_key_profile_spinner, 1)
        set(p) = putIntPref(R.string.pref_key_profile_spinner, p)
    
    var color: Int
        get()  = getIntPref(R.string.pref_key_color, ColorSeekBarPreference.DEFAULT_VALUE)
        set(c) = putIntPref(R.string.pref_key_color, c)

    var intensity: Int
        get()  = getIntPref(R.string.pref_key_intensity, IntensitySeekBarPreference.DEFAULT_VALUE)
        set(i) = putIntPref(R.string.pref_key_intensity, i)

    var dim: Int
        get()  = getIntPref(R.string.pref_key_dim, DimSeekBarPreference.DEFAULT_VALUE)
        set(d) = putIntPref(R.string.pref_key_dim, d)

    var lowerBrightness: Boolean
        get()   = getBooleanPref(R.string.pref_key_lower_brightness, false)
        set(lb) = putBooleanPref(R.string.pref_key_lower_brightness, lb)

    val secureSuspend: Boolean
        get() = getBooleanPref(R.string.pref_key_secure_suspend, false)

    val buttonBacklightFlag: String
        get() = getStringPref(R.string.pref_key_button_backlight, "off")
    
    var darkThemeFlag: Boolean
        get()     = getBooleanPref(R.string.pref_key_dark_theme, false)
        set(flag) = putBooleanPref(R.string.pref_key_dark_theme, flag)

    var timeToggle: Boolean
        get() = getBooleanPref(R.string.pref_key_time_toggle, false)
        set(t) = putBooleanPref(R.string.pref_key_time_toggle, t)

    val customTurnOnTime: String
        get() = getStringPref(R.string.pref_key_custom_turn_on_time, "22:00")

    val customTurnOffTime: String
        get() = getStringPref(R.string.pref_key_custom_turn_off_time, "06:00")

    var useLocation: Boolean
        get() = getBooleanPref(R.string.pref_key_use_location, false)
        set(t) = putBooleanPref(R.string.pref_key_use_location, t)
    //endregion

    //region state
    val activeTheme: Int
        get() = if (darkThemeFlag) { R.style.AppThemeDark } else { R.style.AppTheme }

    val buttonBacklightLevel: Float
        get() = when (buttonBacklightFlag) {
                    "system" -> -1.toFloat()
                    "dim" -> 1 - (dim.toFloat() / 100)
                    else -> 0.toFloat()
                }

    val automaticTurnOnTime: String
        get() = if (useLocation) sunsetTime else customTurnOnTime

    val automaticTurnOffTime: String
        get() = if (useLocation) sunriseTime else customTurnOffTime
    
    const val DEFAULT_LOCATION = "0,0"
    const val NOT_SET: Long = -1
    var location: Triple<String, String, Long?>
        get() {
            val l = getStringPref(R.string.pref_key_location, DEFAULT_LOCATION)
            val latitude  = l.substringBefore(',')
            val longitude = l.substringAfter(',')
            val t = getLongPref(R.string.pref_key_location_timestamp, NOT_SET)
            val timestamp = if (t == NOT_SET) null else t
            return Triple(latitude, longitude, timestamp)
        }
        set(l) {
            putLongPref(R.string.pref_key_location_timestamp, l.third ?: NOT_SET)
            putStringPref(R.string.pref_key_location, l.first + "," + l.second)
        }

    const val DEFAULT_SUNSET = "19:30"
    val sunsetTime: String
        get() {
            val (latitude, longitude, time) = location
            return if (time == null) {
                DEFAULT_SUNSET
            } else {
                val sunLocation = com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude)
                val calculator  = SunriseSunsetCalculator(sunLocation, TimeZone.getDefault())
                calculator.getOfficialSunsetForDate(Calendar.getInstance())
            }
        }

    const val DEFAULT_SUNRISE = "06:30"
    val sunriseTime: String
        get() {
            val (latitude, longitude, time) = location
            return if (time == null) {
                DEFAULT_SUNRISE
            } else {
                val sunLocation = com.luckycatlabs.sunrisesunset.dto.Location(latitude, longitude)
                val calculator  = SunriseSunsetCalculator(sunLocation, TimeZone.getDefault())
                calculator.getOfficialSunriseForDate(Calendar.getInstance())
            }
        }

    var introShown: Boolean
        get()  = getBooleanPref(R.string.pref_key_intro_shown, false)
        set(s) = putBooleanPref(R.string.pref_key_intro_shown, s)

    var brightness: Int
        get()  = getIntPref(R.string.pref_key_brightness, 0)
        set(b) = putIntPref(R.string.pref_key_brightness, b)
    
    var automaticBrightness: Boolean
        get()  = getBooleanPref(R.string.pref_key_automatic_brightness, true)
        set(a) = putBooleanPref(R.string.pref_key_automatic_brightness, a)
    //endregion

    //region application
    var fromVersionCode: Int
        get() = getIntPref(R.string.pref_key_from_version_code, 0)
        set(c) = putIntPref(R.string.pref_key_from_version_code, c)
    //endregion
}
