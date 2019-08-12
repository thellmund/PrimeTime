package com.hellmund.primetime.onboarding

import android.content.Context
import android.content.Intent
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
            supportFragmentManager.transaction {
                val fragment = SelectGenresFragment.newInstance { openMovieSelection() }
                replace(R.id.contentFrame, fragment)
            }
        }
    }

    private fun openMovieSelection() {
        supportFragmentManager.transaction {
            val fragment = SelectMoviesFragment.newInstance { finishOnboarding() }
            replace(R.id.contentFrame, fragment)
            addToBackStack(null)
        }
    }

    private fun finishOnboarding() {
        val intent = createIntent(AddressableActivity.Main)
        startActivity(intent)
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }

}
