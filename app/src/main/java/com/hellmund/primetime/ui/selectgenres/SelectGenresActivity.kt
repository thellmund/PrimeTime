package com.hellmund.primetime.ui.selectgenres

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectmovies.SelectMoviesActivity
import com.hellmund.primetime.utils.observe
import kotlinx.android.synthetic.main.activity_select_genres.*
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

        list_view.setOnItemClickListener { _, _, _, _ ->
            val isEnabled = list_view.checkedItemCount >= MIN_COUNT
            button.isClickable = isEnabled
            button.isEnabled = isEnabled
        }

        button.setOnClickListener {
            saveGenres()
            openMoviesSelection()
        }

        viewModel.viewState.observe(this, this::render)

        // TODO
        // When selecting the first two genres, slide in the next button from the bottom
    }

    private fun render(viewState: SelectGenresViewState) {
        genres.clear()
        genres += viewState.data

        swipeRefreshLayout.isRefreshing = viewState.isLoading
        swipeRefreshLayout.isEnabled = false
        showGenres(viewState.data)

        // TODO: Error and loading handling (SwipeRefreshLayout)
    }

    private fun showGenres(genres: List<Genre>) {
        list_view.adapter = GenresAdapter(this, genres)
    }

    private fun saveGenres() {
        val checkedItems = list_view.checkedItemPositions
        val includedGenres = genres.mapIndexed {
            index, genre -> genre.copy(isPreferred = checkedItems[index])
        }
        viewModel.store(includedGenres)
    }

    private fun openMoviesSelection() {
        val intent = Intent(this, SelectMoviesActivity::class.java)
        startActivity(intent)
    }

    companion object {

        private const val MIN_COUNT = 2

        fun newIntent(context: Context): Intent {
            return Intent(context, SelectGenresActivity::class.java)
        }
    }

}
