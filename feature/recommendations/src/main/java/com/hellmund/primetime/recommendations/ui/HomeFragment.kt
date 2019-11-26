package com.hellmund.primetime.recommendations.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellmund.primetime.core.AddressableActivity
import com.hellmund.primetime.core.FragmentArgs
import com.hellmund.primetime.core.FragmentArgs.KEY_RECOMMENDATIONS_TYPE
import com.hellmund.primetime.core.FragmentFactory
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.OnboardingHelper
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.core.createIntent
import com.hellmund.primetime.data.model.RecommendationsType
import com.hellmund.primetime.data.model.RecommendationsType.Personalized
import com.hellmund.primetime.data.repositories.GenresRepository
import com.hellmund.primetime.recommendations.R
import com.hellmund.primetime.recommendations.di.DaggerMoviesComponent
import com.hellmund.primetime.ui_common.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui_common.MovieViewEntity
import com.hellmund.primetime.ui_common.Reselectable
import com.hellmund.primetime.ui_common.dialogs.RateMovieDialog
import com.hellmund.primetime.ui_common.dialogs.showMultiSelectDialog
import com.hellmund.primetime.ui_common.util.onBottomReached
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import kotlinx.android.synthetic.main.fragment_home.banner
import kotlinx.android.synthetic.main.fragment_home.filterFab
import kotlinx.android.synthetic.main.fragment_home.recyclerView
import kotlinx.android.synthetic.main.fragment_home.swipeRefreshLayout
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

@ScrollAwareFragment
class HomeFragment : Fragment(), Reselectable {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var genresRepository: GenresRepository

    @Inject
    lateinit var viewModelProvider: Provider<HomeViewModel>

    @Inject
    lateinit var fragmentFactory: FragmentFactory

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerMoviesComponent.builder()
            .recommendationsType(type)
            .core(coreComponent)
            .build()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        setupPersonalizationBanner()
        setupRecyclerView()
        setupFab()
        viewModel.viewState.observe(viewLifecycleOwner, this::render)
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle()
    }

    private fun setupPersonalizationBanner() {
        if (onboardingHelper.isFirstLaunch && type is Personalized) {
            banner.setOnClickListener {
                val intent = requireContext().createIntent(AddressableActivity.Onboarding)
                requireContext().startActivity(intent)
            }
            banner.show()
        } else {
            banner.dismiss()
        }
    }

    private fun setupRecyclerView() {
        swipeRefreshLayout.setColorSchemeResources(R.color.teal_500)
        swipeRefreshLayout.setOnRefreshListener { viewModel.dispatch(Action.LoadMovies(page = 1)) }

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter
        recyclerView.onBottomReached {
            viewModel.dispatch(Action.LoadMore)
        }

        val spacing = resources.getDimension(R.dimen.default_space).roundToInt()
        recyclerView.addItemDecoration(EqualSpacingGridItemDecoration(spacing))
    }

    private fun setupFab() {
        filterFab.isVisible = type is Personalized && onboardingHelper.isFirstLaunch.not()
        filterFab.setOnClickListener {
            lifecycleScope.launch {
                showFilterDialog()
            }
        }
    }

    private fun render(viewState: HomeViewState) {
        swipeRefreshLayout.isRefreshing = viewState.isLoading

        if (viewState.isLoading.not()) {
            swipeRefreshLayout.isEnabled = false
        }

        viewState.filtered?.let {
            adapter.update(it)
        } ?: adapter.update(viewState.data)
    }

    private fun openMovieDetails(movie: MovieViewEntity) {
        val args = bundleOf(FragmentArgs.KEY_MOVIE to movie)
        val fragment = fragmentFactory.movieDetails(args) as BottomSheetDialogFragment
        fragment.show(requireFragmentManager(), fragment.tag)
    }

    private fun openRatingDialog(movie: MovieViewEntity) {
        val header = getString(R.string.rate_movie, movie.title)
        RateMovieDialog
            .make(requireActivity())
            .setTitle(header)
            .setPositiveText(R.string.show_more_like_this)
            .setNegativeText(R.string.show_less_like_this)
            .onItemSelected { rating ->
                val ratedMovie = movie.apply(rating)
                viewModel.dispatch(Action.StoreRating(ratedMovie))
            }
            .show()
    }

    private fun initToolbar() {
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
        toolbar.title = title
    }

    private fun openSettings() {
        val intent = requireContext().createIntent(AddressableActivity.Settings)
        startActivity(intent)
    }

    private suspend fun showFilterDialog() {
        val genres = genresRepository.getPreferredGenres()
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
                viewModel.dispatch(Action.Filter(selectedGenres))
            }
        )
    }

    override fun onReselected() {
        recyclerView.smoothScrollToPosition(0)
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
