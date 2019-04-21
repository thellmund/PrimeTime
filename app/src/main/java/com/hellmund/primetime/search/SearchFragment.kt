package com.hellmund.primetime.search

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.hellmund.primetime.R
import com.hellmund.primetime.api.ApiClient
import com.hellmund.primetime.database.GenreDao
import com.hellmund.primetime.database.HistoryMovie
import com.hellmund.primetime.database.PrimeTimeDatabase
import com.hellmund.primetime.history.HistoryRepository
import com.hellmund.primetime.main.MainActivity
import com.hellmund.primetime.main.MainFragment
import com.hellmund.primetime.main.RecommendationsRepository
import com.hellmund.primetime.main.RecommendationsType
import com.hellmund.primetime.model.SearchResult
import com.hellmund.primetime.model2.ApiGenre
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.RealGenresProvider
import com.hellmund.primetime.utils.isVisible
import com.hellmund.primetime.utils.observe
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.state_layout_search_results.*
import kotlinx.android.synthetic.main.view_search_field.*
import org.jetbrains.anko.inputMethodManager
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import java.lang.Math.round

class SearchFragment : Fragment(), TextWatcher,
        TextView.OnEditorActionListener, MainActivity.Reselectable {

    private val viewModel: SearchViewModel by lazy {
        val genresProvider = RealGenresProvider(defaultSharedPreferences)
        val repository = RecommendationsRepository(ApiClient.instance, genresProvider)
        val historyRepository = HistoryRepository(PrimeTimeDatabase.getInstance(requireContext()))
        val factory = SearchViewModel.Factory(repository, historyRepository)
        ViewModelProviders.of(requireActivity(), factory).get(SearchViewModel::class.java)
    }

    private val searchAdapter: SearchAdapter by lazy {
        SearchAdapter(
                requireContext(),
                onShowSimilar = this::showSimilarMovies,
                onWatched = this::onWatched
        )
    }

    private val genreDao: GenreDao by lazy {
        PrimeTimeDatabase.getInstance(requireContext()).genreDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearch()
        initCategoriesRecyclerView()
        initSearchResultsRecyclerView()

        viewModel.viewState.observe(this, this::render)

        val type = arguments?.getParcelable<RecommendationsType>(KEY_RECOMMENDATIONS_TYPE)
        type?.let {
            handleSearchIntent(it)
        }
    }

    private fun initToolbar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.search)
    }

    private fun render(viewState: SearchViewState) {
        searchAdapter.update(viewState.data)

        results_list.isVisible = viewState.data.isNotEmpty()
        loading_container.isVisible = viewState.isLoading
        placeholder_container.isVisible = viewState.showPlaceholder

        search_clear.isVisible = viewState.showClearButton

        viewState.rating?.let {
            showAddedToHistorySnackbar(it)
        } ?: dismissAddedToHistorySnackbar()

        /*if (input.isEmpty()) {
                search_box.text.clear()
                search_box.requestFocus()
            } else {
                toggleKeyboard(false)
                downloadQueryResults(input)
            }*/
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
            toggleSearchResults(false)
        }

        search_box.setOnFocusChangeListener { _, hasFocus ->
            backButton.isVisible = hasFocus
            if (hasFocus) {
                toggleSearchResults(true)
            }
        }

        search_clear.setOnClickListener {
            if (it.alpha == Constants.ENABLED) {
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
        val adapter = SearchCategoriesAdapter(buildCategories(), this::onCategorySelected)
        categoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        categoriesRecyclerView.itemAnimator = DefaultItemAnimator()
        categoriesRecyclerView.adapter = adapter

        val spacing = round(resources.getDimension(R.dimen.default_space))
        categoriesRecyclerView.addItemDecoration(EqualSpacingGridItemDecoration(spacing))
    }

    private fun onCategorySelected(category: String) {
        val genre = genreDao.getGenre(category).blockingGet()
        val apiGenre = ApiGenre(genre.id, genre.name)

        val type = when (category) {
            "Now playing" -> RecommendationsType.NowPlaying
            "Upcoming" -> RecommendationsType.Upcoming
            else -> RecommendationsType.ByGenre(apiGenre)
        }

        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, MainFragment.newInstance(type))
                .addToBackStack(null)
                .commit()
    }

    private fun buildCategories(): List<String> {
        val categories = listOf(getString(R.string.now_playing), getString(R.string.upcoming))
        val genres = genreDao.getAll().blockingGet().map { it.name }
        return categories + genres
    }

    private fun initSearchResultsRecyclerView() {
        results_list.adapter = searchAdapter

        /*results_list.setOnItemClickListener { _, _, position, _ ->
            showSimilarMovies(position)
        }*/

        // TODO
        /*results_list.setOnItemLongClickListener { _, _, position, _ ->
            displayRatingDialog(position)
            true
        }*/
    }

    override fun onResume() {
        super.onResume()
        initToolbar()
        // TODO (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        // TODO (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onReselected() {
        val current = requireFragmentManager().findFragmentById(R.id.contentFrame)
        if (current is SearchFragment) {
            toggleKeyboard(true)
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        viewModel.onTextChanged(s.toString())
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    /*private fun displayRatingDialog(position: Int) {
        val result = results_list.adapter.getItem(position) as SearchResult
        val title = result.title

        val options = arrayOf(
                getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)
        )

        AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(options) { _, which ->
                    if (which == 2) {
                        addToWatchlist(result)
                    } else {
                        val rating = if (which == 0) Constants.LIKE else Constants.DISLIKE
                        addRating(position, rating)
                    }
                }
                .setCancelable(true)
                .show()
    }*/

    private fun addRating(searchResult: SearchResult, rating: Int) {
        val message: String = if (rating == 1) {
            getString(R.string.will_more_like_this)
        } else {
            getString(R.string.will_less_like_this)
        }

        // val result = results_list.adapter.getItem(position) as SearchResult
        // History.add(result, rating);
        val historyMovie = HistoryMovie.fromSearchResult(searchResult, rating)
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

    private fun addToWatchlist(searchResult: SearchResult) {
        viewModel.addToWatchlist(searchResult)

        /*val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Adding movie to watchlist ...")
        progressDialog.show()

        requireActivity().supportLoaderManager
                .initLoader(1, null, object : LoaderManager.LoaderCallbacks<Array<Long>> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Array<Long>> {
                return DownloadRuntimeReleaseLoader(requireContext(), searchResult.id)
            }

            override fun onLoadFinished(loader: Loader<Array<Long>>, results: Array<Long>) {
                if (results[1] != null) {
                    val releaseDate = Date(results[1])
                    searchResult.releaseDate = releaseDate
                }

                if (results[0] != null) {
                    val runtime = results[0].toInt()
                    searchResult.runtime = runtime
                    progressDialog.dismiss()
                    UiUtils.showToast(requireContext(), R.string.added_to_watchlist)
                }
            }

            override fun onLoaderReset(loader: Loader<Array<Long>>) {}
        })*/
    }

    private fun showSimilarMovies(position: Int) {
        val result = results_list.adapter.getItem(position) as SearchResult
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

    private fun showSimilarMovies(searchResult: SearchResult) {
        val type = RecommendationsType.BasedOnMovie(searchResult.id, searchResult.title)
        val fragment = MainFragment.newInstance(type)
        showFragment(fragment)
    }

    private fun onWatched(searchResult: SearchResult) {
        val title = searchResult.title

        val options = arrayOf(
                getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)
        )

        AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(options) { _, which ->
                    val rating = if (which == 0) Constants.LIKE else Constants.DISLIKE
                    addRating(searchResult, rating)
                }
                .setCancelable(true)
                .show()
    }

    /*private fun toggleViews(state: Int) {
        results_list.visibility = View.GONE
        placeholder_container.visibility = View.GONE
        loading_container.visibility = View.GONE

        when (state) {
            DISPLAY_LIST -> results_list.visibility = View.VISIBLE
            DISPLAY_LOADING -> loading_container.visibility = View.VISIBLE
            DISPLAY_EMPTY -> placeholder_container.visibility = View.VISIBLE
        }
    }*/

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
