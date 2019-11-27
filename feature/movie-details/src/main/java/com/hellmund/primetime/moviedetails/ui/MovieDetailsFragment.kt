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
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellmund.api.model.Review
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.Movie.WatchStatus.ON_WATCHLIST
import com.hellmund.primetime.moviedetails.R
import com.hellmund.primetime.moviedetails.databinding.FragmentMovieDetailsBinding
import com.hellmund.primetime.moviedetails.di.DaggerMovieDetailsComponent
import com.hellmund.primetime.moviedetails.util.EqualHorizontalSpacingItemDecoration
import com.hellmund.primetime.moviedetails.util.EqualSpacingItemDecoration
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.dialogs.showLoading
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

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

    private lateinit var binding: FragmentMovieDetailsBinding

    override fun getTheme() = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerMovieDetailsComponent.builder()
            .core(coreComponent)
            .movie(movie)
            .build()
            .inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.uiEvents.observe(viewLifecycleOwner, this::handleViewModelEvent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillInContent()
        downloadPosters()

        binding.backdropImageView.setOnClickListener { viewModel.dispatch(Action.OpenTrailer) }
        binding.moreInfoButton.setOnClickListener { viewModel.dispatch(Action.OpenImdb) }

        binding.addToWatchlistButton.setOnClickListener {
            viewModel.dispatch(Action.AddToWatchlist)
        }

        binding.removeFromWatchlistButton.setOnClickListener {
            viewModel.dispatch(Action.RemoveFromWatchlist)
        }
    }

    private fun fillInContent() = with(binding) {
        titleTextView.text = movie.title
        descriptionTextView.text = movie.description
        genresTextView.text = movie.formattedGenres

        releaseTextView.text = movie.releaseYear
        durationTextView.text = movie.formattedRuntime
        ratingTextView.text = movie.formattedVoteAverage
        votesTextView.text = movie.formattedVoteCount

        val spacing = resources.getDimension(R.dimen.small_space).roundToInt()

        recommendationsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recommendationsRecyclerView.adapter = recommendationsAdapter
        recommendationsRecyclerView.addItemDecoration(EqualHorizontalSpacingItemDecoration(spacing))

        reviewsRecyclerView.adapter = reviewsAdapter
        reviewsRecyclerView.setHasFixedSize(true)
        reviewsRecyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))
    }

    private fun downloadPosters() {
        imageLoader.load(
            url = movie.posterUrl,
            into = binding.posterImageView,
            onComplete = this::onImageLoaded
        )

        imageLoader.load(
            url = movie.backdropUrl,
            into = binding.backdropImageView
        )
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
        binding.recommendationsProgressBar.isVisible = false
        binding.noRecommendationsPlaceholder.isVisible = movies.isEmpty()
        binding.recommendationsRecyclerView.isVisible = movies.isNotEmpty()
        recommendationsAdapter.update(movies)
    }

    private fun showReviews(reviews: List<Review>) {
        binding.reviewsProgressBar.isVisible = false
        binding.noReviewsPlaceholder.isVisible = reviews.isEmpty()
        binding.reviewsRecyclerView.isVisible = reviews.isNotEmpty()
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
        val color = ContextCompat.getColor(requireContext(), R.color.grey_900)
        CustomTabsIntent.Builder()
            .setToolbarColor(color)
            .build()
            .launchUrl(requireContext(), url.toUri())
    }

    private fun showMovieDetails(movie: MovieViewEntity) {
        binding.durationTextView.text = movie.formattedRuntime
    }

    private fun onAddedToWatchlist() {
        updateWatchlistButton(ON_WATCHLIST)
    }

    private fun onRemovedFromWatchlist() {
        updateWatchlistButton(Movie.WatchStatus.NOT_WATCHED)
    }

    private fun updateWatchlistButton(watchStatus: Movie.WatchStatus) {
        binding.addToWatchlistButton.isVisible = watchStatus != ON_WATCHLIST
        binding.removeFromWatchlistButton.isVisible = watchStatus == ON_WATCHLIST
    }

    private fun onImageLoaded(drawable: Drawable) {
        val cover = (drawable as BitmapDrawable).bitmap
        viewModel.dispatch(Action.LoadColorPalette(cover))
    }

    private fun onCoverPaletteLoaded(palette: Palette) {
        val color = palette.mutedSwatch?.rgb ?: return
        val colorStateList = ColorStateList.valueOf(color)
        binding.addToWatchlistButton.backgroundTintList = colorStateList
        binding.removeFromWatchlistButton.strokeColor = colorStateList
    }

    companion object {

        fun newInstance(movie: MovieViewEntity): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = bundleOf(FragmentArgs.KEY_MOVIE to movie)
            }
        }
    }
}
