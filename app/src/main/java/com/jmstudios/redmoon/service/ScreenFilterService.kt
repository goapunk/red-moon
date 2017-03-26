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

package com.jmstudios.redmoon.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.view.WindowManager
import com.jmstudios.redmoon.manager.BrightnessManager
import com.jmstudios.redmoon.manager.CurrentAppMonitor

import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.manager.ScreenManager
import com.jmstudios.redmoon.manager.WindowViewManager
import com.jmstudios.redmoon.presenter.ScreenFilterPresenter
import com.jmstudios.redmoon.receiver.OrientationChangeReceiver
import com.jmstudios.redmoon.view.ScreenFilterView
import com.jmstudios.redmoon.util.appContext

class ScreenFilterService : Service(), ServiceLifeCycleController {
    enum class Command {
        ON, OFF, SHOW_PREVIEW, HIDE_PREVIEW, START_SUSPEND, STOP_SUSPEND, TOGGLE
    }

    private lateinit var mPresenter:           ScreenFilterPresenter
    private lateinit var mOrientationReceiver: OrientationChangeReceiver

    override fun onCreate() {
        super.onCreate()

        Log.i("onCreate")

        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = ScreenFilterView(this)

        val context = this
        val wvm     = WindowViewManager(view, windowManager)
        val sm      = ScreenManager    (this, windowManager)
        val cam     = CurrentAppMonitor(this)
        val bm      = BrightnessManager(this)

        mPresenter = ScreenFilterPresenter(context, this, wvm, sm, cam, bm)


        val orientationIntentFilter = IntentFilter()
        orientationIntentFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED)

        mOrientationReceiver = OrientationChangeReceiver(mPresenter)
        registerReceiver(mOrientationReceiver, orientationIntentFilter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(String.format("onStartCommand(%s, %d, %d", intent, flags, startId))
        val flag = intent.getIntExtra(BUNDLE_KEY_COMMAND, COMMAND_MISSING)
        Log.i("Recieved flag: $flag")
        if (flag != COMMAND_MISSING) mPresenter.handleCommand(Command.values()[flag])

        // Do not attempt to restart if the hosting process is killed by Android
        return Service.START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // Prevent binding.
        return null
    }

    override fun onDestroy() {
        Log.i("onDestroy")

        // TODO: make sure the filterView gets closed. Not a problem right now
        // but without it this is brittle and bug-prone
        unregisterReceiver(mOrientationReceiver)
        mPresenter.updateWidgets()

        super.onDestroy()
    }

    companion object : Logger() {
        private const val BUNDLE_KEY_COMMAND = "jmstudios.bundle.key.COMMAND"
        private const val COMMAND_MISSING = -1

        private val emptyIntent: Intent
            get() = Intent(appContext, ScreenFilterService::class.java)

        fun intent(c: Command): Intent = emptyIntent.putExtra(BUNDLE_KEY_COMMAND, c.ordinal)

        fun start()  { appContext.startService(emptyIntent) }
        //fun stop()   { appContext.stopService(intent)  }

        fun toggle() { moveToState(Command.TOGGLE)  }
        fun moveToState(c: Command) { appContext.startService(intent(c)) }
    }
}
