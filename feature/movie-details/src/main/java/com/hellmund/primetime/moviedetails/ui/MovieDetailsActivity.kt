package com.hellmund.primetime.moviedetails.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.moviedetails.R
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.util.applyMaterialTransform
import com.hellmund.primetime.ui_common.util.requestFullscreenLayout

class MovieDetailsActivity : AppCompatActivity() {

    private val movie: MovieViewEntity.Partial by lazy {
        checkNotNull(intent.getParcelableExtra<MovieViewEntity.Partial>(FragmentArgs.KEY_MOVIE))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyMaterialTransform(movie.id.toString())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_details)
        requestFullscreenLayout()

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.contentFrame, MovieDetailsFragment.newInstance())
            }
        }
    }
}
