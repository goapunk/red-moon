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
 *     Copyright (c) 2016 Zoraver <https://github.com/Zoraver>
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
package com.jmstudios.redmoon.manager

import android.content.Context
import android.provider.Settings
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Permission

class BrightnessManager(private val mContext: Context) {
    companion object: Logger() {
        // Used statically by BootReceiver
        fun setBrightness(brightness: Int, automatic: Boolean, context: Context) {
            Log.i("Setting brightness to: $brightness, automatic: $automatic")
            if (Permission.WriteSettings.isGranted && brightness >= 0) {
                val resolver = context.contentResolver
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                Settings.System.putInt(resolver, "screen_brightness_mode", if (automatic) 1 else 0)
            }
        }
    }

    private var oldAuto:  Boolean = false
    private var oldLevel: Int     = -1

    fun lower() {
        if (!Config.filterIsOn) {
            Log.w("Rejected attempt to lower brightness while filter is off!")
        } else {
            if (Config.lowerBrightness) {
                try {
                    val resolver = mContext.contentResolver
                    oldLevel = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
                    oldAuto = 1 == Settings.System.getInt(resolver, "screen_brightness_mode")
                } catch (e: Settings.SettingNotFoundException) {
                    Log.i("Error reading brightness state $e")
                    oldAuto = false
                }
            } else {
                oldLevel = -1
            }
            Config.automaticBrightness = oldAuto
            Config.brightness = oldLevel
            setBrightness(0, false, mContext)
        }
    }

    fun restore() {
        setBrightness(Config.brightness, Config.automaticBrightness, mContext)
    }
}
