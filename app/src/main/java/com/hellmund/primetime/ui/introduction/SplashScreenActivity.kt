package com.hellmund.primetime.ui.introduction

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.utils.OnboardingHelper
import javax.inject.Inject

class SplashScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        injector.inject(this)

        if (onboardingHelper.isFirstLaunch) {
            openIntroduction()
        } else {
            openMain()
        }
    }

    private fun openIntroduction() {
        val intent = IntroductionActivity.newIntent(this)
        startActivity(intent)
        finish()
    }

    private fun openMain() {
        val intent = MainActivity.newIntent(this, intent.getStringExtra("intent"))
        startActivity(intent)
        finish()
    }

}
