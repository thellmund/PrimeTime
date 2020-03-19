package com.hellmund.primetime.search.ui

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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hellmund.primetime.core.DestinationFactory
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.search.R
import com.hellmund.primetime.search.databinding.FragmentSearchBinding
import com.hellmund.primetime.search.di.DaggerSearchComponent
import com.hellmund.primetime.ui_common.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.RatedMovie
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.ui_common.dialogs.RateMovieDialog
import com.hellmund.primetime.ui_common.util.navigator
import com.hellmund.primetime.ui_common.viewmodel.handle
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

class SearchFragment : Fragment(), TextWatcher,
    TextView.OnEditorActionListener, Reselectable {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<SearchViewModel>

    @Inject
    lateinit var destinationFactory: DestinationFactory

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
        Snackbar.make(binding.root, "", Snackbar.LENGTH_LONG)
    }

    private lateinit var binding: FragmentSearchBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val component = DaggerSearchComponent.builder()
            .core(coreComponent)
            .build()
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearch()
        initCategoriesRecyclerView()
        initSearchResultsRecyclerView()

        viewModel.viewState.observe(viewLifecycleOwner, this::render)
        viewModel.navigationResults.handle(viewLifecycleOwner, this::navigate)

        arguments?.getString(KEY_EXTRA)?.let {
            viewModel.dispatch(ViewEvent.ProcessExtra(it))
        }
    }

    fun openCategory(category: String) {
        viewModel.dispatch(ViewEvent.CategorySelected(category))
    }

    private fun render(viewState: SearchViewState) {
        val categories = buildCategories(viewState.genres)
        categoriesAdapter.update(categories)

        searchResultsAdapter.update(viewState.data)

        binding.stateLayout.resultsRecyclerView.isVisible = viewState.data.isNotEmpty()
        binding.stateLayout.loading.isVisible = viewState.isLoading
        binding.stateLayout.placeholder.isVisible = viewState.showPlaceholder

        binding.searchContainer.clearSearchButton.isVisible = viewState.showClearButton

        viewState.snackbarTextResId?.let {
            snackbar.setText(it).show()
        } ?: snackbar.dismiss()
    }

    private fun initSearch() {
        val searchContainer = binding.searchContainer
        val searchBox = searchContainer.searchBox

        searchBox.setOnEditorActionListener(this)
        searchBox.addTextChangedListener(this)

        searchContainer.backButton.setOnClickListener {
            toggleSearchResults(false)
            toggleKeyboard(false)
            clearSearchBarContent()

            it.isVisible = searchBox.hasFocus() || binding.stateLayout.root.isVisible
        }

        searchBox.setOnFocusChangeListener { _, hasFocus ->
            searchContainer.backButton.isVisible = hasFocus || binding.stateLayout.root.isVisible

            if (hasFocus) {
                toggleSearchResults(true)
            }
        }

        searchContainer.clearSearchButton.setOnClickListener {
            clearSearchBarContent()
            toggleKeyboard(true)
        }
    }

    private fun toggleSearchResults(showSearchResults: Boolean) = with(binding) {
        stateLayout.root.isVisible = showSearchResults
        categoriesRecyclerView.isVisible = showSearchResults.not()
    }

    private fun initCategoriesRecyclerView() {
        val categoriesRecyclerView = binding.categoriesRecyclerView
        categoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        categoriesRecyclerView.itemAnimator = DefaultItemAnimator()
        categoriesRecyclerView.adapter = categoriesAdapter

        val spacing = resources.getDimension(R.dimen.default_space).roundToInt()
        categoriesRecyclerView.addItemDecoration(EqualSpacingGridItemDecoration(spacing))
    }

    private fun onCategorySelected(category: String) {
        viewModel.dispatch(ViewEvent.CategorySelected(category))
    }

    private fun navigate(result: NavigationResult) {
        when (result) {
            is NavigationResult.OpenMovieDetails -> onClickedMovieLoaded(result.viewEntity)
            is NavigationResult.OpenCategory -> openCategory(result.recommendationsType)
        }
    }

    private fun openCategory(type: RecommendationsType) {
        val args = bundleOf(FragmentArgs.KEY_RECOMMENDATIONS_TYPE to type)
        val fragment = destinationFactory.category(args)
        navigator.addFragment(fragment)
    }

    private fun buildCategories(genres: List<Genre>): List<String> {
        val categories = listOf(getString(R.string.now_playing), getString(R.string.upcoming))
        val genreNames = genres.map { it.name }
        return categories + genreNames
    }

    private fun initSearchResultsRecyclerView() {
        val resultsRecyclerView = binding.stateLayout.resultsRecyclerView
        resultsRecyclerView.itemAnimator = DefaultItemAnimator()
        resultsRecyclerView.adapter = searchResultsAdapter
    }

    override fun onReselected() {
        toggleKeyboard(true)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        viewModel.dispatch(ViewEvent.TextChanged(s.toString()))
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    private fun addRating(ratedMovie: RatedMovie.Partial) {
        viewModel.dispatch(ViewEvent.AddToHistory(ratedMovie))
    }

    private fun clearSearchBarContent() = with(binding.searchContainer) {
        clearSearchButton.isVisible = false
        searchBox.text.clear()
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            val input = v.text.toString().trim()
            viewModel.dispatch(ViewEvent.Search(input))
            toggleKeyboard(false)
            return true
        }

        return false
    }

    private fun onItemClick(movie: MovieViewEntity.Partial) {
        val args = bundleOf(FragmentArgs.KEY_MOVIE to movie)
        val intent = destinationFactory.movieDetails(args)
        startActivity(intent)
        // viewModel.dispatch(ViewEvent.MovieClicked(movie))
    }

    private fun onClickedMovieLoaded(movie: MovieViewEntity.Full) {
        val args = bundleOf(FragmentArgs.KEY_MOVIE to movie)
        val intent = destinationFactory.movieDetails(args)
        startActivity(intent)
    }

    private fun onWatched(movie: MovieViewEntity.Partial) {
        RateMovieDialog
            .make(requireActivity())
            .setTitle(movie.title)
            .setPositiveText(R.string.show_more_like_this)
            .setNegativeText(R.string.show_less_like_this)
            .onItemSelected { rating ->
                val ratedMovie = movie + rating
                addRating(ratedMovie)
            }
            .show()
    }

    private fun toggleKeyboard(show: Boolean) {
        val inputMethodManager = requireContext().getSystemService<InputMethodManager>()
        val searchBox = binding.searchContainer.searchBox

        if (show) {
            searchBox.requestFocus()
            inputMethodManager?.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT)
        } else {
            searchBox.clearFocus()
            inputMethodManager?.hideSoftInputFromWindow(searchBox.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        binding.searchContainer.searchBox.removeTextChangedListener(this)
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
