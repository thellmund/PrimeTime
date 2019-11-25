package com.hellmund.primetime.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.transaction
import com.hellmund.primetime.core.AddressableActivity
import com.hellmund.primetime.core.createIntent
import com.hellmund.primetime.onboarding.selectgenres.ui.SelectGenresFragment
import com.hellmund.primetime.onboarding.selectmovies.ui.SelectMoviesFragment

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

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

}