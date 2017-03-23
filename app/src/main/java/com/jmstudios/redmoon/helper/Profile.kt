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
package com.jmstudios.redmoon.helper

import org.json.JSONObject

data class Profile(
        val name:            String,
        val color:           Int     = 0,
        val intensity:       Int     = 0,
        val dim:             Int     = 0,
        val lowerBrightness: Boolean = false) {

    override fun toString() = JSONObject().run {
        put(KEY_NAME,      name)
        put(KEY_COLOR,     color)
        put(KEY_INTENSITY, intensity)
        put(KEY_DIM,       dim)
        put(KEY_LOWER_BRIGHTNESS, lowerBrightness)
        toString()
    }

    internal companion object {
        private const val KEY_NAME = "name"
        private const val KEY_COLOR = "color"
        private const val KEY_INTENSITY = "intensity"
        private const val KEY_DIM = "dim"
        private const val KEY_LOWER_BRIGHTNESS = "lower-brightness"

        internal fun parse(entry: String): Profile = JSONObject(entry).run {
            val name      = optString(KEY_NAME)
            val color     = optInt(KEY_COLOR)
            val intensity = optInt(KEY_INTENSITY)
            val dim       = optInt(KEY_DIM)
            val lowerBrightness = optBoolean(KEY_LOWER_BRIGHTNESS)
            Profile(name, color, intensity, dim, lowerBrightness)
        }
    }
}

