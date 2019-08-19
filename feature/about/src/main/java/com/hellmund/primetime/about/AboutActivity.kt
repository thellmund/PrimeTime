package com.hellmund.primetime.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_about.headerTextView
import kotlinx.android.synthetic.main.view_toolbar.toolbar

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

}
