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
package com.jmstudios.redmoon.fragment

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.jmstudios.redmoon.BuildConfig

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.util.Logger
import de.cketti.library.changelog.ChangeLog

class AboutFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("onCreate()")
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.about)
        preferenceScreen.getPreference(0).apply{
            summary = BuildConfig.VERSION_NAME
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                ChangeLog(activity).logDialog.show()
                true
            }
        }
    }
    companion object : Logger()
}
