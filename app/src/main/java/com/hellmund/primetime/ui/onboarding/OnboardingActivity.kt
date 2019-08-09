package com.hellmund.primetime.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.transaction
import com.hellmund.primetime.R
import com.hellmund.primetime.ui.MainActivity

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
            addToBackStack("SelectGenresFragment")
        }
    }

    private fun finishOnboarding() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, OnboardingActivity::class.java)
    }

}
