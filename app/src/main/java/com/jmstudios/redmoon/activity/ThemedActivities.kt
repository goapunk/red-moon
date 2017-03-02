package com.jmstudios.redmoon.activity

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.jmstudios.redmoon.R
import com.jmstudios.redmoon.fragment.AboutFragment
import com.jmstudios.redmoon.fragment.SecureSuspendFragment
import com.jmstudios.redmoon.fragment.TimeToggleFragment
import com.jmstudios.redmoon.model.Config
import com.jmstudios.redmoon.util.Log
import com.jmstudios.redmoon.util.onRequestPermissionsResult

abstract class ThemedAppCompatActivity : AppCompatActivity() {

    abstract protected val fragment: PreferenceFragment
    abstract protected val tag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Config.activeTheme)
        setContentView(R.layout.activity_main)

        // Only create and attach a new fragment on the first Activity creation.
        if (savedInstanceState == null) {
            Log("onCreate - First creation")
            fragmentManager.beginTransaction()
                           .replace(R.id.fragment_container, fragment, tag)
                           .commit()
        }

        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        onRequestPermissionsResult(requestCode)
    }
}

// One subclass per subscreen

class SecureSuspendActivity : ThemedAppCompatActivity() {
    override val fragment = SecureSuspendFragment()
    override val tag = "jmstudios.fragment.tag.SECURE_SUSPEND"
}

class TimeToggleActivity : ThemedAppCompatActivity() {
    override val fragment = TimeToggleFragment()
    override val tag = "jmstudios.fragment.tag.TIME_TOGGLE"
}

class AboutActivity : ThemedAppCompatActivity() {
    override val fragment = AboutFragment()
    override val tag = "jmstudios.fragment.tag.ABOUT"
}
