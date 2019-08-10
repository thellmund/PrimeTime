package com.hellmund.primetime.ui.suggestions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hellmund.primetime.R
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.data.GenresRepository
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.onboarding.OnboardingActivity
import com.hellmund.primetime.onboarding.OnboardingHelper
import com.hellmund.primetime.ui.MainActivity
import com.hellmund.primetime.settings.ui.SettingsActivity
import com.hellmund.primetime.ui.shared.RateMovieDialog
import com.hellmund.primetime.ui.shared.ScrollAwareFragment
import com.hellmund.primetime.ui.suggestions.RecommendationsType.Personalized
import com.hellmund.primetime.ui.suggestions.details.MovieDetailsFragment
import com.hellmund.primetime.ui_common.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui_common.lazyViewModel
import com.hellmund.primetime.ui_common.observe
import com.hellmund.primetime.ui_common.onBottomReached
import com.hellmund.primetime.ui_common.showMultiSelectDialog
import kotlinx.android.synthetic.main.fragment_main.banner
import kotlinx.android.synthetic.main.fragment_main.filterFab
import kotlinx.android.synthetic.main.fragment_main.recyclerView
import kotlinx.android.synthetic.main.fragment_main.shimmerLayout
import kotlinx.android.synthetic.main.fragment_main.swipeRefreshLayout
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import java.lang.Math.round
import javax.inject.Inject
import javax.inject.Provider

@FlowPreview
@ExperimentalCoroutinesApi
@ScrollAwareFragment
class HomeFragment : Fragment(), MainActivity.Reselectable {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var genresRepository: GenresRepository

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.mainComponent()
            .create(type)
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false)

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
                val intent = OnboardingActivity.newIntent(requireContext())
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

        val spacing = round(resources.getDimension(R.dimen.default_space))
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

        if (viewState.isLoading.not()) {
            shimmerLayout.stopShimmer()
            shimmerLayout.setShimmer(null)
        }

        // TODO Error handling
    }

    private fun openMovieDetails(movie: MovieViewEntity) {
        val fragment = MovieDetailsFragment.newInstance(movie)
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

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val isInSearchTab = bottomNav.selectedItemId == R.id.search

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
        val intent = Intent(requireContext(), SettingsActivity::class.java)
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

        private const val KEY_RECOMMENDATIONS_TYPE = "KEY_RECOMMENDATIONS_TYPE"

        @JvmStatic
        fun newInstance(
            type: RecommendationsType = Personalized()
        ) = HomeFragment().apply {
            arguments = bundleOf(KEY_RECOMMENDATIONS_TYPE to type)
        }

    }

}
