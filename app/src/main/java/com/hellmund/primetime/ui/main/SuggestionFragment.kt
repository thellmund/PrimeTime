package com.hellmund.primetime.ui.main

import android.app.ProgressDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.graphics.Palette
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.Movie.WatchStatus.NOT_WATCHED
import com.hellmund.primetime.data.model.Movie.WatchStatus.ON_WATCHLIST
import com.hellmund.primetime.data.model.MovieViewEntity
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.fragment_movie_suggestion.*
import javax.inject.Inject
import javax.inject.Provider

class SuggestionFragment : Fragment() {

    @Inject
    lateinit var viewModelProvider: Provider<SuggestionsViewModel>

    private val viewModel: SuggestionsViewModel by lazyViewModel { viewModelProvider }

    private val movie: MovieViewEntity by lazy {
        arguments?.getParcelable<MovieViewEntity>(KEY_MOVIE) ?: throw IllegalStateException()
    }

    private lateinit var viewPagerHost: ViewPagerHost

    private lateinit var progressDialog: ProgressDialog

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        injector.suggestionComponent()
                .movie(movie)
                .build()
                .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        viewModel.viewModelEvents.observe(this, this::handleViewModelEvent)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_movie_suggestion, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillInContent()
        downloadPoster()

        trailer_button.setOnClickListener { viewModel.loadTrailer() }
        more_info_button.setOnClickListener { viewModel.openImdb() }

        overlay.setOnClickListener { toggleAdditionalInformation() }
        show_more.setOnClickListener { toggleAdditionalInformation() }

        movie_add_watchlist_button.setOnClickListener { viewModel.addToWatchlist() }
        rating_button.setOnClickListener { openRatingDialog() }
    }

    private fun openRatingDialog() {
        val options = arrayOf(
                getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)
        )

        requireContext().showItems(
                titleResId = R.string.adjust_recommendations,
                items = options,
                onSelected = { index -> viewModel.handleRating(index) }
        )
    }

    private fun toggleAdditionalInformation() {
        val showDetails = infos_container.isVisible.not()
        toggleAdditionalInformation(showDetails)
    }

    private fun toggleAdditionalInformation(showDetails: Boolean) {
        val isLandscape = requireContext().isLandscapeMode

        val maxLines = when {
            showDetails -> Int.MAX_VALUE
            isLandscape -> 0
            else -> DEFAULT_LINES
        }

        movie_description.maxLines = maxLines
        genres_container.isVisible = showDetails
        infos_container.isVisible = showDetails
        action_buttons.isVisible = showDetails

        val rotation = if (showDetails) 180f else 0f
        show_more_icon.rotation = rotation

        val text = if (showDetails) R.string.show_less else R.string.show_more
        show_more_text.setText(text)
    }

    private fun fillInContent() {
        movie_title.text = movie.title
        movie_description.text = movie.description
        genres.text = movie.formattedGenres
        rating.text = movie.formattedVoteAverage
        release.text = movie.releaseYear

        if (movie.hasAdditionalInformation) {
            displayRuntime()
        } else {
            downloadAdditionalInformation()
        }
    }

    private fun displayRuntime() {
        // TODO
    }

    private fun downloadAdditionalInformation() {
        viewModel.loadAdditionalInformation()
    }

    private fun downloadPoster() {
        ImageLoader
                .with(requireContext())
                .load(
                        url = movie.posterUrl,
                        into = background,
                        onComplete = { progress_bar.isVisible = false }
                )
    }

    private fun handleViewModelEvent(event: ViewModelEvent) {
        when (event) {
            is ViewModelEvent.TrailerLoading -> showDialog()
            is ViewModelEvent.TrailerLoaded -> openUrl(event.url)
            is ViewModelEvent.AdditionalInformationLoaded -> showMovieDetails(event.movie)
            is ViewModelEvent.ImdbLinkLoaded -> openUrl(event.url)
            is ViewModelEvent.RatingStored -> viewPagerHost.scrollToNext()
            is ViewModelEvent.AddedToWatchlist -> onAddedToWatchlist()
            is ViewModelEvent.ShowRemoveFromWatchlistDialog -> displayRemoveDialog()
            is ViewModelEvent.RemovedFromWatchlist -> onRemovedFromWatchlist()
            is ViewModelEvent.WatchStatus -> updateWatchlistButton(event.watchStatus)
        }
    }

    private fun showDialog() {
        progressDialog = requireContext().showLoading(R.string.opening_trailer)
    }

    private fun openUrl(url: String) {
        requireContext().openUrl(url)
    }

    private fun showMovieDetails(movie: MovieViewEntity) {
        runtime.text = movie.formattedRuntime
    }

    private fun onAddedToWatchlist() {
        updateWatchlistButton(ON_WATCHLIST)
    }

    private fun displayRemoveDialog() {
        requireContext().showCancelable(
                messageResId = R.string.remove_from_watchlist_header,
                positiveResId = R.string.remove,
                onPositive = { viewModel.removeFromWatchlist() }
        )
    }

    private fun onRemovedFromWatchlist() {
        updateWatchlistButton(NOT_WATCHED)
        requireContext().showToast(R.string.watchlist_removed)
    }

    private fun updateWatchlistButton(watchStatus: Movie.WatchStatus) {
        setButtonText(watchStatus)
        setButtonColor(watchStatus)
    }

    private fun setButtonText(watchStatus: Movie.WatchStatus) {
        val textResId = when {
            watchStatus === NOT_WATCHED -> R.string.add_to_watchlist
            watchStatus === ON_WATCHLIST -> R.string.remove_from_watchlist
            else -> R.string.watched_it
        }
        movie_add_watchlist_button.setText(textResId)
    }

    private fun setButtonColor(watchStatus: Movie.WatchStatus) {
        val color = posterPaletteSwatch?.rgb ?: return
        val finalColor = when (watchStatus) {
            NOT_WATCHED -> color
            else -> ColorUtils.darken(color)
        }
        movie_add_watchlist_button.background.setColorFilter(finalColor, PorterDuff.Mode.MULTIPLY)
    }

    private val posterPaletteSwatch: Palette.Swatch?
        get() {
            if (background == null || background.drawable == null) {
                return null
            }

            val background = (background.drawable as BitmapDrawable).bitmap
            val palette = Palette.from(background).generate()
            return palette.vibrantSwatch
        }

    interface ViewPagerHost {
        fun scrollToNext()
        fun scrollToPrevious()
    }

    companion object {

        private const val KEY_MOVIE = "KEY_MOVIE"
        private const val DEFAULT_LINES = 2

        fun newInstance(
                movie: MovieViewEntity,
                viewPagerHost: ViewPagerHost
        ): SuggestionFragment {
            return SuggestionFragment().apply {
                arguments = Bundle().apply { putParcelable(KEY_MOVIE, movie) }
                this.viewPagerHost = viewPagerHost
            }
        }

    }

}
