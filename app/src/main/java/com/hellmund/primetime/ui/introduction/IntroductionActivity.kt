package com.hellmund.primetime.ui.introduction

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectgenres.SelectGenresActivity
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.isLandscapeMode
import com.hellmund.primetime.utils.observe
import kotlinx.android.synthetic.main.activity_introduction.*
import javax.inject.Inject
import javax.inject.Provider

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
        introductionButton.setOnClickListener { openGenresSelection() }
        viewModel.posterUrls.observe(this, this::displayResults)
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

    companion object {
        fun newIntent(context: Context) = Intent(context, IntroductionActivity::class.java)
    }

}
