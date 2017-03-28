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
package com.jmstudios.redmoon.preference

import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.preference.Preference
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.event.*
import com.jmstudios.redmoon.helper.ProfilesHelper
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.Logger

import org.greenrobot.eventbus.Subscribe

class ProfileSelectorPreference(mContext: Context, attrs: AttributeSet) : Preference(mContext, attrs), OnItemSelectedListener {

    lateinit private var mProfileSpinner: Spinner
    lateinit private var mProfileActionButton: Button
    lateinit private var mView: View

    lateinit internal var mArrayAdapter: ArrayAdapter<CharSequence>

    private var currentColor: Int = 0
    private var currentIntensity: Int = 0
    private var currentDim: Int = 0
    private var currentLowerBrightness: Boolean = false

    private var mIsListenerRegistered: Boolean = false

    init {
        layoutResource = R.layout.preference_profile_selector
        mIsListenerRegistered = false
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInteger(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            Config.profile = getPersistedInt(DEFAULT_VALUE)
        } else {
            Config.profile = (defaultValue as Int?)?: 0
        }
    }

    override fun onBindView(view: View) {
        Log.i("onBindView")
        super.onBindView(view)

        mView = view

        mProfileSpinner = view.findViewById(R.id.profile_spinner) as Spinner
        mProfileActionButton = view.findViewById(R.id.profile_action_button) as Button

        initLayout()

        updateButtonSetup()
    }

    private fun initLayout() {
        Log.i("Starting initLayout")
        mArrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item)
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        for (i in 0..Config.amountProfiles-1) {
            mArrayAdapter.add(ProfilesHelper.getProfileName(i) as CharSequence)
        }

        mProfileSpinner.adapter = mArrayAdapter
        mProfileSpinner.setSelection(Config.profile)
        mProfileSpinner.onItemSelectedListener = this
    }

    private fun updateButtonSetup() {
        if (Config.profile > ProfilesHelper.DEFAULT_OPERATIONS_AM - 1) {
            Log.i("Setting remove button")
            mProfileActionButton.text = context.resources.getString(R.string.button_remove_profile)
            mProfileActionButton.setOnClickListener { openRemoveProfileDialog() }

        } else {
            Log.i("Setting add button")
            mProfileActionButton.text = context.resources.getString(R.string.button_add_profile)
            mProfileActionButton.setOnClickListener { openAddNewProfileDialog() }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View,
                                pos: Int, id: Long) {
        Log.i("Item $pos selected")
        ProfilesHelper.setProfile(pos)
        updateButtonSetup()
    }

    override fun onNothingSelected(parent: AdapterView<*>) { }

    private fun openRemoveProfileDialog() {
        val builder = AlertDialog.Builder(context).apply {
            setTitle(context.resources.getString(R.string.remove_profile_dialog_title))

            val okString = context.resources.getString(R.string.button_remove_profile)
            val cancelString = context.resources.getString(R.string.cancel_dialog)

            setNegativeButton(cancelString) { dialog, _ -> dialog.cancel() }
            setPositiveButton(okString) { _, _ ->
                ProfilesHelper.removeProfile(Config.profile)
            }
        }
        builder.show()
    }

    private fun openAddNewProfileDialog() {
        val builder = AlertDialog.Builder(context).apply {
            setTitle(context.resources.getString(R.string.add_new_profile_dialog_title))

            val nameInput = EditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                hint = context.resources.getString(R.string.add_new_profile_edit_hint)
            }
            setView(nameInput)

            val okString = context.resources.getString(R.string.ok_dialog)
            val cancelString = context.resources.getString(R.string.cancel_dialog)

            setNegativeButton(cancelString) { dialog, _ -> dialog.cancel() }
            setPositiveButton(okString) { dialog, _ ->
                if (nameInput.text.toString().trim { it <= ' ' } != "") {
                    ProfilesHelper.addProfile(nameInput.text.toString())
                } else {
                    dialog.cancel()
                }
            }
        }
        builder.show()
    }

    //region presenter
    @Subscribe
    fun onProfileChanged(event: profileChanged) {
        Log.i("onProfileChanged")
        val pos = Config.profile
        mProfileSpinner.setSelection(pos)

        if (pos != 0) {
            val newProfile = ProfilesHelper.getProfile(pos)

            currentDim = newProfile.mDim
            currentIntensity = newProfile.mIntensity
            currentColor = newProfile.mColor
            currentLowerBrightness = newProfile.mLowerBrightness
        }
    }

    @Subscribe
    fun onAmountProfilesChanged(event: amountProfilesChanged) {
        initLayout()
    }

    @Subscribe
    fun onDimChanged(event: dimChanged) {
        Log.i("onDimChanged")
        if (Config.dim != currentDim) ProfilesHelper.setProfile(0)
    }

    @Subscribe
    fun onIntensityChanged(event: intensityChanged) {
        Log.i("onIntensityChanged")
        if (Config.intensity != currentIntensity) ProfilesHelper.setProfile(0)
    }

    @Subscribe
    fun onColorChanged(event: colorChanged) {
        Log.i("onColorChanged")
        if (Config.color != currentColor) ProfilesHelper.setProfile(0)
    }

    @Subscribe
    fun onLowerBrightnessChanged(event: lowerBrightnessChanged) {
        Log.i("onLowerBrightnessChanged")
        if (Config.lowerBrightness != currentLowerBrightness) { ProfilesHelper.setProfile(0) }
    }
    //endregion

    companion object : Logger() {
        const val DEFAULT_VALUE = 1
    }
}
