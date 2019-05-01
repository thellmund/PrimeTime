package com.hellmund.primetime.ui.about

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.isVisible
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.view_toolbar.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initToolbar()
        setVersionNumber()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setVersionNumber() {
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            val versionText = getString(R.string.version, versionName)
            headerTextView.text = versionText
        } catch (e: PackageManager.NameNotFoundException) {
            headerTextView.isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
