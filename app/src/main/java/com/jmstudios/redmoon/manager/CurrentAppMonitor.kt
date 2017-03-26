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
 */
package com.jmstudios.redmoon.manager

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import com.jmstudios.redmoon.receiver.ScreenStateReceiver
import com.jmstudios.redmoon.thread.CurrentAppMonitoringThread
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.util.atLeastAPI

class CurrentAppMonitor(private val mContext: Context) : ScreenStateReceiver.ScreenStateListener {
    companion object : Logger()
    private val screenStateReceiver = ScreenStateReceiver(this)
    private var mCamThread: CurrentAppMonitoringThread? = null
    private var screenOff: Boolean = false

    override fun onScreenTurnedOn() {
        Log.i("Screen turn on received")
        screenOff = false
        startCamThread()
    }

    override fun onScreenTurnedOff() {
        Log.i("Screen turn off received")
        screenOff = true
        stopCamThread()
    }

    fun start() {
        Log.i("Starting app monitoring")
        val powerManager = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        screenOff = if (atLeastAPI(20)) { !powerManager.isInteractive }
        else @Suppress("DEPRECATION") { !powerManager.isScreenOn }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        mContext.registerReceiver(screenStateReceiver, filter)
        startCamThread()
    }

    fun stop() {
        Log.i("Stopping app monitoring")
        stopCamThread()
        try {
            mContext.unregisterReceiver(screenStateReceiver)
        } catch (e: IllegalArgumentException) {
            // Catch errors when receiver is unregistered more than
            // once, it is not a problem, so we just ignore it.
        }
    }

    private fun startCamThread() {
        if (mCamThread == null && !screenOff) {
            mCamThread = CurrentAppMonitoringThread(mContext)
            mCamThread!!.start()
        }
    }

    private fun stopCamThread() {
        if (mCamThread != null) {
            if (!mCamThread!!.isInterrupted) { mCamThread!!.interrupt() }
            mCamThread = null
        }
    }
}
