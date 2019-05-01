package com.hellmund.primetime.ui.selectmovies

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.search.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.activity_select_movies.*
import kotlinx.android.synthetic.main.view_samples_error.*
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

class SelectMoviesActivity : AppCompatActivity() {

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this).apply {
            setMessage(getString(R.string.downloading_samples))
            setCancelable(false)
        }
    }

    private val adapter: SamplesAdapter by lazy {
        SamplesAdapter(viewModel::onItemClick)
    }

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var viewModelProvider: Provider<SelectMoviesViewModel>

    private val viewModel: SelectMoviesViewModel by lazyViewModel { viewModelProvider }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_movies)
        injector.inject(this)

        setupRecyclerView()
        updateNextButton()

        button.setOnClickListener { saveMovies() }
        error_button.setOnClickListener { viewModel.refresh() }

        viewModel.viewState.observe(this, this::render)
    }

    private fun setupRecyclerView() {
        gridView.adapter = adapter
        gridView.itemAnimator = DefaultItemAnimator()

        gridView.layoutManager = GridLayoutManager(this, 3)

        val spacing = Math.round(resources.getDimension(R.dimen.default_space))
        gridView.addItemDecoration(EqualSpacingGridItemDecoration(spacing, 3))
    }

    private fun render(viewState: SelectMoviesViewState) {
        progressDialog.isVisible = viewState.isLoading
        adapter.update(viewState.data)

        val selected = viewState.data.filter { it.selected }
        updateNextButton(selected.size)

        gridView.isVisible = viewState.isError.not()
        error_container.isVisible = viewState.isError
        button.isVisible = viewState.isError.not()
    }

    private fun updateNextButton(count: Int = 0) {
        val remaining = MIN_COUNT - count
        val hasSelectedEnough = remaining <= 0

        button.isClickable = hasSelectedEnough
        button.isEnabled = hasSelectedEnough

        if (hasSelectedEnough) {
            button.setText(R.string.finish)
        } else {
            button.text = getString(R.string.select_more_format_string, remaining)
        }
    }

    private fun saveMovies() {
        if (!button.isEnabled) {
            this.showToast(getString(R.string.select_at_least, MIN_COUNT))
        }

        if (this.isConnected) {
            saveSelection()
            markIntroDone()
            openRecommendations()
        } else {
            this.showToast(getString(R.string.not_connected))
        }
    }

    private fun markIntroDone() {
        onboardingHelper.isFirstLaunch = false
    }

    private fun openRecommendations() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveSelection() {
        val samples = adapter.items
        val selected = ArrayList<Sample>()

        for (sample in samples) {
            if (sample.selected) {
                selected.add(sample)
            }
        }

        viewModel.store(selected)
    }

    companion object {
        private const val MIN_COUNT = 4
    }

}
