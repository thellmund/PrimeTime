package com.hellmund.primetime.recommendations.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.hellmund.primetime.core.AddressableActivity
import com.hellmund.primetime.core.DestinationFactory
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.core.FragmentArgs.KEY_RECOMMENDATIONS_TYPE
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.core.createIntent
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.model.RecommendationsType.Personalized
import com.hellmund.primetime.recommendations.R
import com.hellmund.primetime.recommendations.databinding.FragmentHomeBinding
import com.hellmund.primetime.recommendations.di.DaggerMoviesComponent
import com.hellmund.primetime.ui_common.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.ui_common.dialogs.RateMovieDialog
import com.hellmund.primetime.ui_common.dialogs.showMultiSelectDialog
import com.hellmund.primetime.ui_common.util.makeSceneTransitionAnimation
import com.hellmund.primetime.ui_common.util.onBottomReached
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

@ScrollAwareFragment
class HomeFragment : Fragment(), Reselectable {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var destinationFactory: DestinationFactory

    @Inject
    lateinit var viewModelProvider: Provider<HomeViewModel>

    private val viewModel: HomeViewModel by lazyViewModel { viewModelProvider }

    private val type: RecommendationsType by lazy {
        checkNotNull(arguments?.getParcelable<RecommendationsType>(KEY_RECOMMENDATIONS_TYPE))
    }

    private val adapter: MoviesAdapter by lazy {
        MoviesAdapter(
            imageLoader = imageLoader,
            onClick = this::openMovieDetails,
            onLongClick = this::openRatingDialog
        )
    }

    private lateinit var binding: FragmentHomeBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerMoviesComponent.builder()
            .recommendationsType(type)
            .core(coreComponent)
            .build()
            .inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.viewState.observe(viewLifecycleOwner, this::render)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        setupPersonalizationBanner()
        setupRecyclerView()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle()
    }

    private fun setupPersonalizationBanner() = with(binding.banner) {
        setOnClickListener(this@HomeFragment::openOnboarding)
        setOnDismissListener { viewModel.dispatch(ViewEvent.DismissPersonalizationBanner) }
    }

    private fun openOnboarding() {
        val intent = requireContext().createIntent(AddressableActivity.Onboarding)
        requireContext().startActivity(intent)
    }

    private fun setupRecyclerView() = with(binding) {
        swipeRefreshLayout.setColorSchemeResources(R.color.teal_500)
        swipeRefreshLayout.setOnRefreshListener { viewModel.dispatch(ViewEvent.LoadMovies(page = 1)) }

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        recyclerView.onBottomReached {
            viewModel.dispatch(ViewEvent.LoadMore)
        }

        val spacing = resources.getDimension(R.dimen.default_space).roundToInt()
        recyclerView.addItemDecoration(EqualSpacingGridItemDecoration(spacing))
    }

    private fun setupFab() = with(binding) {
        filterFab.doOnApplyWindowInsets { view, insets, initialState ->
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialState.margins.bottom + insets.systemWindowInsetBottom
            }
        }
    }

    private fun render(viewState: HomeViewState) {
        binding.swipeRefreshLayout.isRefreshing = viewState.isLoading

        if (viewState.isLoading.not()) {
            binding.swipeRefreshLayout.isEnabled = false
        }

        viewState.filtered?.let {
            adapter.update(it)
        } ?: adapter.update(viewState.data)

        binding.filterFab.setOnClickListener { showFilterDialog(viewState.preferredGenres) }
        binding.filterFab.isVisible = viewState.showFilterButton

        if (viewState.showPersonalizationBanner) {
            binding.banner.show()
        } else {
            binding.banner.dismiss()
        }
    }

    private fun openMovieDetails(movie: MovieViewEntity.Partial, startView: View) {
        val args = bundleOf(FragmentArgs.KEY_MOVIE to movie)
        val intent = destinationFactory.movieDetails(args)
        val options = requireActivity().makeSceneTransitionAnimation(startView, movie.id.toString())
        startActivity(intent, options.toBundle())
    }

    private fun openRatingDialog(movie: MovieViewEntity.Partial) {
        val header = getString(R.string.rate_movie, movie.title)
        RateMovieDialog
            .make(requireActivity())
            .setTitle(header)
            .setPositiveText(R.string.show_more_like_this)
            .setNegativeText(R.string.show_less_like_this)
            .onItemSelected { rating ->
                val ratedMovie = movie + rating
                viewModel.dispatch(ViewEvent.StoreRating(ratedMovie))
            }
            .show()
    }

    private fun initToolbar() {
        val toolbar = binding.toolbarContainer.toolbar
        toolbar.setTitle(R.string.app_name)
        val isInSearchTab = type !is Personalized

        if (isInSearchTab) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        } else {
            toolbar.inflateMenu(R.menu.menu_main)
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    openSettings()
                    true
                }
                else -> super.onOptionsItemSelected(menuItem)
            }
        }
    }

    private fun setToolbarTitle() {
        val title = when (val type = type) {
            is Personalized -> getString(R.string.app_name)
            is RecommendationsType.BasedOnMovie -> type.title
            is RecommendationsType.NowPlaying -> getString(R.string.now_playing)
            is RecommendationsType.Upcoming -> getString(R.string.upcoming)
            is RecommendationsType.ByGenre -> type.genre.name
        }
        binding.toolbarContainer.toolbar.title = title
    }

    private fun openSettings() {
        val intent = requireContext().createIntent(AddressableActivity.Settings)
        startActivity(intent)
    }

    private fun showFilterDialog(genres: List<Genre>) {
        val genreNames = genres
            .map { it.name }
            .toTypedArray()

        val checkedItems = when (val type = type) {
            is Personalized -> {
                val selectedGenres = type.genres ?: genres
                genres.map { selectedGenres.contains(it) }.toBooleanArray()
            }
            else -> genres.map { true }.toBooleanArray()
        }

        requireContext().showMultiSelectDialog(
            titleResId = R.string.filter_recommendations,
            items = genreNames,
            checkedItems = checkedItems,
            positiveResId = R.string.filter,
            onConfirmed = { selected ->
                val selectedGenres = genres.filterIndexed { i, _ -> selected.contains(i) }
                viewModel.dispatch(ViewEvent.Filter(selectedGenres))
            }
        )
    }

    override fun onReselected() {
        binding.recyclerView.smoothScrollToPosition(0)
    }

    companion object {

        @JvmStatic
        fun newInstance(
            type: RecommendationsType = Personalized()
        ) = HomeFragment().apply {
            arguments = bundleOf(KEY_RECOMMENDATIONS_TYPE to type)
        }
    }
}
