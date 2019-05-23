package com.hellmund.primetime.ui.selectmovies

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.shared.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.activity_select_movies.*
import kotlinx.android.synthetic.main.view_samples_error.*
import javax.inject.Inject
import javax.inject.Provider

class SelectMoviesActivity : AppCompatActivity() {

    private val adapter: SamplesAdapter by lazy {
        SamplesAdapter(viewModel::onItemClick)
    }

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var viewModelProvider: Provider<SelectMoviesViewModel>

    private val viewModel: SelectMoviesViewModel by lazyViewModel { viewModelProvider }

    private var isLoadingMore: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_movies)
        injector.selectMoviesComponent().inject(this)

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

        gridView.onBottomReached {
            if (isLoadingMore.not()) {
                viewModel.refresh()
                isLoadingMore = true
            }
        }

        val spacing = Math.round(resources.getDimension(R.dimen.default_space))
        gridView.addItemDecoration(EqualSpacingGridItemDecoration(spacing, 3))
    }

    private fun render(viewState: SelectMoviesViewState) {
        if (viewState.isFinished) {
            openNext()
            return
        }

        adapter.update(viewState.data)

        if (viewState.isLoading.not()) {
            shimmerLayout.stopShimmer()
            shimmerLayout.setShimmer(null)
        }

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
            showToast(getString(R.string.select_at_least, MIN_COUNT))
        }

        if (isConnected) {
            saveSelection()
        } else {
            showToast(getString(R.string.not_connected))
        }
    }

    private fun saveSelection() {
        val selected = adapter.selected
        viewModel.store(selected)
    }

    private fun openNext() {
        onboardingHelper.isFirstLaunch = false

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val MIN_COUNT = 4
    }

}
