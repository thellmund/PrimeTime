package com.hellmund.primetime.ui.recommendations

import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellmund.primetime.R
import com.hellmund.primetime.ui.shared.BottomNavigationBehavior
import com.hellmund.primetime.ui.shared.ScrollAwareFragment

class FragmentLifecycleCallback(
    private val activity: AppCompatActivity
) : FragmentManager.FragmentLifecycleCallbacks() {

    private val contentFrame: FrameLayout by lazy {
        activity.findViewById<FrameLayout>(R.id.contentFrame)
    }

    private val bottomNavigation: BottomNavigationView by lazy {
        activity.findViewById<BottomNavigationView>(R.id.bottomNavigation)
    }

    private val bottomNavHeight: Int by lazy {
        activity.resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        if (f is BottomSheetDialogFragment) {
            return
        }

        if (f.javaClass.isAnnotationPresent(ScrollAwareFragment::class.java)) {
            scrollAwareNavigation()
        } else {
            fixedNavigation()
        }
    }

    private fun scrollAwareNavigation() {
        val layoutParams = bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = BottomNavigationBehavior.with(contentFrame, bottomNavHeight)
        bottomNavigation.requestLayout()
    }

    private fun fixedNavigation() {
        val layoutParams = bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = null
        bottomNavigation.translationY = 0f
        bottomNavigation.requestLayout()
    }

}
