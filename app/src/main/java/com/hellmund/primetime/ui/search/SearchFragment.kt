package com.hellmund.primetime.ui.search

import android.content.Context
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.ApiGenre
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.shared.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.ui.suggestions.MainFragment
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.details.MovieDetailsFragment
import com.hellmund.primetime.ui.suggestions.details.Rating
import com.hellmund.primetime.utils.*
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
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
    lateinit var genresRepository: GenresRepository

    private val categoriesAdapter: SearchCategoriesAdapter by lazy {
        SearchCategoriesAdapter(onItemClick = this::onCategorySelected)
    }

    private val searchResultsAdapter: SearchResultsAdapter by lazy {
        SearchResultsAdapter(
                ImageLoader.with(requireContext()),
                onItemClick = this::onItemClick,
                onWatched = this::onWatched
        )
    }

    private val viewModel: SearchViewModel by lazyViewModel { viewModelProvider }

    private val snackbar: Snackbar by lazy {
        Snackbar.make(resultsRecyclerView, "", Snackbar.LENGTH_LONG)
    }

    // TODO Move to ViewModel
    private val compositeDisposable = CompositeDisposable()

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

        resultsRecyclerView.isVisible = viewState.data.isNotEmpty()
        loading.isVisible = viewState.isLoading
        placeholder.isVisible = viewState.showPlaceholder

        clearSearchButton.isVisible = viewState.showClearButton

        viewState.snackbarText?.let {
            snackbar.setText(it)
                    .show()
        } ?: snackbar.dismiss()
    }

    private fun handleSearchIntent(type: RecommendationsType) {
        val fragment = MainFragment.newInstance(type)
        showFragment(fragment)
    }

    private fun showFragment(fragment: Fragment) {
        requireFragmentManager().transaction {
            replace(R.id.contentFrame, fragment)
            addToBackStack(fragment.javaClass.simpleName)
        }
    }

    private fun initSearch() {
        searchBox.setOnEditorActionListener(this)
        searchBox.addTextChangedListener(this)

        backButton.setOnClickListener {
            clearSearchBarContent()
            toggleSearchResults(false)
            toggleKeyboard(false)
            it.isVisible = searchBox.hasFocus() || searchResultsContainer.isVisible
        }

        searchBox.setOnFocusChangeListener { _, hasFocus ->
            backButton.isVisible = hasFocus || searchResultsContainer.isVisible

            if (hasFocus || searchResultsContainer.isVisible) {
                supportActionBar?.hide()
            } else {
                supportActionBar?.show()
            }

            if (hasFocus) {
                searchResultsAdapter.clear()
                toggleSearchResults(true)
            }
        }

        clearSearchButton.setOnClickListener {
            clearSearchBarContent()
            toggleKeyboard(true)
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
        val recommendationsType = when (category) {
            "Now playing" -> Maybe.just(RecommendationsType.NowPlaying)
            "Upcoming" -> Maybe.just(RecommendationsType.Upcoming)
            else -> {
                genresRepository
                        .getGenreByName(category)
                        .map { ApiGenre(it.id, it.name) }
                        .map { RecommendationsType.ByGenre(it) }
            }
        }

        compositeDisposable += recommendationsType.subscribe { navigate(it) }
    }

    private fun navigate(recommendationsType: RecommendationsType) {
        val fragment = MainFragment.newInstance(recommendationsType)
        requireFragmentManager().transaction {
            replace(R.id.contentFrame, fragment)
            addToBackStack(fragment.javaClass.simpleName)
        }
    }

    private fun buildCategories(genres: List<Genre>): List<String> {
        val categories = listOf(getString(R.string.now_playing), getString(R.string.upcoming))
        val genreNames = genres.map { it.name }
        return categories + genreNames
    }

    private fun initSearchResultsRecyclerView() {
        resultsRecyclerView.itemAnimator = DefaultItemAnimator()
        resultsRecyclerView.adapter = searchResultsAdapter
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
        val historyMovie = HistoryMovie.fromRating(rating)
        viewModel.addToHistory(historyMovie)
    }

    private fun clearSearchBarContent() {
        clearSearchButton.isVisible = false
        searchBox.text.clear()
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

    private fun onItemClick(movie: MovieViewEntity) {
        val fragment = MovieDetailsFragment.newInstance(movie)
        fragment.show(requireFragmentManager(), fragment.tag)
    }

    private fun onWatched(movie: MovieViewEntity) {
        val title = movie.title

        val options = arrayOf(
                getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)
        )

        requireContext().showItemsDialog(
                title = title,
                items = options,
                onSelected = { index ->
                    val rating = if (index == 0) Rating.Like(movie) else Rating.Dislike(movie)
                    addRating(rating)
                }
        )
    }

    private fun toggleKeyboard(show: Boolean) {
        val inputMethodManager = requireContext().inputMethodManager

        if (show) {
            searchBox.requestFocus()
            inputMethodManager.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT)
        } else {
            searchBox.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(searchBox.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        searchBox.removeTextChangedListener(this)
        super.onDestroyView()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
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
