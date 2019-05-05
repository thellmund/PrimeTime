package com.hellmund.primetime.ui.suggestions

import android.app.ProgressDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectstreamingservices.EqualHorizontalSpacingItemDecoration
import com.hellmund.primetime.ui.selectstreamingservices.StreamingService
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.fragment_movie_details.*
import kotlinx.android.synthetic.main.fragment_movie_suggestion.*
import java.lang.Math.round
import javax.inject.Inject
import javax.inject.Provider

class MovieDetailsFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<SuggestionsViewModel>

    private val viewModel: SuggestionsViewModel by lazyViewModel { viewModelProvider }

    private val movie: MovieViewEntity by lazy {
        checkNotNull(arguments?.getParcelable<MovieViewEntity>(KEY_MOVIE))
    }

    private val recommendationsAdapter: RecommendationsAdapter by lazy {
        RecommendationsAdapter(this::onRecommendationClicked)
    }

    private var progressDialog: ProgressDialog? = null

    override fun onAttach(context: Context) {
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
    ): View? = inflater.inflate(R.layout.fragment_movie_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillInContent()
        downloadPosters()

        backdropImageView.setOnClickListener { viewModel.loadTrailer() }
        moreInfoButton.setOnClickListener { viewModel.openImdb() }

        addToWatchlistButton.setOnClickListener { viewModel.addToWatchlist() }
        // TODO rating_button.setOnClickListener { openRatingDialog() }
    }

    private fun fillInContent() {
        titleTextView.text = movie.title
        descriptionTextView.text = movie.description
        genresTextView.text = movie.formattedGenres
        // rating.text = movie.formattedVoteAverage
        // release.text = movie.releaseYear

        recommendationsRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recommendationsRecyclerView.adapter = recommendationsAdapter
        recommendationsRecyclerView.setHasFixedSize(true)

        val spacing = round(resources.getDimension(R.dimen.small_space))
        recommendationsRecyclerView.addItemDecoration(EqualHorizontalSpacingItemDecoration(spacing))

        if (movie.hasAdditionalInformation) {
            // displayRuntime()
        } else {
            downloadAdditionalInformation()
        }

        downloadRecommendations()
    }

    private fun downloadAdditionalInformation() {
        viewModel.loadAdditionalInformation()
    }

    private fun downloadRecommendations() {
        viewModel.loadRecommendations()
    }

    private fun downloadPosters() {
        imageLoader.load(url = movie.posterUrl, into = posterImageView)
        imageLoader.load(url = movie.backdropUrl, into = backdropImageView)
    }

    private fun handleViewModelEvent(event: ViewModelEvent) {
        when (event) {
            is ViewModelEvent.TrailerLoading -> showDialog()
            is ViewModelEvent.TrailerLoaded -> openUrl(event.url)
            is ViewModelEvent.AdditionalInformationLoaded -> showMovieDetails(event.movie)
            is ViewModelEvent.ImdbLinkLoaded -> openUrl(event.url)
            is ViewModelEvent.RatingStored ->  onRatingStored()
            is ViewModelEvent.AddedToWatchlist -> onAddedToWatchlist()
            is ViewModelEvent.ShowRemoveFromWatchlistDialog -> displayRemoveDialog()
            is ViewModelEvent.RemovedFromWatchlist -> onRemovedFromWatchlist()
            is ViewModelEvent.WatchStatus -> updateWatchlistButton(event.watchStatus)
            is ViewModelEvent.StreamingServicesLoaded -> showStreamingServices(event.services)
            is ViewModelEvent.RecommendationsLoaded -> showRecommendations(event.movies)
        }
    }

    private fun showStreamingServices(services: List<StreamingService>) {
        // streamingContainer.isVisible = services.isNotEmpty()

        val text = services.map(StreamingService::name).joinToString(", ")
        // streamingTextView.text = getString(R.string.available_on_format_string, text)
    }

    private fun showRecommendations(movies: List<MovieViewEntity>) {
        progressBar.isVisible = movies.isEmpty()
        recommendationsRecyclerView.isVisible = movies.isNotEmpty()
        recommendationsAdapter.update(movies)
    }

    private fun onRecommendationClicked(movie: MovieViewEntity) {
        val fragment = newInstance(movie)
        fragment.show(requireFragmentManager(), fragment.tag)
    }

    private fun showDialog() {
        progressDialog = requireContext().showLoading(R.string.opening_trailer)
    }

    private fun openUrl(url: String) {
        progressDialog?.dismiss()
        requireContext().openUrl(url)
    }

    private fun onRatingStored() {
        updateWatchlistButton(Movie.WatchStatus.WATCHED)
    }

    private fun showMovieDetails(movie: MovieViewEntity) {
        // runtime.text = movie.formattedRuntime
    }

    private fun onAddedToWatchlist() {
        updateWatchlistButton(Movie.WatchStatus.ON_WATCHLIST)
    }

    private fun displayRemoveDialog() {
        requireContext().showCancelableDialog(
                messageResId = R.string.remove_from_watchlist_header,
                positiveResId = R.string.remove,
                onPositive = { viewModel.removeFromWatchlist() }
        )
    }

    private fun onRemovedFromWatchlist() {
        updateWatchlistButton(Movie.WatchStatus.NOT_WATCHED)
    }

    private fun updateWatchlistButton(watchStatus: Movie.WatchStatus) {
        setButtonText(watchStatus)
        setButtonColor(watchStatus)
    }

    private fun setButtonText(watchStatus: Movie.WatchStatus) {
        val textResId = when {
            watchStatus === Movie.WatchStatus.NOT_WATCHED -> R.string.add_to_watchlist
            watchStatus === Movie.WatchStatus.ON_WATCHLIST -> R.string.remove_from_watchlist
            else -> R.string.watched_it
        }
        addToWatchlistButton.setText(textResId)
    }

    private fun setButtonColor(watchStatus: Movie.WatchStatus) {
        val color = posterPaletteSwatch?.rgb ?: return
        val finalColor = when (watchStatus) {
            Movie.WatchStatus.NOT_WATCHED -> color
            else -> ColorUtils.darken(color)
        }
        addToWatchlistButton.background.setColorFilter(finalColor, PorterDuff.Mode.MULTIPLY)
    }

    private val posterPaletteSwatch: Palette.Swatch?
        get() {
            if (background == null || background.drawable == null) {
                return null
            }

            val cover = (posterImageView.drawable as BitmapDrawable).bitmap
            val palette = Palette.from(cover).generate()
            return palette.vibrantSwatch
        }

    companion object {

        private const val KEY_MOVIE = "KEY_MOVIE"

        fun newInstance(movie: MovieViewEntity): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = bundleOf(KEY_MOVIE to movie)
            }
        }

    }

}
