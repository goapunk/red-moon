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

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.appContext
import com.jmstudios.redmoon.util.Logger

import com.jmstudios.redmoon.model.ProfilesModel

object ProfilesHelper : Logger() {
    const val DEFAULT_OPERATIONS_AM = 3

    // TODO: Hold a reference instead of getting a new model each time
    private val model: ProfilesModel
        get() = ProfilesModel(appContext)

    fun getProfileName(profile: Int): String {
        Log.i("getProfileName $profile")
        if (profile < DEFAULT_OPERATIONS_AM) {
            return appContext.resources.getStringArray(R.array.standard_profiles_array)[profile]
        } else {
            return model.getProfile(profile - DEFAULT_OPERATIONS_AM).mName
        }
    }

    fun getProfile(profile: Int): ProfilesModel.Profile {
        Log.i("getProfile $profile")
        val name = getProfileName(profile)
        return  when (profile) {
              // ProfilesModel.Profile(name, color, intensity, dim, lowerBrightness)
            0 -> ProfilesModel.Profile(name, 0, 0, 0, false)
            1 -> ProfilesModel.Profile(name, 10, 30, 40, false)
            2 -> ProfilesModel.Profile(name, 20, 60, 78, false)
            else -> model.getProfile(profile - DEFAULT_OPERATIONS_AM)
        }
    }

    fun setProfile(profile: Int) {
        Log.i("setProfile: $profile")
        // TODO: Allow updating the profile before the related settings without causing bugs
        // Update settings that are based on the profile
        if (profile != 0) {
            getProfile(profile).apply {
                Log.i("color=$mColor, intensity=$mIntensity, dim=$mDim, lb=$mLowerBrightness")
                Config.color = mColor
                Config.intensity = mIntensity
                Config.dim = mDim
                Config.lowerBrightness = mLowerBrightness
            }
        }
        Config.profile = profile
    }

    fun addProfile(name: String) {
        Log.i("addProfile $name")
        val profile = ProfilesModel.Profile(name,
                                            Config.color,
                                            Config.intensity,
                                            Config.dim,
                                            Config.lowerBrightness)
        model.addProfile(profile)
        updateAmountProfiles()
        val i = model.profiles.size - 1 + DEFAULT_OPERATIONS_AM
        setProfile(i)
    }

    fun removeProfile(profile: Int) {
        Log.i("removeProfile $profile")
        model.removeProfile(profile - DEFAULT_OPERATIONS_AM)
        updateAmountProfiles()
        setProfile(0)
    }

    private fun updateAmountProfiles() {
        Config.amountProfiles = model.profiles.size + DEFAULT_OPERATIONS_AM
        Log.i("There are now ${Config.amountProfiles} profiles.")
    }
}
