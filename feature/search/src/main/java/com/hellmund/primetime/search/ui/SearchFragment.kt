package com.hellmund.primetime.search.ui

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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.core.FragmentFactory
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.search.R
import com.hellmund.primetime.ui_common.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.ui_common.dialogs.RateMovieDialog
import com.hellmund.primetime.ui_common.util.ImageLoader
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import com.pandora.bottomnavigator.BottomNavigator
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_search.categoriesRecyclerView
import kotlinx.android.synthetic.main.state_layout_search_results.loading
import kotlinx.android.synthetic.main.state_layout_search_results.placeholder
import kotlinx.android.synthetic.main.state_layout_search_results.resultsRecyclerView
import kotlinx.android.synthetic.main.state_layout_search_results.searchResultsContainer
import kotlinx.android.synthetic.main.view_search_field.backButton
import kotlinx.android.synthetic.main.view_search_field.clearSearchButton
import kotlinx.android.synthetic.main.view_search_field.searchBox
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.lang.Math.round
import javax.inject.Inject
import javax.inject.Provider

@ExperimentalCoroutinesApi
@FlowPreview
class SearchFragment : DaggerFragment(), TextWatcher,
    TextView.OnEditorActionListener, Reselectable {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<SearchViewModel>

    @Inject
    lateinit var fragmentFactory: FragmentFactory

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

        arguments?.getString(KEY_EXTRA)?.let {
            viewModel.dispatch(Action.ProcessExtra(it))
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
            snackbar.setText(it).show()
        } ?: snackbar.dismiss()
    }

    private fun initSearch() {
        searchBox.setOnEditorActionListener(this)
        searchBox.addTextChangedListener(this)

        backButton.setOnClickListener {
            toggleSearchResults(false)
            toggleKeyboard(false)
            clearSearchBarContent()

            it.isVisible = searchBox.hasFocus() || searchResultsContainer.isVisible
        }

        searchBox.setOnFocusChangeListener { _, hasFocus ->
            backButton.isVisible = hasFocus || searchResultsContainer.isVisible

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
        viewModel.dispatch(Action.CategorySelected(category))
    }

    private fun navigate(event: NavigationEvent) {
        val recommendationsType = event.getIfNotHandled() ?: return
        openCategory(recommendationsType)
    }

    private fun openCategory(type: RecommendationsType) {
        val args = bundleOf(FragmentArgs.KEY_RECOMMENDATIONS_TYPE to type)
        val fragment = fragmentFactory.category(args)

        val navigator = BottomNavigator.provide(requireActivity())
        navigator.addFragment(fragment)
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
        viewModel.dispatch(Action.TextChanged(s.toString()))
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    private fun addRating(ratedMovie: RatedMovie) {
        viewModel.dispatch(Action.AddToHistory(ratedMovie))
    }

    private fun clearSearchBarContent() {
        clearSearchButton.isVisible = false
        searchBox.text.clear()
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            val input = v.text.toString().trim()
            viewModel.dispatch(Action.Search(input))
            toggleKeyboard(false)
            return true
        }

        return false
    }

    private fun onItemClick(movie: MovieViewEntity) {
        val args = bundleOf(FragmentArgs.KEY_MOVIE to movie)
        val fragment = fragmentFactory.movieDetails(args) as BottomSheetDialogFragment
        fragment.show(requireFragmentManager(), fragment.tag)
    }

    private fun onWatched(movie: MovieViewEntity) {
        RateMovieDialog
            .make(requireActivity())
            .setTitle(movie.title)
            .setPositiveText(R.string.show_more_like_this)
            .setNegativeText(R.string.show_less_like_this)
            .onItemSelected { rating ->
                val ratedMovie = RatedMovie(movie, rating)
                addRating(ratedMovie)
            }
            .show()
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

        private const val KEY_EXTRA = "KEY_EXTRA"

        @JvmStatic
        fun newInstance(
            extra: String? = null
        ) = SearchFragment().apply {
            arguments = bundleOf(KEY_EXTRA to extra)
        }

    }

}
