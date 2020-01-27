package com.hellmund.primetime.onboarding

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
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

        binding.contentFrame.doOnApplyWindowInsets { v, insets, initialState ->
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

    override fun provideOnboardingComponent(): OnboardingComponent = onboardingComponent
}
