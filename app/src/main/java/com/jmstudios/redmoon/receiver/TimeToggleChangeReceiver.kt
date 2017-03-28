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

import com.jmstudios.redmoon.helper.DismissNotificationRunnable
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.presenter.ScreenFilterPresenter
import com.jmstudios.redmoon.service.LocationUpdateService
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.util.appContext
import com.jmstudios.redmoon.util.atLeastAPI
import com.jmstudios.redmoon.util.Logger

import java.util.Calendar
import java.util.GregorianCalendar


class TimeToggleChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Alarm received")

        val turnOn = intent.data.toString() == "turnOnIntent"

        val command = if (turnOn) ScreenFilterService.Command.ON
        else ScreenFilterService.Command.OFF
        ScreenFilterService.moveToState(command)
        cancelAlarm(turnOn)
        scheduleNextCommand(turnOn)

        // We want to dismiss the notification if the filter is turned off
        // automatically.
        // However, the filter fades out and the notification is only
        // refreshed when this animation has been completed.  To make sure
        // that the new notification is removed we create a new runnable to
        // be executed 100 ms after the filter has faded out.
        val handler = Handler()

        val runnable = DismissNotificationRunnable(context)
        handler.postDelayed(runnable, (ScreenFilterPresenter.FADE_DURATION_MS + 100).toLong())

        LocationUpdateService.update(foreground = false)
    }

    companion object : Logger() {
        private val intent: Intent
            get() = Intent(appContext, TimeToggleChangeReceiver::class.java)

        private val alarmManager: AlarmManager
            get() = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Conveniences
        fun scheduleNextOnCommand() = scheduleNextCommand(true)
        fun scheduleNextOffCommand() = scheduleNextCommand(false)
        fun rescheduleOnCommand() {
            cancelAlarm(true)
            scheduleNextCommand(true)
        }
        fun rescheduleOffCommand() {
            cancelAlarm(false)
            scheduleNextCommand(false)
        }
        fun cancelAlarms() {
            cancelAlarm(true)
            cancelAlarm(false)
        }

        private fun scheduleNextCommand(turnOn: Boolean) {
            if (Config.timeToggle) {
                Log.d("Scheduling alarm to turn filter ${if (turnOn) "on" else "off"}")
                val time = if (turnOn) { Config.automaticTurnOnTime }
                           else { Config.automaticTurnOffTime }

                val command = intent.apply {
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

                Log.i("Scheduling alarm for " + calendar.toString())

                val pendingIntent = PendingIntent.getBroadcast(appContext, 0, command, 0)

                if (atLeastAPI(19)) {
                    alarmManager.setExact(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
                }
            } else {
                Log.i("Tried to schedule alarm, but timer is disabled.")
            }
        }

        private fun cancelAlarm(turnOn: Boolean) {
            Log.d("Canceling alarm to turn filter ${if (turnOn) "on" else "off"}")
            val command = intent.apply {
                data = Uri.parse(if (turnOn) "turnOnIntent" else "offIntent")
            }
            val pendingIntent = PendingIntent.getBroadcast(appContext, 0, command, 0)
            alarmManager.cancel(pendingIntent)
        }
    }
}
