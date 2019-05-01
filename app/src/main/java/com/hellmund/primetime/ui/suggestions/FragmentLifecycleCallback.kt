package com.hellmund.primetime.ui.suggestions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AppCompatActivity

class FragmentLifecycleCallback(
        private val activity: AppCompatActivity
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(fm.backStackEntryCount > 0)
    }

}
