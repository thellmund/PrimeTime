package com.hellmund.primetime.ui.suggestions

import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hellmund.primetime.R
import com.hellmund.primetime.ui.search.SearchFragment
import com.hellmund.primetime.ui.shared.BottomNavigationBehavior
import com.hellmund.primetime.ui.shared.ScrollAwareFragment

class FragmentLifecycleCallback(
    private val activity: AppCompatActivity
) : FragmentManager.FragmentLifecycleCallbacks() {

    private val searchToolbarColor: Int by lazy {
        ContextCompat.getColor(activity, R.color.adapterBackground)
    }

    private val defaultToolbarColor: Int by lazy {
        ContextCompat.getColor(activity, R.color.toolbar)
    }

    private val toolbar: Toolbar by lazy {
        activity.findViewById<Toolbar>(R.id.toolbar)
    }

    private val appBarLayout: AppBarLayout by lazy {
        activity.findViewById<AppBarLayout>(R.id.appBarLayout)
    }

    private val contentFrame: FrameLayout by lazy {
        activity.findViewById<FrameLayout>(R.id.contentFrame)
    }

    private val bottomNavigation: BottomNavigationView by lazy {
        activity.findViewById<BottomNavigationView>(R.id.bottomNavigation)
    }

    private val fab: FloatingActionButton? by lazy {
        activity.findViewById<FloatingActionButton?>(R.id.filterFab)
    }

    private val fabMargin: Int by lazy {
        activity.resources.getDimensionPixelSize(R.dimen.default_space)
    }

    private val bottomNavHeight: Int by lazy {
        activity.resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        super.onFragmentResumed(fm, f)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(fm.backStackEntryCount > 1)

        val isBottomSheet = f is BottomSheetDialogFragment

        if (isBottomSheet.not()) {
            if (f.javaClass.isAnnotationPresent(ScrollAwareFragment::class.java)) {
                scrollAwareNavigation()
            } else {
                fixedNavigation()
            }
        }

        if (f is SearchFragment) {
            toolbar.setBackgroundColor(searchToolbarColor)
            appBarLayout.elevation = 0f
        } else {
            toolbar.setBackgroundColor(defaultToolbarColor)
            appBarLayout.elevation = 11f
            activity.supportActionBar?.show()
        }
    }

    private fun scrollAwareNavigation() {
        val contentLayoutParams = contentFrame.layoutParams as CoordinatorLayout.LayoutParams
        contentLayoutParams.updateMargins(bottom = 0)
        contentFrame.requestLayout()

        val navigationLayoutParams = bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        navigationLayoutParams.behavior = BottomNavigationBehavior()
        bottomNavigation.requestLayout()

        fab?.let {
            val fabLayoutParams = it.layoutParams as CoordinatorLayout.LayoutParams
            fabLayoutParams.updateMargins(bottom = fabMargin + bottomNavHeight)
            it.requestLayout()
        }
    }

    private fun fixedNavigation() {
        val contentLayoutParams = contentFrame.layoutParams as CoordinatorLayout.LayoutParams
        contentLayoutParams.updateMargins(bottom = bottomNavHeight)
        contentFrame.requestLayout()

        val navigationLayoutParams = bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        navigationLayoutParams.behavior = null
        bottomNavigation.translationY = 0f
        bottomNavigation.requestLayout()

        fab?.let {
            val fabLayoutParams = it.layoutParams as CoordinatorLayout.LayoutParams
            fabLayoutParams.updateMargins(bottom = fabMargin + bottomNavHeight)
            it.requestLayout()
        }
    }

    private fun CoordinatorLayout.LayoutParams.updateMargins(
        left: Int = leftMargin,
        top: Int = topMargin,
        right: Int = rightMargin,
        bottom: Int = bottomMargin
    ) {
        setMargins(left, top, right, bottom)
    }

}
