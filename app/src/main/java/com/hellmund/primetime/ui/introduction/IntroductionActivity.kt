package com.hellmund.primetime.ui.introduction

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectgenres.SelectGenresActivity
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.observe
import kotlinx.android.synthetic.main.activity_introduction.gridView
import kotlinx.android.synthetic.main.activity_introduction.introductionButton
import java.util.Timer
import javax.inject.Inject
import javax.inject.Provider
import kotlin.concurrent.schedule


class IntroductionActivity : AppCompatActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<IntroductionViewModel>

    private val viewModel: IntroductionViewModel by lazyViewModel { viewModelProvider }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)
        injector.inject(this)

        startButtonAnimation()

        introductionButton.setOnClickListener { openGenresSelection() }
        viewModel.posterUrls.observe(this, this::displayResults)
    }

    // TODO: Find an easier way to do this
    private fun startButtonAnimation() {
        Timer().schedule(500) { hideButton() }
        Timer().schedule(1_800) { slideInButton() }
    }

    private fun hideButton() {
        val buttonHeight = introductionButton.height
        val buttonMargin = resources.getDimensionPixelOffset(R.dimen.default_space)
        val verticalButtonTranslation = (buttonHeight + buttonMargin).toFloat()

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            introductionButton.animate()
                    .setDuration(400L)
                    .translationYBy(verticalButtonTranslation)
                    .setInterpolator(OvershootInterpolator(1f))
                    .start()
        }
    }

    private fun slideInButton() {
        val buttonHeight = introductionButton.height
        val buttonMargin = resources.getDimensionPixelOffset(R.dimen.default_space)
        val verticalButtonTranslation = (buttonHeight + buttonMargin).toFloat()

        val handler = Handler(Looper.getMainLooper())
        handler.post {
            introductionButton.animate()
                    .alphaBy(1f)
                    .translationYBy(verticalButtonTranslation * (-1))
                    .setInterpolator(OvershootInterpolator(1f))
                    .start()
        }
    }

    private fun displayResults(results: List<String>) {
        val adapter = PostersAdapter(imageLoader, results)
        gridView.itemAnimator = DefaultItemAnimator()
        gridView.adapter = adapter
        gridView.isEnabled = false

        val columns = if (isLandscapeMode) 4 else 3
        gridView.layoutManager = GridLayoutManager(this, columns)
    }

    private fun openGenresSelection() {
        val intent = SelectGenresActivity.newIntent(this)
        startActivity(intent)
    }

    private val Context.isLandscapeMode: Boolean
        get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

}
