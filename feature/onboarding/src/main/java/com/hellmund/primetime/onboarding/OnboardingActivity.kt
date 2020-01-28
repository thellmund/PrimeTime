package com.hellmund.primetime.onboarding

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.transaction
import com.hellmund.primetime.core.AddressableActivity
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.core.createIntent
import com.hellmund.primetime.onboarding.databinding.ActivityOnboardingBinding
import com.hellmund.primetime.onboarding.selectgenres.di.DaggerOnboardingComponent
import com.hellmund.primetime.onboarding.selectgenres.di.OnboardingComponent
import com.hellmund.primetime.onboarding.selectgenres.di.OnboardingComponentProvider
import com.hellmund.primetime.onboarding.selectgenres.ui.SelectGenresFragment
import com.hellmund.primetime.onboarding.selectmovies.ui.SelectMoviesFragment
import com.hellmund.primetime.ui_common.util.requestFullscreenLayout
import dev.chrisbanes.insetter.doOnApplyWindowInsets

class OnboardingActivity : AppCompatActivity(), OnboardingComponentProvider {

    private val fragmentLifecycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            val backButtonResId = when (f) {
                is SelectGenresFragment -> R.drawable.ic_close
                is SelectMoviesFragment -> R.drawable.ic_arrow_back
                else -> throw IllegalStateException()
            }
            binding.closeButton.setImageResource(backButtonResId)

            val callback = when (f) {
                is SelectGenresFragment -> this@OnboardingActivity::finish
                is SelectMoviesFragment ->  this@OnboardingActivity::onBackPressed
                else -> throw IllegalStateException()
            }
            binding.closeButton.setOnClickListener { callback.invoke() }
        }
    }

    private val onboardingComponent: OnboardingComponent by lazy {
        DaggerOnboardingComponent.builder()
            .core(coreComponent)
            .build()
    }

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.requestFullscreenLayout()
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallback, false)

        binding.root.doOnApplyWindowInsets { v, insets, initialState ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = initialState.margins.top + insets.systemWindowInsetTop
            }
        }

        if (savedInstanceState == null) {
            val fragment = SelectGenresFragment.newInstance(onFinished = this::openMovieSelection)
            supportFragmentManager.transaction {
                replace(R.id.contentFrame, fragment)
            }
        }
    }

    private fun openMovieSelection() {
        val fragment = SelectMoviesFragment.newInstance(onFinished = this::finishOnboarding)
        supportFragmentManager.transaction {
            replace(R.id.contentFrame, fragment)
            addToBackStack(null)
        }
    }

    private fun finishOnboarding() {
        val intent = createIntent(AddressableActivity.Main)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallback)
        super.onDestroy()
    }

    override fun provideOnboardingComponent(): OnboardingComponent = onboardingComponent
}
