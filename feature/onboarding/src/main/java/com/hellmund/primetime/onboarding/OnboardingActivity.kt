package com.hellmund.primetime.onboarding

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.hellmund.primetime.core.di.coreComponent
import com.hellmund.primetime.onboarding.databinding.ActivityOnboardingBinding
import com.hellmund.primetime.onboarding.di.DaggerOnboardingComponent
import com.hellmund.primetime.onboarding.di.OnboardingComponent
import com.hellmund.primetime.onboarding.di.OnboardingComponentProvider
import com.hellmund.primetime.onboarding.ui.OnboardingNavigator
import com.hellmund.primetime.ui_common.util.requestFullscreenLayout
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import javax.inject.Inject

class OnboardingActivity : AppCompatActivity(), OnboardingComponentProvider {

    private val fragmentLifecycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?
        ) {
            val backButtonProvider = f as BackButtonIconProvider
            val backButtonResource = backButtonProvider.provideIconResource()
            binding.closeButton.setImageResource(backButtonResource)
        }
    }

    private val onboardingComponent: OnboardingComponent by lazy {
        DaggerOnboardingComponent.builder()
            .core(coreComponent)
            .activity(this)
            .build()
    }

    private val binding: ActivityOnboardingBinding by lazy {
        ActivityOnboardingBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var onboardingNavigator: OnboardingNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        requestFullscreenLayout()

        onboardingComponent.inject(this)
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallback, false)

        binding.root.doOnApplyWindowInsets { v, insets, initialState ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = initialState.margins.top + insets.systemWindowInsetTop
            }
        }

        binding.closeButton.setOnClickListener {
            onboardingNavigator.close()
        }

        if (savedInstanceState == null) {
            onboardingNavigator.start()
        }
    }

    override fun onDestroy() {
        supportFragmentManager.unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallback)
        super.onDestroy()
    }

    override fun provideOnboardingComponent(): OnboardingComponent = onboardingComponent

    interface BackButtonIconProvider {
        @DrawableRes
        fun provideIconResource(): Int
    }
}
