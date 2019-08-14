package com.hellmund.primetime.moviedetails.ui

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
import com.hellmund.api.model.Review
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.moviedetails.R
import com.hellmund.primetime.moviedetails.di.MovieDetailsComponent
import com.hellmund.primetime.moviedetails.util.EqualHorizontalSpacingItemDecoration
import com.hellmund.primetime.moviedetails.util.EqualSpacingItemDecoration
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.dialogs.showLoading
import com.hellmund.primetime.ui_common.util.ImageLoader
import com.hellmund.primetime.ui_common.util.observe
import com.hellmund.primetime.ui_common.util.openUrl
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
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
        checkNotNull(arguments?.getParcelable<MovieViewEntity>(FragmentArgs.KEY_MOVIE))
    }

    private val recommendationsAdapter: RecommendationsAdapter by lazy {
        RecommendationsAdapter(
            imageLoader = imageLoader,
            onClick = this::onRecommendationClicked
        )
    }

    private val reviewsAdapter = ReviewsAdapter()

    @Suppress("DEPRECATION")
    private var progressDialog: ProgressDialog? = null

    override fun getTheme() = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as Injector)
            .movieDetailsComponent()
            .create(movie)
            .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.uiEvents.observe(this, this::handleViewModelEvent)
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

        backdropImageView.setOnClickListener { viewModel.dispatch(Action.OpenTrailer) }
        moreInfoButton.setOnClickListener { viewModel.dispatch(Action.OpenImdb) }

        addToWatchlistButton.setOnClickListener { viewModel.dispatch(Action.AddToWatchlist) }
        removeFromWatchlistButton.setOnClickListener { viewModel.dispatch(Action.RemoveFromWatchlist) }
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
    }

    private fun downloadPosters() {
        imageLoader.load(url = movie.posterUrl, into = posterImageView, onComplete = this::onImageLoaded)
        imageLoader.load(url = movie.backdropUrl, into = backdropImageView)
    }

    private fun handleViewModelEvent(event: UiEvent) {
        when (event) {
            is UiEvent.TrailerLoading -> showDialog()
            is UiEvent.TrailerLoaded -> openUrl(event.url)
            is UiEvent.AdditionalInformationLoaded -> showMovieDetails(event.movie)
            is UiEvent.ImdbLinkLoaded -> openUrl(event.url)
            is UiEvent.AddedToWatchlist -> onAddedToWatchlist()
            is UiEvent.RemovedFromWatchlist -> onRemovedFromWatchlist()
            is UiEvent.WatchStatus -> updateWatchlistButton(event.watchStatus)
            is UiEvent.RecommendationsLoaded -> showRecommendations(event.movies)
            is UiEvent.ReviewsLoaded -> showReviews(event.reviews)
            is UiEvent.ColorPaletteLoaded -> onCoverPaletteLoaded(event.palette)
        }
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
        reviewsRecyclerView.isVisible = reviews.isNotEmpty()
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
        viewModel.dispatch(Action.LoadColorPalette(cover))
    }

    private fun onCoverPaletteLoaded(palette: Palette) {
        val color = palette.mutedSwatch?.rgb ?: return
        val colorStateList = ColorStateList.valueOf(color)
        addToWatchlistButton.backgroundTintList = colorStateList
        removeFromWatchlistButton.strokeColor = colorStateList
    }

    interface Injector {
        fun movieDetailsComponent(): MovieDetailsComponent.Factory
    }

    companion object {

        fun newInstance(movie: MovieViewEntity): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = bundleOf(FragmentArgs.KEY_MOVIE to movie)
            }
        }

    }

}
