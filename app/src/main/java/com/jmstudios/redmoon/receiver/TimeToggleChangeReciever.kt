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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log

import com.jmstudios.redmoon.helper.DismissNotificationRunnable
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.presenter.ScreenFilterPresenter
import com.jmstudios.redmoon.service.LocationUpdateService
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.util.atLeastAPI
import com.jmstudios.redmoon.util.Log

import java.util.Calendar
import java.util.GregorianCalendar


class TimeToggleChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log("Alarm received")

        val turnOn = intent.data.toString() == "turnOnIntent"

        val command = if (turnOn) ScreenFilterService.Command.ON
        else ScreenFilterService.Command.OFF
        ScreenFilterService.moveToState(command)
        cancelAlarm(context, turnOn)
        scheduleNextCommand(context, turnOn)

        // We want to dismiss the notification if the filter is turned off
        // automatically.
        // However, the filter fades out and the notification is only
        // refreshed when this animation has been completed.  To make sure
        // that the new notification is removed we create a new runnable to
        // be executed 100 ms after the filter has faded out.
        val handler = Handler()

        val runnable = DismissNotificationRunnable(context)
        handler.postDelayed(runnable, (ScreenFilterPresenter.FADE_DURATION_MS + 100).toLong())

        if (Config.timeToggle && Config.useLocation) {
            LocationUpdateService.start()
        }
    }

    companion object {
        private const val TAG = "TimeToggleChange"
        private const val DEBUG = true
        private val intent = { ctx: Context -> Intent(ctx, TimeToggleChangeReceiver::class.java) }

        // Conveniences
        val scheduleNextOnCommand = { context: Context -> scheduleNextCommand(context, true) }
        val scheduleNextOffCommand = { context: Context -> scheduleNextCommand(context, false) }
        //val cancelTurnOnAlarm = { context: Context -> cancelAlarm(context, true) }
        //val cancelOffAlarm = { context: Context -> cancelAlarm(context, false) }
        val rescheduleOnCommand = { context: Context ->
            cancelAlarm(context, true)
            scheduleNextCommand(context, true)
        }
        val rescheduleOffCommand = { context: Context ->
            cancelAlarm(context, false)
            scheduleNextCommand(context, false)
        }
        val cancelAlarms = { context: Context ->
            cancelAlarm(context, true)
            cancelAlarm(context, false)
        }

        private fun scheduleNextCommand(context: Context, turnOn: Boolean) {
            if (Config.timeToggle) {
                if (DEBUG) {
                    val state = if (turnOn) "on" else "off"
                    Log.d(TAG, "Scheduling alarm to turn filter $state")
                }
                val time = if (turnOn) { Config.automaticTurnOnTime }
                           else { Config.automaticTurnOffTime }

                val command = intent(context).apply {
                    data = Uri.parse(if (turnOn) "turnOnIntent" else "offIntent")
                    putExtra("turn_on", turnOn)
                }

                val calendar = GregorianCalendar().apply {
                    set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[0]))
                    set(Calendar.MINUTE, Integer.parseInt(time.split(":".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[1]))
                }

                val now = GregorianCalendar()
                now.add(Calendar.SECOND, 1)
                if (calendar.before(now)) { calendar.add(Calendar.DATE, 1) }

                Log("Scheduling alarm for " + calendar.toString())

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pendingIntent = PendingIntent.getBroadcast(context, 0, command, 0)

                if (atLeastAPI(19)) {
                    alarmManager.setExact(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
                }
            } else {
                Log("Tried to schedule alarm, but timer is disabled.")
            }
        }

        private fun cancelAlarm(context: Context, turnOn: Boolean) {
            if (DEBUG) {
                val state = if (turnOn) "on" else "off"
                Log.d(TAG, "Canceling alarm to turn filter " + state)
            }
            val command = intent(context)
            command.data = if (turnOn) Uri.parse("turnOnIntent")
                            else Uri.parse("offIntent")
            val pendingIntent = PendingIntent.getBroadcast(context, 0, command, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}
