package com.hellmund.primetime.moviedetails.ui

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
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellmund.api.model.Review
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.Movie.WatchStatus.ON_WATCHLIST
import com.hellmund.primetime.moviedetails.R
import com.hellmund.primetime.moviedetails.databinding.FragmentMovieDetailsBinding
import com.hellmund.primetime.moviedetails.di.DaggerMovieDetailsComponent
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.PartialMovieViewEntity
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.ui_common.dialogs.showLoading
import com.hellmund.primetime.ui_common.util.navigator
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import com.hellmund.primetime.ui_common.viewmodel.observeSingleEvents
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

class MovieDetailsFragment : Fragment(), Reselectable {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<MovieDetailsViewModel>

    private val viewModel: MovieDetailsViewModel by lazyViewModel { viewModelProvider }

    private val movie: MovieViewEntity by lazy {
        checkNotNull(requireArguments().getParcelable<MovieViewEntity>(FragmentArgs.KEY_MOVIE))
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
        viewModel.viewState.observe(viewLifecycleOwner, this::render)
        viewModel.navigationResults.observeSingleEvents(viewLifecycleOwner, this::navigate)
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

        downloadPosters()
        setupSimilarMoviesList()
        setupReviewsList()

        binding.backButton.setOnClickListener {
            navigator.pop()
        }

        binding.backdropImageView.setOnClickListener { viewModel.dispatch(ViewEvent.OpenTrailer) }
        binding.moreInfoButton.setOnClickListener { viewModel.dispatch(ViewEvent.OpenImdb) }

        binding.addToWatchlistButton.setOnClickListener {
            viewModel.dispatch(ViewEvent.AddToWatchlist)
        }

        binding.removeFromWatchlistButton.setOnClickListener {
            viewModel.dispatch(ViewEvent.RemoveFromWatchlist)
        }
    }

    private fun setupSimilarMoviesList() = with(binding) {
        val spacing = resources.getDimension(R.dimen.small_space).roundToInt()
        recommendationsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recommendationsRecyclerView.adapter = recommendationsAdapter
        recommendationsRecyclerView.addItemDecoration(EqualHorizontalSpacingItemDecoration(spacing))
    }

    private fun setupReviewsList() = with(binding) {
        val spacing = resources.getDimension(R.dimen.small_space).roundToInt()
        reviewsRecyclerView.adapter = reviewsAdapter
        reviewsRecyclerView.setHasFixedSize(true)
        reviewsRecyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))
    }

    private fun render(viewState: MovieDetailsViewState) = with(binding) {
        fillInContent(viewState.movie)

        viewState.color?.let { onMovieColorLoaded(it) }
        viewState.recommendations?.let { showRecommendations(it) }
        viewState.reviews?.let { showReviews(it) }

        if (viewState.isTrailerLoading) {
            progressDialog = requireContext().showLoading(R.string.opening_trailer)
        } else {
            progressDialog?.dismiss()
        }

        updateWatchlistButton(viewState.watchStatus)
    }

    private fun navigate(result: NavigationResult) {
        when (result) {
            is NavigationResult.OpenImdb -> openUrl(result.url)
            is NavigationResult.OpenSimilarMovie -> openClickedRecommendation(result.viewEntity)
            is NavigationResult.OpenTrailer -> openUrl(result.url)
        }
    }

    private fun fillInContent(movie: MovieViewEntity) = with(binding) {
        titleTextView.text = movie.title
        descriptionTextView.text = movie.description
        genresTextView.text = movie.formattedGenres

        releaseTextView.text = movie.releaseYear
        durationTextView.text = movie.formattedRuntime
        ratingTextView.text = movie.formattedVoteAverage
        votesTextView.text = movie.formattedVoteCount
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

    private fun showRecommendations(movies: List<PartialMovieViewEntity>) {
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

    private fun onRecommendationClicked(movie: PartialMovieViewEntity) {
        viewModel.dispatch(ViewEvent.RecommendationClicked(movie.id))
    }

    private fun openClickedRecommendation(movie: MovieViewEntity) {
        navigator.addFragment(newInstance(movie))
    }

    private fun openUrl(url: String) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(requireContext(), url.toUri())
    }

    private fun updateWatchlistButton(watchStatus: Movie.WatchStatus) {
        binding.addToWatchlistButton.isVisible = watchStatus != ON_WATCHLIST
        binding.removeFromWatchlistButton.isVisible = watchStatus == ON_WATCHLIST
    }

    private fun onImageLoaded(drawable: Drawable) {
        val cover = (drawable as BitmapDrawable).bitmap
        viewModel.dispatch(ViewEvent.LoadColorPalette(cover))
    }

    private fun onMovieColorLoaded(color: Int) {
        val colorStateList = ColorStateList.valueOf(color)
        binding.addToWatchlistButton.backgroundTintList = colorStateList
        binding.removeFromWatchlistButton.strokeColor = colorStateList
    }

    override fun onReselected() {
        navigator.pop()
    }

    companion object {

        fun newInstance(movie: MovieViewEntity): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = bundleOf(FragmentArgs.KEY_MOVIE to movie)
            }
        }
    }
}
