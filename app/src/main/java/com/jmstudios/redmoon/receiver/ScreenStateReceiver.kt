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
package com.jmstudios.redmoon.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.jmstudios.redmoon.util.Logger

class ScreenStateReceiver(private val mListener: ScreenStateReceiver.ScreenStateListener?) : BroadcastReceiver() {

    companion object : Logger()

    interface ScreenStateListener {
        fun onScreenTurnedOn()
        fun onScreenTurnedOff()
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Intent received")

        if (Intent.ACTION_SCREEN_ON == intent.action) {
            Log.i("Screen turned on")

            mListener?.onScreenTurnedOn()
        } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
            Log.i("Screen turned off")

            mListener?.onScreenTurnedOff()
        }
    }
}
