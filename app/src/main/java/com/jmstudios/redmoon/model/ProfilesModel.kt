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
package com.jmstudios.redmoon.model

import android.content.Context
import android.content.SharedPreferences

import java.util.ArrayList
import org.json.JSONObject

import com.jmstudios.redmoon.util.Logger

/**
 * This class manages the SharedPreference that store all custom
 * filter profiles added by the user.

 * The profiles are stored in a separate SharedPreference, with per
 * profile a key given by "$PROFILE_NAME_$ID", where $PROFILE_NAME is
 * the name given to the profile by the user and $ID is the position
 * of the list of profiles, starting with 0 for the first custom
 * profile created by the user. A string is associated with every key
 * with the format "$PROGRESS_COLOR,$PROGRESS_INTENSITY,$PROGRESS_DIM"
 */
class ProfilesModel(context: Context) {

    private val mSharedPrefs = context.getSharedPreferences(preferenceName, mode)

    var profiles: ArrayList<Profile>
        private set

    init {
        Log.i("Creating ProfilesModel")
        val mPrefsContentsMap = mSharedPrefs.all
        profiles = ArrayList<Profile>()

        val amProfiles = mPrefsContentsMap.entries.size
        Log.d("Allocating $amProfiles profiles")
        profiles.ensureCapacity(amProfiles)

        for ((key, value) in mPrefsContentsMap) {
            Log.d("Parsing key: $key")
            val i = Integer.parseInt(key)
            profiles.add(i, parseProfile(value as String))
        }
    }

    fun addProfile(profile: Profile) {
        Log.i("addProfile ${profile.mName}; Current Size: ${profiles.size}")
        profiles.add(profile)
        updateSharedPreferences()
    }

    fun getProfile(index: Int): Profile {
        val p = profiles[index]
        Log.i("getProfile $index of ${profiles.size}, ${p.mName}")
        return p
    }

    fun removeProfile(index: Int) {
        Log.i("removeProfile $index")
        profiles.removeAt(index)
        updateSharedPreferences()
    }

    private fun parseProfile(entry: String): Profile {
        // Log.i("ParseProfile: $entry")
        val json = JSONObject(entry)
        val name = json.optString(KEY_NAME)
        val color = json.optInt(KEY_COLOR)
        val intensity = json.optInt(KEY_INTENSITY)
        val dim = json.optInt(KEY_DIM)
        val lowerBrightness = json.optBoolean(KEY_LOWER_BRIGHTNESS)
        return Profile(name, color, intensity, dim, lowerBrightness)
    }

    private fun updateSharedPreferences() {
        Log.i("Updating SharedPreferences")
        val editor = mSharedPrefs.edit()
        editor.clear()

        for ((i, profile) in profiles.withIndex()) {
            Log.i("Storing profile $i, ${profile.mName}")
            editor.putString(Integer.toString(i), profile.values)
        }

        editor.apply()
        Log.d("Done updating SharedPreferences")
    }

    class Profile(var mName: String,
                  var mColor:     Int,
                  var mIntensity: Int,
                  var mDim:       Int,
                  val mLowerBrightness: Boolean){

        val values: String
            get() {
                val json = JSONObject()
                json.put(KEY_NAME,      mName)
                json.put(KEY_COLOR,     mColor)
                json.put(KEY_INTENSITY, mIntensity)
                json.put(KEY_DIM,       mDim)
                json.put(KEY_LOWER_BRIGHTNESS, mLowerBrightness)
                return json.toString(2)
            }
    }

    companion object : Logger() {
        private const val preferenceName = "com.jmstudios.redmoon.PROFILES_PREFERENCE"
        private const val mode = Context.MODE_PRIVATE

        private const val KEY_NAME = "name"
        private const val KEY_COLOR = "color"
        private const val KEY_INTENSITY = "intensity"
        private const val KEY_DIM = "dim"
        private const val KEY_LOWER_BRIGHTNESS = "lower-brightness"
    }
}
