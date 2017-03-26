package com.jmstudios.redmoon.helper

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log

import com.jmstudios.redmoon.BuildConfig
import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.ProfilesModel
import com.jmstudios.redmoon.util.appContext

fun upgrade() {
    fun upgradeProfiles() {
        val PREFERENCE_NAME = "com.jmstudios.redmoon.PROFILES_PREFERENCE"
        val MODE = Context.MODE_PRIVATE
        val prefs  = appContext.getSharedPreferences(PREFERENCE_NAME, MODE)

        val profiles = prefs.all.entries.map { (key, v) ->
            val values = v as String

            val index      = Integer.parseInt(key.substringAfter('_')) + 1
            val pName      = key.substringBefore('_')
            val pColor     = Integer.parseInt(values.substringBefore(','))
            val pIntensity = Integer.parseInt(values.substringAfter(',').substringBefore(','))
            val pDim       = Integer.parseInt(values.substringAfterLast(','))
            val profile    = Profile(pName, pColor, pIntensity, pDim)

            Pair(index.toString(), profile.toString())
        }

        val editor = prefs.edit()
        editor.run {
            clear()
            putString("0", Profile("TO_BE_DELETED").toString())
            profiles.forEach { (index, profile) ->
                Log.i("UPGRADE_PROFILES", "Storing profile $index, $profile")
                putString(index, profile)
            }
        }
        editor.apply()
        Config.amountProfiles = ProfilesModel.reset()
        Config.profile = 1
    }

    fun upgradeToggleModePreferences() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        val timerKey = appContext.getString(R.string.pref_key_time_toggle)
        val currentToggleMode: String = sharedPrefs.getString(timerKey, "manual")
        sharedPrefs.edit().remove(timerKey).apply()
        Config.timeToggle = currentToggleMode != "manual"
        Config.useLocation = currentToggleMode == "sun"
    }

    tailrec fun upgradeFrom(version: Int): Unit = when (version) {
        BuildConfig.VERSION_CODE -> {
            Config.fromVersionCode = version
        } in 0..25 -> {
            upgradeToggleModePreferences()
            upgradeFrom(26)
        } in 26..27 -> {
            upgradeFrom(28)
        } 28 -> {
            upgradeProfiles()
            upgradeFrom(29)
        } else -> {
            Log.e("handleUpgrades", "Didn't catch upgrades from version $version")
            upgradeFrom(version+1)
        }
    }

    upgradeFrom(if (Config.introShown) Config.fromVersionCode else BuildConfig.VERSION_CODE)
}