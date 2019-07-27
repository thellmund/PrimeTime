package com.hellmund.primetime.ui.selectgenres

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectmovies.SelectMoviesActivity
import com.hellmund.primetime.utils.observe
import kotlinx.android.synthetic.main.activity_select_genres.button
import kotlinx.android.synthetic.main.activity_select_genres.chipGroup
import kotlinx.android.synthetic.main.activity_select_genres.container
import kotlinx.android.synthetic.main.activity_select_genres.recommendationsProgressBar
import javax.inject.Inject
import javax.inject.Provider

class SelectGenresActivity : AppCompatActivity() {

    private val genres = mutableListOf<Genre>()

    @Inject
    lateinit var viewModelProvider: Provider<SelectGenresViewModel>

    private val viewModel: SelectGenresViewModel by lazyViewModel { viewModelProvider }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_genres)
        injector.inject(this)

        container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        updateNextButton()
        button.setOnClickListener { saveGenres() }

        viewModel.viewState.observe(this, this::render)
    }

    private fun updateNextButton(count: Int = 0) {
        val remaining = MIN_COUNT - count
        val hasSelectedEnough = remaining <= 0

        button.isClickable = hasSelectedEnough
        button.isEnabled = hasSelectedEnough

        if (hasSelectedEnough) {
            button.setText(R.string.next)
        } else {
            button.text = getString(R.string.select_more_format_string, remaining)
        }
    }

    private fun render(viewState: SelectGenresViewState) {
        if (viewState.isFinished) {
            openMoviesSelection()
            return
        }

        genres.clear()
        genres += viewState.data

        recommendationsProgressBar.isVisible = viewState.isLoading
        chipGroup.isVisible = viewState.isLoading.not()

        showGenres(viewState.data)

        // TODO: Error and loading handling (SwipeRefreshLayout)
    }

    private fun showGenres(genres: List<Genre>) {
        genres.map { GenreChip(this, it.name) }.forEach {
            it.setOnCheckedChangeListener { _, _ -> onCheckedChange() }
            chipGroup.addView(it)
        }
    }

    private fun onCheckedChange() {
        val count = chipGroup.childCount
        val children = (0 until count).map { chipGroup.getChildAt(it) as Chip }
        val checked = children.filter { it.isChecked }
        updateNextButton(checked.size)
    }

    private fun saveGenres() {
        val checkedItems = chipGroup.children.toList().map { it as Chip }
        val includedGenres = genres.mapIndexed { index, genre ->
            genre.copy(isPreferred = checkedItems[index].isChecked)
        }
        viewModel.store(includedGenres)
    }

    private fun openMoviesSelection() {
        val intent = Intent(this, SelectMoviesActivity::class.java)
        startActivity(intent)
    }

    companion object {

        private const val MIN_COUNT = 2

        fun newIntent(context: Context): Intent = Intent(context, SelectGenresActivity::class.java)

    }

}
