package com.hellmund.primetime.moviedetails.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.moviedetails.R
import com.hellmund.primetime.ui_common.MovieViewEntity

class MovieDetailsActivity : AppCompatActivity() {

    private val movie: MovieViewEntity by lazy {
        checkNotNull(intent.getParcelableExtra<MovieViewEntity>(FragmentArgs.KEY_MOVIE))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.contentFrame, MovieDetailsFragment.newInstance(movie))
            }
        }
    }
}
