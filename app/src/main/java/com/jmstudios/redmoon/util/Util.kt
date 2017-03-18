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
package com.jmstudios.redmoon.util

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log

import com.jmstudios.redmoon.BuildConfig
import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.application.RedMoonApplication
import com.jmstudios.redmoon.event.locationPermissionDialogClosed
import com.jmstudios.redmoon.model.Config

import org.greenrobot.eventbus.EventBus

val appContext = RedMoonApplication.app

fun getString(resId: Int): String = appContext.getString(resId)
fun getColor(resId: Int): Int = ContextCompat.getColor(appContext, resId)

val atLeastAPI: (Int) -> Boolean = { it <= android.os.Build.VERSION.SDK_INT }
val belowAPI: (Int) -> Boolean = { !atLeastAPI(it) }

private val lp = Manifest.permission.ACCESS_FINE_LOCATION
private val granted = PackageManager.PERMISSION_GRANTED
//private val OVERLAY_PERMISSION_REQ_CODE = 1111
private val LOCATION_PERMISSION_REQ_CODE = 2222

val hasLocationPermission: Boolean
    get() = ContextCompat.checkSelfPermission(appContext, lp) == granted

val hasWriteSettingsPermission: Boolean
    get() = if (atLeastAPI(23)) Settings.System.canWrite(appContext) else true

val hasOverlayPermission: Boolean
    get() = if (atLeastAPI(23)) Settings.canDrawOverlays(appContext) else true

fun requestLocationPermission(activity: Activity): Boolean {
    if (!hasLocationPermission) {
        val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(activity, permission, LOCATION_PERMISSION_REQ_CODE)
    }
    return hasLocationPermission
}

fun requestWriteSettingsPermission(context: Context): Boolean {
    if (!hasWriteSettingsPermission) @TargetApi(23) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                            Uri.parse("package:" + context.packageName))
        val builder = AlertDialog.Builder(context)
        builder.setMessage(R.string.write_settings_dialog_message)
               .setTitle(R.string.write_settings_dialog_title)
               .setPositiveButton(R.string.ok_dialog) { _, _ ->
                   context.startActivity(intent)
               }.show()
    }
    return hasWriteSettingsPermission
}

fun requestOverlayPermission(context: Context): Boolean {
    if (!hasOverlayPermission) @TargetApi(23) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + context.packageName))
        val builder = AlertDialog.Builder(context)
        builder.setMessage(R.string.overlay_dialog_message)
               .setTitle(R.string.overlay_dialog_title)
               .setPositiveButton(R.string.ok_dialog) { _, _ ->
                   context.startActivity(intent)
               }.show()
    }
    return hasOverlayPermission
}

fun onRequestPermissionsResult(requestCode: Int) {
    if (requestCode == LOCATION_PERMISSION_REQ_CODE) {
        EventBus.getDefault().post(locationPermissionDialogClosed())
    }
}

fun handleUpgrades() {
    tailrec fun upgradeFrom(version: Int): Unit = when (version) {
        BuildConfig.VERSION_CODE -> {
            Config.fromVersionCode = version
        } in 0..25 -> {
            upgradeToggleModePreferences()
            upgradeFrom(26)
        } 26..27 -> {
            upgradeFrom(28)
        } else -> {
            Log.e("handleUpgrades", "Didn't catch upgrades from version $version")
            upgradeFrom(version+1)
        }
    }
    upgradeFrom(Config.fromVersionCode)
}

private fun upgradeToggleModePreferences() {
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)
    val timerKey = appContext.getString(R.string.pref_key_time_toggle)
    val currentToggleMode: String = sharedPrefs.getString(timerKey, "manual")
    sharedPrefs.edit().remove(timerKey).apply()
    Config.timeToggle = currentToggleMode != "manual"
    Config.useLocation = currentToggleMode == "sun"
}
