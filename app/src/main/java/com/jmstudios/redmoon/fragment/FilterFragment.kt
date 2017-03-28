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
package com.jmstudios.redmoon.fragment

import android.os.Bundle
import android.preference.Preference
import android.preference.TwoStatePreference

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.preference.ProfileSelectorPreference
import com.jmstudios.redmoon.preference.SeekBarPreference
import com.jmstudios.redmoon.util.hasWriteSettingsPermission
import com.jmstudios.redmoon.util.requestWriteSettingsPermission
import com.jmstudios.redmoon.util.Logger

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class FilterFragment : EventPreferenceFragment() {
    //private var hasShownWarningToast = false
    companion object : Logger()

    // Preferences
    private val profileSelectorPref: ProfileSelectorPreference
             get() = (preferenceScreen.findPreference
                     (getString(R.string.pref_key_profile_spinner)) as ProfileSelectorPreference)

    private val colorPref: SeekBarPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_color)) as SeekBarPreference)

    private val intensityPref: SeekBarPreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_intensity)) as SeekBarPreference)

    private val dimPref: SeekBarPreference
        get()= (preferenceScreen.findPreference
               (getString(R.string.pref_key_dim)) as SeekBarPreference)

    private val lowerBrightnessPref: TwoStatePreference
        get() = (preferenceScreen.findPreference
                (getString(R.string.pref_key_lower_brightness)) as TwoStatePreference)

    private val timeTogglePref: Preference
        get() = (preferenceScreen.findPreference(getString(R.string.pref_key_time_toggle_header)))

    private val secureSuspendPref: Preference
        get() = preferenceScreen.findPreference(getString(R.string.pref_key_secure_suspend_header))

    private val buttonBacklightPref: Preference
        get() = preferenceScreen.findPreference(getString(R.string.pref_key_button_backlight))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.filter_preferences)

        if (!hasWriteSettingsPermission) { lowerBrightnessPref.isChecked = false }
        updateSecureSuspendSummary()
        updateTimeToggleSummary()
        updateBacklightPrefSummary()

        lowerBrightnessPref.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val checked = newValue as Boolean
                    if (checked) { requestWriteSettingsPermission(activity) } else { true }
                }
    }

    override fun onResume() {
        Log.i("onResume")
        super.onResume()
        EventBus.getDefault().register(profileSelectorPref)
        updateSecureSuspendSummary()
        updateTimeToggleSummary()
    }

    override fun onPause() {
        EventBus.getDefault().unregister(profileSelectorPref)
        super.onPause()
    }

    private fun updateTimeToggleSummary() {
        timeTogglePref.setSummary(if (Config.timeToggle) R.string.text_switch_on
                                  else R.string.text_switch_off)
    }

    private fun updateSecureSuspendSummary() {
        secureSuspendPref.setSummary(if (Config.secureSuspend) R.string.text_switch_on
                                     else R.string.text_switch_off)
    }

    private fun updateBacklightPrefSummary() {
        buttonBacklightPref.setSummary(when(Config.buttonBacklightFlag) {
            "system" -> R.string.pref_button_backlight_entries_array_0
            "dim"    -> R.string.pref_button_backlight_entries_array_1
            else     -> R.string.pref_button_backlight_entries_array_2
        })
    }

    //region presenter
    @Subscribe
    fun onProfileChanged(event: profileChanged) {
        Log.i("Profile changed. profile: ${Config.profile}, color: ${Config.color}, intensity: " +
            "${Config.intensity}, dim: ${Config.dim}, lowerBrightness: ${Config.lowerBrightness}")
        colorPref.setProgress(Config.color)
        intensityPref.setProgress(Config.intensity)
        dimPref.setProgress(Config.dim)
        lowerBrightnessPref.isChecked = Config.lowerBrightness
    }

    @Subscribe
    fun onButtonBacklightChanged(event: buttonBacklightChanged) {
        updateBacklightPrefSummary()
    }
    //endregion
}
