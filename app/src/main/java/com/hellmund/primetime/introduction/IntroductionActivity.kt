package com.hellmund.primetime.introduction

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.hellmund.primetime.R
import com.hellmund.primetime.selectgenres.SelectGenreActivity
import com.hellmund.primetime.utils.isLandscapeMode
import com.hellmund.primetime.utils.observe
import kotlinx.android.synthetic.main.activity_introduction.*

class IntroductionActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_introduction)

        introductionButton.setOnClickListener { openGenresSelection() }

        val viewModel = ViewModelProviders.of(this).get(IntroductionViewModel::class.java)
        viewModel.posterUrls.observe(this, this::displayResults)
    }

    private fun displayResults(results: List<String>) {
        val adapter = PostersAdapter(Glide.with(this), results)
        gridView.itemAnimator = DefaultItemAnimator()
        gridView.adapter = adapter
        gridView.isEnabled = false

        val columns = if (isLandscapeMode) 4 else 3
        gridView.layoutManager = GridLayoutManager(this, columns)
    }

    private fun openGenresSelection() {
        val intent = Intent(this, SelectGenreActivity::class.java)
        startActivity(intent)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, IntroductionActivity::class.java)
    }

}
