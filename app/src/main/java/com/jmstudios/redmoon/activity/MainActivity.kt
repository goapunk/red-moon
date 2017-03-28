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
package com.jmstudios.redmoon.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Switch

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.fragment.FilterFragment
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.service.ScreenFilterService
import com.jmstudios.redmoon.util.Logger
import com.jmstudios.redmoon.util.handleUpgrades
import com.jmstudios.redmoon.util.requestOverlayPermission

import de.cketti.library.changelog.ChangeLog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MainActivity : ThemedAppCompatActivity() {

    companion object : Logger() {
        const val EXTRA_FROM_SHORTCUT_BOOL = "com.jmstudios.redmoon.activity.MainActivity.EXTRA_FROM_SHORTCUT_BOOL"
    }

    override val fragment = FilterFragment()
    override val tag = "jmstudios.fragment.tag.FILTER"

    lateinit private var mSwitch : Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        handleUpgrades()
        val intent = intent
        Log.i("Got intent")
        val fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false)
        if (fromShortcut) { toggleAndFinish() }

        super.onCreate(savedInstanceState)
        if (!Config.introShown) { startIntro() }
        ChangeLog(this).run { if (isFirstRun) logDialog.show() }

        EventBus.getDefault().postSticky(mainUI(isOpen = true))
        // The preview will appear faster if we don't have to start the service
        ScreenFilterService.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)

        menu.findItem(R.id.menu_dark_theme).isChecked = Config.darkThemeFlag

        mSwitch = (menu.findItem(R.id.screen_filter_switch).actionView as Switch).apply {
            isChecked = Config.filterIsOn
            setOnClickListener {
                val state = if (mSwitch.isChecked) { ScreenFilterService.Command.ON }
                            else { ScreenFilterService.Command.OFF }
                Log.i("Toggling $state via switch")
                ScreenFilterService.moveToState(state)
            }
        }

        return true
    }

    override fun onResume() {
        super.onResume()
        // The switch is null here, so we can't set its position directly.
        invalidateOptionsMenu()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(mainUI(isOpen = false))
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        val fromShortcut = intent.getBooleanExtra(EXTRA_FROM_SHORTCUT_BOOL, false)
        if (fromShortcut) { toggleAndFinish() }
        Log.i("onNewIntent")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.show_intro_button -> {
                startIntro()
            }
            R.id.about_button -> {
                val aboutIntent = Intent(this, AboutActivity::class.java)
                startActivity(aboutIntent)
            }
            R.id.menu_dark_theme -> {
                Config.darkThemeFlag = !Config.darkThemeFlag
                recreate()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun startIntro() {
        val introIntent = Intent(this, Intro::class.java)
        startActivity(introIntent)
        Config.introShown = true
    }

    private fun toggleAndFinish() {
        ScreenFilterService.toggle()
        finish()
    }

    @Subscribe
    fun onFilterIsOnChanged(event: filterIsOnChanged) {
        mSwitch.isChecked = Config.filterIsOn
    }

    @Subscribe
    fun onOverlayPermissionDenied(event: overlayPermissionDenied) {
        mSwitch.isChecked = false
        requestOverlayPermission(this)
    }
}
