/*
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

package com.jmstudios.redmoon.event

/* EventBus uses classes as events. These are the available events to send */

abstract class Event

class filterIsOnChanged        : Event()
class dimChanged               : Event()
class intensityChanged         : Event()
class colorChanged             : Event()
//class themeChanged             : Event()
class customTurnOnTimeChanged  : Event()
class customTurnOffTimeChanged : Event()
class lowerBrightnessChanged   : Event()
class profileChanged           : Event()
class timeToggleChanged        : Event()
class useLocationChanged       : Event()
class locationChanged          : Event()
class sunsetTimeChanged        : Event()
class sunriseTimeChanged       : Event()
class secureSuspendChanged     : Event()
class buttonBacklightChanged   : Event()

class overlayPermissionDenied  : Event()
class locationServicesDisabled : Event()
class locationAccessDenied     : Event()
class locationUpdating         : Event()
class changeBrightnessDenied   : Event()

class locationPermissionDialogClosed : Event()
class uiClosed : Event()
