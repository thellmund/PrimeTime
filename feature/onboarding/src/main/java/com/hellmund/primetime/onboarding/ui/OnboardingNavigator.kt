package com.hellmund.primetime.onboarding.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.transaction
import com.hellmund.primetime.core.OnboardingHelper
import com.hellmund.primetime.core.navigation.AddressableActivity
import com.hellmund.primetime.core.navigation.createIntent
import com.hellmund.primetime.onboarding.R
import com.hellmund.primetime.onboarding.ui.selectgenres.SelectGenresFragment
import com.hellmund.primetime.onboarding.ui.selectmovies.SelectMoviesFragment
import javax.inject.Inject

interface OnboardingNavigator {
    fun start()
    fun next()
    fun close()
}

class RealOnboardingNavigator @Inject constructor(
    private val activity: FragmentActivity,
    private val onboardingHelper: OnboardingHelper
) : OnboardingNavigator {

    private val currentFragment: Fragment?
        get() = activity.supportFragmentManager.findFragmentById(R.id.contentFrame)

    override fun start() {
        val fragment = SelectGenresFragment.newInstance()
        activity.supportFragmentManager.transaction {
            replace(R.id.contentFrame, fragment)
        }
    }

    override fun next() {
        when (currentFragment) {
            is SelectGenresFragment -> openMoviesSelection()
            is SelectMoviesFragment -> finishOnboarding()
        }
    }

    private fun openMoviesSelection() {
        val fragment = SelectMoviesFragment.newInstance()
        activity.supportFragmentManager.transaction {
            replace(R.id.contentFrame, fragment)
            addToBackStack(null)
        }
    }

    private fun finishOnboarding() {
        onboardingHelper.markFinished()

        val intent = activity.createIntent(AddressableActivity.Main)
        activity.startActivity(intent)
        activity.finish()
    }

    override fun close() {
        when (currentFragment) {
            is SelectGenresFragment -> activity.finish()
            is SelectMoviesFragment -> activity.onBackPressed()
        }
    }
}
