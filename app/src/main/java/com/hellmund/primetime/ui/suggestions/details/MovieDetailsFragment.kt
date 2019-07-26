package com.hellmund.primetime.ui.suggestions.details

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectstreamingservices.EqualHorizontalSpacingItemDecoration
import com.hellmund.primetime.ui.selectstreamingservices.StreamingService
import com.hellmund.primetime.ui.shared.EqualSpacingItemDecoration
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.RecommendationsAdapter
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.observe
import com.hellmund.primetime.utils.openUrl
import com.hellmund.primetime.utils.showLoading
import kotlinx.android.synthetic.main.fragment_movie_details.addToWatchlistButton
import kotlinx.android.synthetic.main.fragment_movie_details.backdropImageView
import kotlinx.android.synthetic.main.fragment_movie_details.descriptionTextView
import kotlinx.android.synthetic.main.fragment_movie_details.durationTextView
import kotlinx.android.synthetic.main.fragment_movie_details.genresTextView
import kotlinx.android.synthetic.main.fragment_movie_details.moreInfoButton
import kotlinx.android.synthetic.main.fragment_movie_details.noRecommendationsPlaceholder
import kotlinx.android.synthetic.main.fragment_movie_details.noReviewsPlaceholder
import kotlinx.android.synthetic.main.fragment_movie_details.posterImageView
import kotlinx.android.synthetic.main.fragment_movie_details.ratingTextView
import kotlinx.android.synthetic.main.fragment_movie_details.recommendationsProgressBar
import kotlinx.android.synthetic.main.fragment_movie_details.recommendationsRecyclerView
import kotlinx.android.synthetic.main.fragment_movie_details.releaseTextView
import kotlinx.android.synthetic.main.fragment_movie_details.removeFromWatchlistButton
import kotlinx.android.synthetic.main.fragment_movie_details.reviewsProgressBar
import kotlinx.android.synthetic.main.fragment_movie_details.reviewsRecyclerView
import kotlinx.android.synthetic.main.fragment_movie_details.titleTextView
import kotlinx.android.synthetic.main.fragment_movie_details.votesTextView
import java.lang.Math.round
import javax.inject.Inject
import javax.inject.Provider

class MovieDetailsFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<MovieDetailsViewModel>

    private val viewModel: MovieDetailsViewModel by lazyViewModel { viewModelProvider }

    private val movie: MovieViewEntity by lazy {
        checkNotNull(arguments?.getParcelable<MovieViewEntity>(KEY_MOVIE))
    }

    private val recommendationsAdapter: RecommendationsAdapter by lazy {
        RecommendationsAdapter(
            imageLoader = imageLoader,
            onClick = this::onRecommendationClicked
        )
    }

    private val reviewsAdapter: ReviewsAdapter by lazy { ReviewsAdapter() }

    private var progressDialog: ProgressDialog? = null

    override fun getTheme() = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.movieDetailsComponent()
            .create(movie)
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
        removeFromWatchlistButton.setOnClickListener { viewModel.removeFromWatchlist() }
    }

    private fun fillInContent() {
        titleTextView.text = movie.title
        descriptionTextView.text = movie.description
        genresTextView.text = movie.formattedGenres

        releaseTextView.text = movie.releaseYear
        durationTextView.text = movie.formattedRuntime
        ratingTextView.text = movie.formattedVoteAverage
        votesTextView.text = movie.formattedVoteCount

        val spacing = round(resources.getDimension(R.dimen.small_space))

        recommendationsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recommendationsRecyclerView.adapter = recommendationsAdapter
        recommendationsRecyclerView.addItemDecoration(EqualHorizontalSpacingItemDecoration(spacing))

        reviewsRecyclerView.adapter = reviewsAdapter
        reviewsRecyclerView.setHasFixedSize(true)
        reviewsRecyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))

        if (movie.hasAdditionalInformation.not()) {
            viewModel.loadAdditionalInformation()
        }

        viewModel.loadRecommendations()
        viewModel.loadReviews()
    }

    private fun downloadPosters() {
        imageLoader.load(url = movie.posterUrl, into = posterImageView, onComplete = this::onImageLoaded)
        imageLoader.load(url = movie.backdropUrl, into = backdropImageView)
    }

    private fun handleViewModelEvent(event: ViewModelEvent) {
        when (event) {
            is ViewModelEvent.TrailerLoading -> showDialog()
            is ViewModelEvent.TrailerLoaded -> openUrl(event.url)
            is ViewModelEvent.AdditionalInformationLoaded -> showMovieDetails(event.movie)
            is ViewModelEvent.ImdbLinkLoaded -> openUrl(event.url)
            is ViewModelEvent.RatingStored -> onRatingStored()
            is ViewModelEvent.AddedToWatchlist -> onAddedToWatchlist()
            is ViewModelEvent.RemovedFromWatchlist -> onRemovedFromWatchlist()
            is ViewModelEvent.WatchStatus -> updateWatchlistButton(event.watchStatus)
            is ViewModelEvent.StreamingServicesLoaded -> showStreamingServices(event.services)
            is ViewModelEvent.RecommendationsLoaded -> showRecommendations(event.movies)
            is ViewModelEvent.ReviewsLoaded -> showReviews(event.reviews)
        }
    }

    private fun showStreamingServices(services: List<StreamingService>) {
        // streamingContainer.isVisible = services.isNotEmpty()

        val text = services.map(StreamingService::name).joinToString(", ")
        // streamingTextView.text = getString(R.string.available_on_format_string, text)
    }

    private fun showRecommendations(movies: List<MovieViewEntity>) {
        recommendationsProgressBar.isVisible = false
        noRecommendationsPlaceholder.isVisible = movies.isEmpty()
        recommendationsRecyclerView.isVisible = movies.isNotEmpty()
        recommendationsAdapter.update(movies)
    }

    private fun showReviews(reviews: List<Review>) {
        reviewsProgressBar.isVisible = false
        noReviewsPlaceholder.isVisible = reviews.isEmpty()
        reviewsProgressBar.isVisible = reviews.isNotEmpty()
        reviewsAdapter.update(reviews)
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
        durationTextView.text = movie.formattedRuntime
    }

    private fun onAddedToWatchlist() {
        updateWatchlistButton(Movie.WatchStatus.ON_WATCHLIST)
    }

    private fun onRemovedFromWatchlist() {
        updateWatchlistButton(Movie.WatchStatus.NOT_WATCHED)
    }

    private fun updateWatchlistButton(watchStatus: Movie.WatchStatus) {
        addToWatchlistButton.isVisible = watchStatus != Movie.WatchStatus.ON_WATCHLIST
        removeFromWatchlistButton.isVisible = watchStatus == Movie.WatchStatus.ON_WATCHLIST
    }

    private fun onImageLoaded(drawable: Drawable) {
        val cover = (drawable as BitmapDrawable).bitmap
        Palette.from(cover).generate { palette ->
            palette?.let {
                val color = it.mutedSwatch?.rgb ?: return@let
                val colorStateList = ColorStateList.valueOf(color)
                addToWatchlistButton.backgroundTintList = colorStateList
                removeFromWatchlistButton.strokeColor = colorStateList
            }
        }
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
