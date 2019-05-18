package com.hellmund.primetime.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.AppDatabase
import com.hellmund.primetime.data.database.GenreDao
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.ApiGenre
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.shared.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.ui.suggestions.MainFragment
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.details.Rating
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.observe
import com.hellmund.primetime.utils.supportActionBar
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.state_layout_search_results.*
import kotlinx.android.synthetic.main.view_search_field.*
import org.jetbrains.anko.inputMethodManager
import java.lang.Math.round
import javax.inject.Inject
import javax.inject.Provider

class SearchFragment : Fragment(), TextWatcher,
        TextView.OnEditorActionListener, MainActivity.Reselectable {

    @Inject
    lateinit var viewModelProvider: Provider<SearchViewModel>

    @Inject
    lateinit var database: AppDatabase

    private val categoriesAdapter: SearchCategoriesAdapter by lazy {
        SearchCategoriesAdapter(onItemClick = this::onCategorySelected)
    }

    private val searchResultsAdapter: SearchResultsAdapter by lazy {
        SearchResultsAdapter(
                requireContext(),
                onShowSimilar = this::showSimilarMovies,
                onWatched = this::onWatched
        )
    }

    private val genreDao: GenreDao by lazy {
        database.genreDao()
    }

    private val viewModel: SearchViewModel by lazyViewModel { viewModelProvider }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearch()
        initCategoriesRecyclerView()
        initSearchResultsRecyclerView()

        viewModel.viewState.observe(viewLifecycleOwner, this::render)

        val type = arguments?.getParcelable<RecommendationsType>(KEY_RECOMMENDATIONS_TYPE)
        type?.let {
            handleSearchIntent(it)
        }
    }

    override fun onResume() {
        super.onResume()
        initToolbar()
    }

    private fun initToolbar() {
        supportActionBar?.title = null
    }

    private fun render(viewState: SearchViewState) {
        val categories = buildCategories(viewState.genres)
        categoriesAdapter.update(categories)

        searchResultsAdapter.update(viewState.data)

        results_list.isVisible = viewState.data.isNotEmpty()
        loading_container.isVisible = viewState.isLoading
        placeholder_container.isVisible = viewState.showPlaceholder

        search_clear.isVisible = viewState.showClearButton

        viewState.rating?.let {
            showAddedToHistorySnackbar(it)
        } ?: dismissAddedToHistorySnackbar()
    }

    private fun handleSearchIntent(type: RecommendationsType) {
        val fragment = MainFragment.newInstance(type)
        showFragment(fragment)
    }

    private fun showFragment(fragment: Fragment) {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .addToBackStack(null)
                .commit()
    }

    private fun initSearch() {
        search_box.setOnEditorActionListener(this)
        search_box.addTextChangedListener(this)

        backButton.setOnClickListener {
            clearSearchBarContent()
            toggleKeyboard(false)
            toggleSearchResults(false)
        }

        search_box.setOnFocusChangeListener { _, hasFocus ->
            backButton.isVisible = hasFocus

            if (hasFocus) {
                supportActionBar?.hide()
            } else {
                supportActionBar?.show()
            }

            if (hasFocus) {
                searchResultsAdapter.clear()
                toggleSearchResults(true)
            }
        }

        search_clear.setOnClickListener {
            if (it.alpha == 1f) {
                clearSearchBarContent()
                toggleKeyboard(true)
            }
        }
    }

    private fun toggleSearchResults(showSearchResults: Boolean) {
        searchResultsContainer.isVisible = showSearchResults
        categoriesRecyclerView.isVisible = showSearchResults.not()
    }

    private fun initCategoriesRecyclerView() {
        categoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        categoriesRecyclerView.itemAnimator = DefaultItemAnimator()
        categoriesRecyclerView.adapter = categoriesAdapter

        val spacing = round(resources.getDimension(R.dimen.default_space))
        categoriesRecyclerView.addItemDecoration(EqualSpacingGridItemDecoration(spacing))
    }

    private fun onCategorySelected(category: String) {
        val type = when (category) {
            "Now playing" -> RecommendationsType.NowPlaying
            "Upcoming" -> RecommendationsType.Upcoming
            else -> {
                val genre = genreDao.getGenre(category).blockingGet()
                val apiGenre = ApiGenre(genre.id, genre.name)
                RecommendationsType.ByGenre(apiGenre)
            }
        }

        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, MainFragment.newInstance(type))
                .addToBackStack(null)
                .commit()
    }

    private fun buildCategories(genres: List<Genre>): List<String> {
        val categories = listOf(getString(R.string.now_playing), getString(R.string.upcoming))
        val genreNames = genres.map { it.name }
        return categories + genreNames
    }

    private fun initSearchResultsRecyclerView() {
        results_list.adapter = searchResultsAdapter
    }

    override fun onReselected() {
        val current = requireFragmentManager().findFragmentById(R.id.contentFrame)
        if (current is SearchFragment) {
            if (searchResultsContainer.isVisible) {
                searchResultsContainer.isVisible = false
                categoriesRecyclerView.isVisible = true
            } else {
                toggleKeyboard(true)
            }
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        viewModel.onTextChanged(s.toString())
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    private fun addRating(rating: Rating) {
        val message = when (rating) {
            is Rating.Like -> getString(R.string.will_more_like_this)
            is Rating.Dislike -> getString(R.string.will_less_like_this)
        }

        val historyMovie = HistoryMovie.fromRating(rating)
        viewModel.addToHistory(historyMovie)

        Snackbar.make(results_list, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) { /* TODO */ }
                // TODO .setActionTextColor(UiUtils.getSnackbarColor(requireContext()))
                .show()
    }

    private val historySnackbar: Snackbar by lazy {
        Snackbar.make(results_list, "", Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) { /* TODO */ }
                // TODO .setActionTextColor(UiUtils.getSnackbarColor(requireContext()))
    }

    private fun showAddedToHistorySnackbar(rating: Int) {
        val message: String = if (rating == 1) {
            getString(R.string.will_more_like_this)
        } else {
            getString(R.string.will_less_like_this)
        }

        historySnackbar
                .setText(message)
                .show()
    }

    private fun dismissAddedToHistorySnackbar() {
        historySnackbar.dismiss()
    }

    private fun addToWatchlist(movie: MovieViewEntity) {
        viewModel.addToWatchlist(movie)
    }

    private fun showSimilarMovies(position: Int) {
        val result = results_list.adapter.getItem(position) as MovieViewEntity
        val id = result.id
        val title = result.title

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra(Constants.SINGLE_MOVIE, true)
        intent.putExtra(Constants.MOVIE_ID, id)
        intent.putExtra(Constants.MOVIE_TITLE, title)

        startActivity(intent)
        requireActivity().finish()
    }

    private fun clearSearchBarContent() {
        search_clear.alpha = 0.7f
        search_box.text.clear()
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            val input = v.text.toString().trim()
            viewModel.search(input)
            toggleKeyboard(false)
            return true
        }

        return false
    }

    private fun showSimilarMovies(movie: MovieViewEntity) {
        val type = RecommendationsType.BasedOnMovie(movie.id, movie.title)
        val fragment = MainFragment.newInstance(type)
        showFragment(fragment)
    }

    private fun onWatched(movie: MovieViewEntity) {
        val title = movie.title

        val options = arrayOf(
                getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)
        )

        AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(options) { _, index ->
                    val rating = if (index == 0) Rating.Like(movie) else Rating.Dislike(movie)
                    // val rating = if (which == 0) Constants.LIKE else Constants.DISLIKE
                    addRating(rating)
                }
                .setCancelable(true)
                .show()
    }

    private fun toggleKeyboard(show: Boolean) {
        val inputMethodManager = requireContext().inputMethodManager

        if (show) {
            search_box.requestFocus()
            inputMethodManager.showSoftInput(search_box, InputMethodManager.SHOW_IMPLICIT)
        } else {
            search_box.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(search_box.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        search_box.removeTextChangedListener(this)
        super.onDestroyView()
    }

    companion object {

        private const val KEY_RECOMMENDATIONS_TYPE = "KEY_RECOMMENDATIONS_TYPE"

        @JvmStatic
        fun newInstance(type: RecommendationsType? = null): SearchFragment {
            return SearchFragment().apply {
                arguments = Bundle().apply { putParcelable(KEY_RECOMMENDATIONS_TYPE, type) }
            }
        }

    }

}
