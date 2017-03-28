/*
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

package com.jmstudios.redmoon.application

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.receiver.TimeToggleChangeReceiver
import com.jmstudios.redmoon.util.Logger

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class RedMoonApplication: Application(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val mSharedPrefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(this)

    override fun onCreate() {
        app = this
        super.onCreate()
        //EventBus.builder().addIndex(eventBusIndex()).installDefaultEventBus()
        EventBus.getDefault().register(this)
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this)
        Log.d("Opened Settings change listener")
    }

    // Only called in emulated environments. In production, just gets killed.
    override fun onTerminate() {
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
        EventBus.getDefault().unregister(this)
        Log.d("Closed Settings change listener")
        super.onTerminate()
    }

    //region OnSharedPreferenceChangeListener
    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        Log.i("onPreferenceChanged: $key")
        EventBus.getDefault().post(when (key) {
            getString(R.string.pref_key_filter_is_on)         -> filterIsOnChanged()
            getString(R.string.pref_key_dim)                  -> dimChanged()
            getString(R.string.pref_key_intensity)            -> intensityChanged()
            getString(R.string.pref_key_color)                -> colorChanged()
            // getString(R.string.pref_key_dark_theme)           -> themeChanged()
            getString(R.string.pref_key_time_toggle)          -> timeToggleChanged()
            getString(R.string.pref_key_custom_turn_on_time)  -> customTurnOnTimeChanged()
            getString(R.string.pref_key_custom_turn_off_time) -> customTurnOffTimeChanged()
            getString(R.string.pref_key_lower_brightness)     -> lowerBrightnessChanged()
            getString(R.string.pref_key_profile_spinner)      -> profileChanged()
            getString(R.string.pref_key_num_profiles)         -> amountProfilesChanged()
            getString(R.string.pref_key_use_location)         -> useLocationChanged()
            getString(R.string.pref_key_location)             -> locationChanged()
            getString(R.string.pref_key_sunset_time)          -> sunsetTimeChanged()
            getString(R.string.pref_key_sunrise_time)         -> sunriseTimeChanged()
            getString(R.string.pref_key_secure_suspend)       -> secureSuspendChanged()
            getString(R.string.pref_key_button_backlight)     -> buttonBacklightChanged()
            else -> return
            /* Preferences for which no Event is posted */
            // getString(R.string.pref_key_brightness_level)
            // getString(R.string.pref_key_intro_shown)
            // getString(R.string.pref_key_dim_buttons)
        })
    }
    //endregion

    // There's probably a better place to do this to keep this class clean
    // For now it works, though
    @Subscribe
    fun onTimeToggleChanged(event: timeToggleChanged) {
        Log.i("Timer turned ${if (Config.timeToggle) "on" else "off"}")
        if (Config.timeToggle) {
            TimeToggleChangeReceiver.rescheduleOnCommand()
            TimeToggleChangeReceiver.rescheduleOffCommand()
        } else {
            TimeToggleChangeReceiver.cancelAlarms()
        }
    }

    @Subscribe
    fun onCustomTurnOnTimeChanged(event: customTurnOnTimeChanged) {
        TimeToggleChangeReceiver.rescheduleOnCommand()
    }

    @Subscribe
    fun onCustomTurnOffTimeChanged(event: customTurnOffTimeChanged) {
        TimeToggleChangeReceiver.rescheduleOffCommand()
    }

    @Subscribe
    fun onLocationChanged(event: locationChanged) {
        TimeToggleChangeReceiver.rescheduleOffCommand()
        TimeToggleChangeReceiver.rescheduleOnCommand()
    }

    companion object : Logger() {
        lateinit var app: RedMoonApplication
    }
}
