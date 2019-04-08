package com.hellmund.primetime.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import com.hellmund.primetime.R
import com.hellmund.primetime.model.Movie
import com.hellmund.primetime.search.SearchActivity
import com.hellmund.primetime.settings.SettingsActivity
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.DeviceUtils
import com.hellmund.primetime.utils.UiUtils
import com.hellmund.primetime.watchlist.WatchlistActivity
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

        setToolbarSubtitle()

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }

        val intentExtra = arguments?.getString(KEY_INTENT)
        if (intentExtra != null) {
            setupIntentRecommendations(intentExtra)
        } else if (displaySingleMovieRecommendation()) {
            setupSingleMovieRecommendations()
        }

        presenter.downloadRecommendationsAsync()
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

    private fun setupIntentRecommendations(intentExtra: String) {
        presenter.setupCategoryRecommendations(intentExtra)
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
            suggestions.adapter = SuggestionsAdapter(requireFragmentManager(), requireContext(), viewState, size, this, this)
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

    override fun openCategories() {
        val activity = requireActivity() as MainActivity
        activity.openSearch()
    }

    private fun refreshRecommendations() {
        if (!DeviceUtils.isConnected(requireContext())) {
            UiUtils.showToast(requireContext(), getString(com.hellmund.primetime.R.string.not_connected))
            return
        }

        presenter.forceRecommendationsDownload()
        presenter.downloadRecommendationsAsync()
    }

    override fun openSearch() {
        val intent = Intent(requireContext(), SearchActivity::class.java)
        startActivity(intent)
    }

    override fun openWatchlist() {
        val intent = Intent(requireContext(), WatchlistActivity::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        val intent = Intent(requireContext(), SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(com.hellmund.primetime.R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshRecommendations()
                true
            }
            R.id.action_settings -> {
                openSettings()
                true
            }
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onStop() {
        presenter.saveIndices()
        super.onStop()
    }

    companion object {

        private const val KEY_INTENT = "KEY_INTENT"

        @JvmStatic
        fun newInstance(intent: String? = null) = MainFragment().apply {
            arguments = Bundle().apply { putString(KEY_INTENT, intent) }
        }

    }

}
