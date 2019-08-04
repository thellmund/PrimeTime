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
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.shared.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui.shared.NavigationEvent
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.ui.suggestions.MainFragment
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.RatedMovie
import com.hellmund.primetime.ui.suggestions.RecommendationsType
import com.hellmund.primetime.ui.suggestions.details.MovieDetailsFragment
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.observe
import com.hellmund.primetime.utils.showItemsDialog
import kotlinx.android.synthetic.main.fragment_search.categoriesRecyclerView
import kotlinx.android.synthetic.main.state_layout_search_results.loading
import kotlinx.android.synthetic.main.state_layout_search_results.placeholder
import kotlinx.android.synthetic.main.state_layout_search_results.resultsRecyclerView
import kotlinx.android.synthetic.main.state_layout_search_results.searchResultsContainer
import kotlinx.android.synthetic.main.view_search_field.backButton
import kotlinx.android.synthetic.main.view_search_field.clearSearchButton
import kotlinx.android.synthetic.main.view_search_field.searchBox
import kotlinx.android.synthetic.main.view_toolbar_search.toolbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.lang.Math.round
import javax.inject.Inject
import javax.inject.Provider

@ExperimentalCoroutinesApi
@FlowPreview
class SearchFragment : Fragment(), TextWatcher,
    TextView.OnEditorActionListener, MainActivity.Reselectable {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<SearchViewModel>

    private val categoriesAdapter: SearchCategoriesAdapter by lazy {
        SearchCategoriesAdapter(onItemClick = this::onCategorySelected)
    }

    private val searchResultsAdapter: SearchResultsAdapter by lazy {
        SearchResultsAdapter(
            imageLoader,
            onItemClick = this::onItemClick,
            onWatched = this::onWatched
        )
    }

    private val viewModel: SearchViewModel by lazyViewModel { viewModelProvider }

    private val snackbar: Snackbar by lazy {
        Snackbar.make(resultsRecyclerView, "", Snackbar.LENGTH_LONG)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.inject(this)
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
        viewModel.destinations.observe(viewLifecycleOwner, this::navigate)

        val type = arguments?.getParcelable<RecommendationsType>(KEY_RECOMMENDATIONS_TYPE)
        type?.let {
            handleSearchIntent(it)
        }
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
            toolbar.isVisible = !searchResultsContainer.isVisible
        }

        searchBox.setOnFocusChangeListener { _, hasFocus ->
            backButton.isVisible = hasFocus || searchResultsContainer.isVisible
            toolbar.isVisible = !backButton.isVisible

            if (hasFocus) {
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
        viewModel.onCategorySelected(category)
    }

    private fun navigate(event: NavigationEvent) {
        val recommendationsType = event.getIfNotHandled() ?: return
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
        toggleKeyboard(true)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        viewModel.onTextChanged(s.toString())
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    private fun addRating(ratedMovie: RatedMovie) {
        val historyMovie = HistoryMovie.from(ratedMovie)
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
                val rating = if (index == 0) Rating.Like else Rating.Dislike
                val ratedMovie = RatedMovie(movie, rating)
                addRating(ratedMovie)
            }
        )
    }

    private fun toggleKeyboard(show: Boolean) {
        val inputMethodManager = requireContext().getSystemService<InputMethodManager>()

        if (show) {
            searchBox.requestFocus()
            inputMethodManager?.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT)
        } else {
            searchBox.clearFocus()
            inputMethodManager?.hideSoftInputFromWindow(searchBox.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        searchBox.removeTextChangedListener(this)
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
