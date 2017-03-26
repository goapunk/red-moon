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

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.WindowManager

import com.jmstudios.redmoon.helper.AbstractAnimatorListener
import com.jmstudios.redmoon.helper.Profile
import com.jmstudios.redmoon.view.ScreenFilterView
import com.jmstudios.redmoon.helper.Logger

class WindowViewManager(private val mView: ScreenFilterView,
                        private val mWindowManager: WindowManager) {
    companion object : Logger()

    private var mScreenFilterOpen = false

    /**
     * Creates and opens a new Window to display `view`.
     * *
     * @param wlp the [android.view.WindowManager.LayoutParams] to use when laying out the window.
     */
    fun openWindow(wlp: WindowManager.LayoutParams, profile: Profile, time: Int = 0) {
        fun open() {
            mWindowManager.addView(mView, wlp)
            mScreenFilterOpen = true
        }
        if (mScreenFilterOpen) {
            reLayoutWindow(wlp)
            animateIntensity(profile.intensity, time)
            animateDimLevel (profile.dimLevel,  time)
            animateColor    (profile.color,     time)
        } else if (time == 0) {
            Log.i("Opening screen filter instantly")
            mView.profile = profile
            open()
        } else {
            Log.i("Opening screen filter to $profile, animating over $time")
            // Display the transparent filter
            animateIntensity(profile.intensity, time, 0)
            animateDimLevel (profile.dimLevel,  time, 0)
            open()
        }
    }

    // Triggers a Window undergo a screen measurement and layout pass
    fun reLayoutWindow(wlp: WindowManager.LayoutParams) {
        if (mScreenFilterOpen) {
            mWindowManager.updateViewLayout(mView, wlp)
        }
    }

    // Closes the Window that is currently displaying `mView`.
    fun closeWindow(time: Int = 0) {
        fun close() {
            Log.i("Closing screen filter instantly")
            mWindowManager.removeView(mView)
            mScreenFilterOpen = false
        }

        if (!mScreenFilterOpen) {
            Log.w("Can't close Screen filter; it's already closed")
        } else if (time == 0) {
            close()
        } else {
            Log.i("Closing screen filter; animating out over $time")
            val listener = object: AbstractAnimatorListener() {
                override fun onAnimationEnd(animator: Animator) { close() }
            }
            animateIntensity(0, time, mView.filterIntensityLevel)
            animateDimLevel (0, time, mView.filterDimLevel, listener)
        }
    }

    fun setColor(color: Int) {
        mColorAnimator.cancelRunning()
        mView.color = color
    }

    fun setIntensity(intensity: Int) {
        mIntensityAnimator.cancelRunning()
        mView.filterIntensityLevel = intensity
    }

    fun setDim(dim: Int) {
        mDimAnimator.cancelRunning()
        mView.filterDimLevel = dim
    }

    private var mIntensityAnimator: ValueAnimator = intAnimator(0, 0, 0, null)
    private var mDimAnimator:       ValueAnimator = intAnimator(0, 0, 0, null)
    private var mColorAnimator:     ValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), 0, 0)

    private fun intAnimator(to: Int, from: Int, over: Int,
                            listener: Animator.AnimatorListener?): ValueAnimator {
        return ValueAnimator.ofInt(from, to).apply {
            duration = over.toLong()
            if (listener != null) { addListener(listener) }
        }
    }

    fun ValueAnimator.cancelRunning() {
        if (isRunning) cancel()
    }

    private fun animateColor(color: Int, time: Int, startColor: Int = mView.color) {
        mColorAnimator.cancelRunning()

        mColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, color).apply {
            duration = time.toLong()
            addUpdateListener { valueAnimator ->
                mView.color = valueAnimator.animatedValue as Int
            }
        }
        mColorAnimator.start()
    }

    private fun animateDimLevel(dimLevel: Int, time: Int, startLevel: Int = mView.filterDimLevel,
                                listener: Animator.AnimatorListener? = null) {
        mDimAnimator.cancelRunning()

        mDimAnimator = intAnimator(dimLevel, startLevel, time, listener).apply {
            addUpdateListener { valueAnimator ->
                mView.filterDimLevel = valueAnimator.animatedValue as Int
            }
        }

        mDimAnimator.start()
    }

    private fun animateIntensity(intensity: Int, time: Int,
                                 startLevel: Int = mView.filterIntensityLevel,
                                 listener: Animator.AnimatorListener? = null) {
        mIntensityAnimator.cancelRunning()

        mIntensityAnimator = intAnimator(intensity, startLevel, time, listener).apply {
            addUpdateListener { valueAnimator ->
                mView.filterIntensityLevel = valueAnimator.animatedValue as Int
            }
        }
        mIntensityAnimator.start()
    }
}
