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

package com.jmstudios.redmoon.presenter

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.activity.MainActivity
import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.helper.EventBus
import com.jmstudios.redmoon.helper.Logger
import com.jmstudios.redmoon.helper.Permission
import com.jmstudios.redmoon.manager.BrightnessManager
import com.jmstudios.redmoon.manager.CurrentAppMonitor
import com.jmstudios.redmoon.manager.ScreenManager
import com.jmstudios.redmoon.manager.WindowViewManager
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.model.ProfilesModel
import com.jmstudios.redmoon.receiver.NextProfileCommandReceiver
import com.jmstudios.redmoon.receiver.OrientationChangeReceiver
import com.jmstudios.redmoon.receiver.SwitchAppWidgetProvider
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.service.ScreenFilterService.Command
import com.jmstudios.redmoon.service.ServiceLifeCycleController
import com.jmstudios.redmoon.util.*

import org.greenrobot.eventbus.Subscribe

class ScreenFilterPresenter(private val mServiceController: ServiceLifeCycleController,
                            private val mContext:           Context,
                            private val mWindowViewManager: WindowViewManager,
                            private val mScreenManager:     ScreenManager,
                            private val mCurrentAppMonitor: CurrentAppMonitor,
                            private val mBrightnessManager: BrightnessManager) :
                        OrientationChangeReceiver.OnOrientationChangeListener {

    private var mWindowLayoutParams = mScreenManager.layoutParams

    private val layoutParams
        get() = mWindowLayoutParams.apply{
            buttonBrightness = Config.buttonBacklightLevel
        }

    private val mOnState      = OnState()
    private val mOffState     = OffState()
    private val mPreviewState = PreviewState()
    private val mSuspendState = SuspendState()

    private var mCurrentState: State = mOffState

    init {
        Log.i("Initializing")
        Config.filterIsOn = mCurrentState.filterIsOn
        EventBus.register(mCurrentState)
    }

    private fun updateLayoutParams() {
        mWindowLayoutParams = mScreenManager.layoutParams
    }

    fun updateWidgets() {
        //Broadcast to keep appwidgets in sync
        Log.i("Sending update broadcast")
        val updateAppWidgetIntent = Intent(mContext, SwitchAppWidgetProvider::class.java)
        updateAppWidgetIntent.action = SwitchAppWidgetProvider.ACTION_UPDATE
        updateAppWidgetIntent.putExtra(SwitchAppWidgetProvider.EXTRA_POWER,
                                       mCurrentState.filterIsOn)
        mContext.sendBroadcast(updateAppWidgetIntent)
    }

    fun handleCommand(command: Command) {
        mCurrentState.handleCommand(command)
    }

    override fun onPortraitOrientation() {
        updateLayoutParams()
        mWindowViewManager.reLayoutWindow(layoutParams)
    }

    override fun onLandscapeOrientation() {
        updateLayoutParams()
        mWindowViewManager.reLayoutWindow(layoutParams)
    }

    // TODO: Clean up notification refresh code
    private abstract inner class State {

        abstract val filterIsOn: Boolean

        open protected val toggleIconResId  = R.drawable.ic_play
        open protected val toggleActionText = getString(R.string.action_on)
        open protected val toggleCommand    = Command.ON

        open protected val notificationContentText
            get() = ProfilesModel.getProfileName(Config.profile)

        protected val notification: NotificationCompat.Builder
            get() = NotificationCompat.Builder(mContext).apply {
                // Set notification appearance
                setSmallIcon(R.drawable.notification_icon_half_moon)
                color    = ContextCompat.getColor(mContext, R.color.color_primary)
                priority = Notification.PRIORITY_MIN

                if (belowAPI(24)) { setContentTitle(getString(R.string.app_name)) }
                setContentText(notificationContentText)

                // Open Red Moon when tapping notification body
                val mainActivityIntent = Intent(mContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                setContentIntent(PendingIntent.getActivity(mContext, REQUEST_CODE_ACTION_SETTINGS,
                                            mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT))

                // Add toggle action
                val togglePI = PendingIntent.getService(mContext, REQUEST_CODE_ACTION_TOGGLE,
                                                        ScreenFilterService.intent(toggleCommand),
                                                        PendingIntent.FLAG_UPDATE_CURRENT)
                addAction(toggleIconResId, toggleActionText, togglePI)

                // Add profile switch action
                val nextProfileText = getString(R.string.action_next_filter)
                val nextProfileIntent = Intent(mContext, NextProfileCommandReceiver::class.java)
                val nextProfilePI = PendingIntent.getBroadcast(mContext, REQUEST_CODE_NEXT_PROFILE,
                                                               nextProfileIntent, 0)
                addAction(R.drawable.ic_skip_next_white_36dp, nextProfileText, nextProfilePI)
            }

        open protected fun onActivation(prevState: State) {
            Log.i("super($this).onActivation($prevState)")
            EventBus.register(this)
            Config.filterIsOn = filterIsOn
            refreshNotification()
        }

        open internal fun nextState(command: Command): State = when (command) {
            Command.OFF -> mOffState
            Command.ON -> mOnState
            Command.SHOW_PREVIEW -> mPreviewState
            Command.START_SUSPEND -> mSuspendState
            else -> this
        }

        open internal fun handleCommand(command: Command) {
            moveToState(nextState(command))
        }

        protected fun moveToState(newState: State) {
            if (!Permission.Overlay.isGranted) {
                Log.i("No overlay permission.")
                EventBus.post(overlayPermissionDenied())
            } else if (newState !== this) {
                Log.i("Transitioning from $this to $newState")
                EventBus.unregister(this)
                mCurrentState = newState
                mCurrentState.onActivation(this)
            }
        }

        @Subscribe open fun onProfileChanged(event: profileChanged) = refreshNotification()

        @Subscribe fun onPowerStateChanged(event: filterIsOnChanged) {
            updateWidgets()
            // If an app like Tasker wants to do something each time
            // Red Moon is toggled, it can listen for this event
            val intent = Intent()
            intent.action = BROADCAST_ACTION
            intent.putExtra(BROADCAST_FIELD, filterIsOn)
            mContext.sendBroadcast(intent)
        }

        open protected fun refreshNotification() {}

        override fun toString(): String = javaClass.simpleName
    }

    private inner class OnState : State() {
        override val filterIsOn = true

        override val toggleIconResId  = R.drawable.ic_stop_circle_outline_white_36dp
        override val toggleActionText = getString(R.string.action_off)
        override val toggleCommand    = Command.OFF

        override fun onActivation(prevState: State) {
            super.onActivation(prevState)

            updateLayoutParams()
            Log.i("Active profile is $activeProfile")
            mWindowViewManager.openWindow(layoutParams, activeProfile, FADE_DURATION_LONG)

            if (Config.lowerBrightness) { mBrightnessManager.lower() }
            if (Config.secureSuspend)   { mCurrentAppMonitor.start() }
        }

        override fun nextState(command: Command): State {
            return if (command == Command.TOGGLE) mOffState else super.nextState(command)
        }

        override fun refreshNotification() {
            Log.d("Creating a persistent notification")
            mServiceController.startForeground(NOTIFICATION_ID, notification.build())
        }

        @Subscribe override fun onProfileChanged(event: profileChanged) {
            mWindowViewManager.openWindow(layoutParams, activeProfile, FADE_DURATION_SHORT)
        }

        @Subscribe fun onLowerBrightnessChanged(event: lowerBrightnessChanged) {
            if (!Permission.WriteSettings.isGranted) {
                EventBus.post(changeBrightnessDenied())
            } else {
                val lowerBrightness = Config.lowerBrightness
                Log.i("Lower brightness flag changed to: $lowerBrightness")
                if (lowerBrightness) {
                    mBrightnessManager.lower()
                } else {
                    mBrightnessManager.restore()
                }
            }
        }

        @Subscribe fun onSecureSuspendChanged(event: secureSuspendChanged) {
            if (Config.secureSuspend) {
                mCurrentAppMonitor.start()
            } else {
                mCurrentAppMonitor.stop()
            }
        }

        @Subscribe fun onButtonBacklightChanged(event: buttonBacklightChanged) {
            mWindowViewManager.reLayoutWindow(layoutParams)
        }
    }

    private inner class OffState : State() {
        override val filterIsOn = false

        override fun onActivation(prevState: State) {
            super.onActivation(prevState)

            val fadeLength = if (prevState === mPreviewState) 0 else FADE_DURATION_LONG
            mWindowViewManager.closeWindow(fadeLength)
            
            if (Config.lowerBrightness) { mBrightnessManager.restore() }
            if (Config.secureSuspend) {   mCurrentAppMonitor.stop()    }
            val ui = EventBus.getSticky(MainActivity.UI::class)
            if (ui == null) { mServiceController.stopSelf() } // ui is closed
        }

        override fun nextState(command: Command): State {
            return if (command == Command.TOGGLE) mOnState else super.nextState(command)
        }

        override fun refreshNotification() {
            Log.d("Creating notification while in $this")
            mServiceController.stopForeground(false)
            val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, notification.build())
        }

        @Subscribe(sticky = true)
        fun stopServiceWhenUICloses(ui: MainActivity.UI) {
            if (!ui.isOpen) {
                EventBus.removeSticky(ui)
                mServiceController.stopSelf()
            }
        }
    }

    /* This State is used to present the filter to the user when (s)he
     * is holding one of the seekbars to adjust the filter. It turns
     * on the filter and saves what state it should be when it will be
     * turned off.
     */
    private inner class PreviewState : State() {
        lateinit var stateToReturnTo: State
        var pressesActive: Int = 0

        override val filterIsOn: Boolean
            get() = stateToReturnTo.filterIsOn

        override fun onActivation(prevState: State) {
            stateToReturnTo = prevState
            pressesActive = 1
            super.onActivation(prevState)

            mWindowViewManager.openWindow(layoutParams, activeProfile, 0)
        }

        override fun nextState(command: Command): State = stateToReturnTo.nextState(command)

        override fun handleCommand(command: Command) {
            Log.d("Preview, got command: " + command.name)
            when (command) {
                Command.SHOW_PREVIEW -> {
                    pressesActive++
                    Log.d(String.format("%d presses active", pressesActive))
                }
                Command.HIDE_PREVIEW -> {
                    pressesActive--
                    Log.d(String.format("%d presses active", pressesActive))
                    if (pressesActive <= 0) {
                        Log.d("Moving back to state: $stateToReturnTo")
                        moveToState(stateToReturnTo)
                    }
                }
                else -> stateToReturnTo = nextState(command)
            }
        }

        @Subscribe fun onColorChanged(event: colorChanged) {
            mWindowViewManager.setColor(Config.color)
        }

        @Subscribe fun onIntensityChanged(event: intensityChanged) {
            mWindowViewManager.setIntensity(Config.intensity)
        }

        @Subscribe fun onDimChanged(event: dimChanged) {
            mWindowViewManager.setDim(Config.dimLevel)
            if (Config.buttonBacklightFlag == "dim") {
                mWindowViewManager.reLayoutWindow(layoutParams)
            }
        }
    }

    /* This state is used when the filter is suspended temporarily,
     * because the user is in an excluded app (for example the package
     * installer). It stops the filter like in the OffState, but
     * doesn't change the UI, switch or brightness state just like the
     * PreviewState. Like the PreviewState, it logs changes to the
     * state and applies them when the suspend state is deactivated.
     */
    private inner class SuspendState : State() {
        lateinit var stateToReturnTo: State
        
        override val filterIsOn: Boolean
            get() = stateToReturnTo.filterIsOn

        override val toggleIconResId  = R.drawable.ic_stop_circle_outline_white_36dp
        override val toggleActionText = getString(R.string.action_off)
        override val toggleCommand    = Command.OFF

        override val notificationContentText = getString(R.string.paused)

        override fun onActivation(prevState: State) {
            stateToReturnTo = prevState
            mWindowViewManager.closeWindow()
            super.onActivation(prevState)
        }

        override fun nextState(command: Command): State = stateToReturnTo.nextState(command)

        override fun handleCommand(command: Command) {
            Log.d("In Suspend, got command: " + command.name)
            when (command) {
                Command.STOP_SUSPEND  -> moveToState(stateToReturnTo)
                Command.START_SUSPEND -> {}
                else -> stateToReturnTo = nextState(command)
            }
        }

        override fun refreshNotification() {
            Log.d("Creating notification while in $this")
            mServiceController.stopForeground(false)
            val nm = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, notification.build())
        }
    }

    companion object : Logger() {
        const val NOTIFICATION_ID = 1
        private const val REQUEST_CODE_ACTION_SETTINGS = 1000
        private const val REQUEST_CODE_ACTION_TOGGLE   = 3000
        private const val REQUEST_CODE_NEXT_PROFILE    = 4000

        const val FADE_DURATION_LONG  = 1000
        const val FADE_DURATION_SHORT = 250

        const val BROADCAST_ACTION = "com.jmstudios.redmoon.RED_MOON_TOGGLED"
        const val BROADCAST_FIELD  = "jmstudios.bundle.key.FILTER_IS_ON"

        // Statically used by BootReceiver
    }
}
