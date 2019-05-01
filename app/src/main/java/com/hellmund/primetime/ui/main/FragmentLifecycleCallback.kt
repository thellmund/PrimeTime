package com.hellmund.primetime.ui.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity

class FragmentLifecycleCallback(
        private val activity: AppCompatActivity
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(fm.backStackEntryCount > 0)
    }

}
