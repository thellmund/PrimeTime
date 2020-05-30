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
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hellmund.api.model.Review
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.di.coreComponent
import com.hellmund.primetime.core.navigation.DestinationFactory
import com.hellmund.primetime.core.navigation.DestinationsArgs
import com.hellmund.primetime.data.model.Movie
import com.hellmund.primetime.data.model.Movie.WatchStatus.OnWatchlist
import com.hellmund.primetime.moviedetails.R
import com.hellmund.primetime.moviedetails.databinding.FragmentMovieDetailsBinding
import com.hellmund.primetime.moviedetails.di.DaggerMovieDetailsComponent
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.dialogs.showLoading
import com.hellmund.primetime.ui_common.util.makeSceneTransitionAnimation
import com.hellmund.primetime.ui_common.util.observeWhenCreated
import com.hellmund.primetime.ui_common.util.updateBottomPaddingForFullscreenLayout
import com.hellmund.primetime.ui_common.util.updateTopMarginForFullscreenLayout
import com.hellmund.primetime.ui_common.viewmodel.handle
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

class MovieDetailsFragment : Fragment() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var destinationFactory: DestinationFactory

    @Inject
    lateinit var viewModelProvider: Provider<MovieDetailsViewModel>

    private val viewModel: MovieDetailsViewModel by lazyViewModel { viewModelProvider }

    private val movie: MovieViewEntity.Partial by lazy {
        val intent = requireActivity().intent
        checkNotNull(intent.getParcelableExtra<MovieViewEntity.Partial>(DestinationsArgs.KEY_MOVIE))
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

        viewModel.state.observeWhenCreated(lifecycleScope, this::render)
        viewModel.viewEffects.handle(viewLifecycleOwner, this::handleViewEffect)
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
            requireActivity().onBackPressed()
        }

        binding.backdropImageView.setOnClickListener {
            viewModel.handleViewEvent(ViewEvent.OpenTrailer)
        }

        binding.moreInfoButton.setOnClickListener {
            viewModel.handleViewEvent(ViewEvent.OpenImdb)
        }

        binding.addToWatchlistButton.setOnClickListener {
            viewModel.handleViewEvent(ViewEvent.AddToWatchlist)
        }

        binding.removeFromWatchlistButton.setOnClickListener {
            viewModel.handleViewEvent(ViewEvent.RemoveFromWatchlist)
        }

        binding.backButton.updateTopMarginForFullscreenLayout()
        binding.playButton.updateTopMarginForFullscreenLayout()
        binding.contentContainer.updateBottomPaddingForFullscreenLayout()
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

    private fun handleViewEffect(effect: ViewEffect) {
        when (effect) {
            is ViewEffect.OpenImdb -> openUrl(effect.url)
            is ViewEffect.OpenTrailer -> openUrl(effect.url)
            is ViewEffect.ShowError -> {
                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                if (effect.closeScreen) {
                    requireActivity().onBackPressed()
                }
            }
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

    private fun showRecommendations(movies: List<MovieViewEntity.Partial>) {
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

    private fun onRecommendationClicked(movie: MovieViewEntity.Partial, startView: View) {
        val intent = destinationFactory.movieDetails(movie)
        val options = requireActivity().makeSceneTransitionAnimation(startView, movie.id.toString())
        startActivity(intent, options.toBundle())
    }

    private fun openUrl(url: String) {
        CustomTabsIntent.Builder()
            .build()
            .launchUrl(requireContext(), url.toUri())
    }

    private fun updateWatchlistButton(watchStatus: Movie.WatchStatus) {
        binding.addToWatchlistButton.isVisible = watchStatus != OnWatchlist
        binding.removeFromWatchlistButton.isVisible = watchStatus == OnWatchlist
    }

    private fun onImageLoaded(drawable: Drawable) {
        val cover = (drawable as BitmapDrawable).bitmap
        viewModel.handleViewEvent(ViewEvent.LoadColorPalette(cover))
    }

    private fun onMovieColorLoaded(color: Int) {
        val colorStateList = ColorStateList.valueOf(color)
        binding.addToWatchlistButton.backgroundTintList = colorStateList
        binding.removeFromWatchlistButton.strokeColor = colorStateList
    }

    companion object {
        fun newInstance() = MovieDetailsFragment()
    }
}
