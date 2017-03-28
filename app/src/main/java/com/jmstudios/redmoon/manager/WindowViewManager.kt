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
package com.jmstudios.redmoon.manager

import com.jmstudios.redmoon.util.Logger
import android.view.WindowManager

import com.jmstudios.redmoon.view.ScreenFilterView

/**
 * Convenience class that wraps [WindowManager] for dependency injection.
 */
class WindowViewManager(private val mWindowManager: WindowManager,
                        val mView: ScreenFilterView) {

    companion object : Logger()

    private var mScreenFilterOpen = false

    /**
     * Creates and opens a new Window to display `view`.
     * *
     * @param wlp the [android.view.WindowManager.LayoutParams] to use when laying out the window.
     */
    fun openWindow(wlp: WindowManager.LayoutParams) {
        if (mScreenFilterOpen) {
            Log.i("Screen filter is already open!")
        } else {
            Log.i("Opening screen filter")
            // Display the transparent filter
            mWindowManager.addView(mView, wlp)
            mScreenFilterOpen = true
        }
    }

    // Triggers a Window undergo a screen measurement and layout pass
    fun reLayoutWindow(wlp: WindowManager.LayoutParams) {
        if (mScreenFilterOpen) {
            mWindowManager.updateViewLayout(mView, wlp)
        }
    }

    // Closes the Window that is currently displaying `mView`.
    fun closeWindow() {
        if (mScreenFilterOpen) {
            Log.i("Closing screen filter")
            // Close the window once the fade-out animation is complete
            mWindowManager.removeView(mView)
            mScreenFilterOpen = false
        } else {
            Log.i("Can't close Screen filter; it's already closed")
        }
    }
}
