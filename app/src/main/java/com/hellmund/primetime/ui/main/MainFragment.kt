package com.hellmund.primetime.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import com.hellmund.primetime.model.Movie
import com.hellmund.primetime.ui.SettingsActivity
import com.hellmund.primetime.ui.history.HistoryActivity
import com.hellmund.primetime.ui.search.SearchActivity
import com.hellmund.primetime.ui.watchlist.WatchlistActivity
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.fragment_main.*


class MainFragment : Fragment(), MainContract.View, SuggestionFragment.OnInteractionListener,
        SuggestionErrorFragment.OnInteractionListener, DiscoverMoreFragment.OnInteractionListener {

    private lateinit var presenter: MainContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        presenter = MainPresenterImpl(requireActivity())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.hellmund.primetime.R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        presenter.loadIndices()

        val intent = requireActivity().intent
        if (intent.extras != null) {
            val extra = intent.extras.getString("intent")
            if (extra != null) {
                presenter.handleShortcutOpen(extra)
            }
        }

        setToolbarSubtitle()

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }

        if (displaySingleMovieRecommendation()) {
            setupSingleMovieRecommendations()
        }

        if (!PrefUtils.hasDownloadedHistoryInRealm(requireContext())) { // || History.get().isEmpty()) {
            presenter.downloadHistoryAndRecommendations()
        } else {
            presenter.downloadRecommendationsAsync()
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = requireActivity().intent
        val presenter = intent.getParcelableExtra<MainPresenterImpl>("presenter")
        if (presenter != null) {
            presenter.restoreState(requireActivity())

            val movies: ArrayList<Movie> = intent.getParcelableArrayListExtra("movies")
            presenter.setRecommendations(movies)

            // this.presenter = presenter
        }
    }

    override fun onOpenRatingDialog(position: Int) {
        val options = arrayOf(
                getString(com.hellmund.primetime.R.string.show_more_like_this),
                getString(com.hellmund.primetime.R.string.show_less_like_this)
        )

        AlertDialog.Builder(requireContext())
                .setTitle(getString(com.hellmund.primetime.R.string.adjust_recommendations))
                .setItems(options) { _, which ->
                    val rating = if (which == 0) Constants.LIKE else Constants.DISLIKE
                    presenter.addMovieRating(position, rating)
                }
                .setCancelable(true)
                .show()
    }

    override fun onAddToWatchlist(position: Int) {
        presenter.addToWatchlist(position)
    }

    override fun onRemoveFromWatchlist(position: Int) {
        presenter.removeFromWatchlist(id)
    }

    override fun onGetRecommendation(position: Int): Movie {
        return presenter.getMovieAt(position)
    }

    override fun onGetWatchedStatus(position: Int): Int {
        val id = presenter.getMovieAt(position).id

        if (presenter.onWatchlist(id)) {
            return Constants.ON_WATCHLIST
        }

        if (PrefUtils.hasDownloadedHistoryInRealm(requireContext())) {
            /*if (History.contains(id)) {
                return Constants.WATCHED
            }*/
        }

        return Constants.NOT_WATCHED
    }

    private fun setToolbarSubtitle() {
        requireActivity().actionBar?.subtitle = presenter.getToolbarSubtitle()
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.getParcelableArrayList<Movie>("movies")?.let {
            presenter.setRecommendations(it)
        }
    }

    private fun displaySingleMovieRecommendation(): Boolean {
        val shouldDisplay = requireActivity().intent.getBooleanExtra(Constants.SINGLE_MOVIE, false)
        requireActivity().intent.removeExtra(Constants.SINGLE_MOVIE)
        return shouldDisplay
    }

    private fun setupSingleMovieRecommendations() {
        val intent = requireActivity().intent
        presenter.setupSingleMovieRecommendations(
                intent.getIntExtra(Constants.MOVIE_ID, 0),
                intent.getStringExtra(Constants.MOVIE_TITLE)
        )
        setToolbarSubtitle()
    }

    override fun onDownloadStart() {
        progressBar.visibility = View.VISIBLE
        suggestions.visibility = View.GONE
    }

    override fun onSuccess() {
        initViewPager(Constants.IDEAL_STATE)
    }

    private fun initViewPager(viewState: Int) {
        val size = when (viewState) {
            Constants.IDEAL_STATE -> presenter.getRecommendations().size + 1
            else -> 1
        }

        if (suggestions != null) {
            suggestions.adapter = SuggestionsAdapter(requireFragmentManager(), requireContext(), viewState, size, this)
            progressBar.visibility = View.GONE
            suggestions.visibility = View.VISIBLE
        }
    }

    override fun onError() {
        initViewPager(Constants.ERROR_STATE)
    }

    override fun onEmpty() {
        initViewPager(Constants.EMPTY_STATE)
    }

    override fun onMovieRatingAdded(id: Int, rating: Int) {
        displayRatingSnackbar(suggestions.currentItem, id, rating)
        suggestions.currentItem = suggestions.currentItem + 1
    }

    private fun displayRatingSnackbar(position: Int, id: Int, rating: Int) {
        val message = if (rating == Constants.LIKE) {
            getString(com.hellmund.primetime.R.string.will_more_like_this)
        } else {
            getString(com.hellmund.primetime.R.string.will_less_like_this)
        }

        val movie = presenter.getMovieAt(position)
        // History.add(movie, rating)

        Snackbar.make(suggestions, message, Snackbar.LENGTH_LONG)
                .setAction(com.hellmund.primetime.R.string.undo) {
                    suggestions.currentItem = position
                    presenter.showUndoToast(id, rating)
                    // History.remove(id);
                }
                .setActionTextColor(UiUtils.getSnackbarColor(requireContext()))
                .show()
    }

    override fun tryDownloadAgain() {
        presenter.forceRecommendationsDownload()
        presenter.downloadRecommendationsAsync()
    }

    override fun openGenresDialog() {
        val genres = buildGenresList()
        val adapter = GenresDialogAdapter(requireContext(), genres)

        AlertDialog.Builder(requireContext())
                .setAdapter(adapter) { dialog, which ->
                    val selected = adapter.getItem(which)

                    if (presenter.genreAlreadySelected(selected)) {
                        dialog.dismiss()
                        return@setAdapter
                    }

                    if (!DeviceUtils.isConnected(requireContext())) {
                        UiUtils.showToast(requireContext(), getString(com.hellmund.primetime.R.string.not_connected))
                    } else {
                        handleGenreDialogInput(selected, which)
                    }
                }
                .setCancelable(true)
                .setNegativeButton(com.hellmund.primetime.R.string.close) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun handleGenreDialogInput(selected: String, which: Int) {
        if (which == 1) {
            openSearch()
        } else {
            presenter.handleGenreDialogInput(selected, which)
            setToolbarSubtitle()
            refreshRecommendations()
        }
    }

    private fun refreshRecommendations() {
        if (!DeviceUtils.isConnected(requireContext())) {
            UiUtils.showToast(requireContext(), getString(com.hellmund.primetime.R.string.not_connected))
            return
        }

        presenter.forceRecommendationsDownload()
        presenter.downloadRecommendationsAsync()
    }

    private fun buildGenresList(): Array<String> {
        val nonGenreCategories = 4

        val genres = GenreUtils.getGenres(requireContext())
        val genreTitles = arrayOfNulls<String>(genres.size)

        for (i in genres.indices) {
            genreTitles[i] = genres[i].name
        }

        val length = nonGenreCategories + GenreUtils.getGenres(requireContext()).size

        val categories = arrayOfNulls<String>(length)
        categories[0] = getString(com.hellmund.primetime.R.string.personalized_recommendations)
        categories[1] = getString(com.hellmund.primetime.R.string.movie_based_recommendations)
        categories[2] = getString(com.hellmund.primetime.R.string.now_playing)
        categories[3] = getString(com.hellmund.primetime.R.string.upcoming)

        System.arraycopy(genreTitles, 0, categories, nonGenreCategories, genreTitles.size)
        return categories as Array<String>
    }

    override fun openSearch() {
        val intent = Intent(requireContext(), SearchActivity::class.java)
        startActivity(intent)
    }

    override fun openWatchlist() {
        val intent = Intent(requireContext(), WatchlistActivity::class.java)
        startActivity(intent)
    }

    private fun openHistory() {
        val intent = Intent(requireContext(), HistoryActivity::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent(requireContext(), SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(com.hellmund.primetime.R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            com.hellmund.primetime.R.id.action_watchlist -> {
                openWatchlist()
                return true
            }
            com.hellmund.primetime.R.id.action_genre_recommendations -> {
                openGenresDialog()
                return true
            }
            com.hellmund.primetime.R.id.action_refresh -> {
                refreshRecommendations()
                return true
            }
            com.hellmund.primetime.R.id.action_history -> {
                openHistory()
                return true
            }
            com.hellmund.primetime.R.id.action_settings -> {
                openSettings()
                return true
            }
            android.R.id.home -> {
                requireActivity().onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    override fun onStop() {
        presenter.saveIndices()
        super.onStop()
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

}
