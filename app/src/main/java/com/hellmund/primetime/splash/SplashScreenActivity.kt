package com.hellmund.primetime.splash

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellmund.primetime.R
import com.hellmund.primetime.introduction.IntroductionActivity
import com.hellmund.primetime.main.MainActivity
import com.hellmund.primetime.utils.OnboardingHelper

class SplashScreenActivity : AppCompatActivity() {

    private val onboardingHelper: OnboardingHelper by lazy {
        OnboardingHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        if (true) { // TODO onboardingHelper.isFirstLaunch) {
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
