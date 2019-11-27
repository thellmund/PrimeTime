package com.hellmund.primetime.about

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.hellmund.primetime.about.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        setVersionNumber()
    }

    private fun initToolbar() {
        val toolbar = binding.toolbarContainer.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setVersionNumber() {
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            val versionText = getString(R.string.version, versionName)
            binding.headerTextView.text = versionText
        } catch (e: PackageManager.NameNotFoundException) {
            binding.headerTextView.isVisible = false
        }
    }
}
