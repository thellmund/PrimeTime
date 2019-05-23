package com.hellmund.primetime.ui.suggestions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectgenres.GenresRepository
import com.hellmund.primetime.ui.selectgenres.SelectGenresActivity
import com.hellmund.primetime.ui.settings.SettingsActivity
import com.hellmund.primetime.ui.shared.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui.suggestions.RecommendationsType.Personalized
import com.hellmund.primetime.ui.suggestions.details.MovieDetailsFragment
import com.hellmund.primetime.ui.suggestions.details.Rating
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.Math.round
import javax.inject.Inject
import javax.inject.Provider

class MainFragment : Fragment(), MainActivity.Reselectable {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var genresRepository: GenresRepository

    @Inject
    lateinit var viewModelProvider: Provider<MainViewModel>

    private val viewModel: MainViewModel by lazyViewModel { viewModelProvider }

    private val type: RecommendationsType by lazy {
        checkNotNull(arguments?.getParcelable<RecommendationsType>(KEY_RECOMMENDATIONS_TYPE))
    }

    private var isLoadingMore: Boolean = false

    private val adapter: MoviesAdapter by lazy {
        MoviesAdapter(
                imageLoader = imageLoader,
                onClick = this::openMovieDetails,
                onMenuClick = this::openRatingDialog
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.mainComponent()
                .type(type)
                .build()
                .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel.refresh() // TODO Move to ViewModel
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPersonalizationBanner()

        setupRecyclerView()
        setupFab()

        viewModel.viewState.observe(viewLifecycleOwner, this::render)
    }

    override fun onResume() {
        super.onResume()
        setToolbarSubtitle()
    }

    private fun setupPersonalizationBanner() {
        if (onboardingHelper.isFirstLaunch && type is Personalized) {
            banner.setOnClickListener {
                val intent = SelectGenresActivity.newIntent(requireContext())
                requireContext().startActivity(intent)
            }
            banner.show()
        } else {
            banner.dismiss()
        }
    }

    private fun setupRecyclerView() {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }

        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        recyclerView.onBottomReached {
            if (isLoadingMore.not()) {
                viewModel.refresh()
                isLoadingMore = true
            }
        }

        val spacing = round(resources.getDimension(R.dimen.default_space))
        recyclerView.addItemDecoration(EqualSpacingGridItemDecoration(spacing))
    }

    private fun setupFab() {
        filterFab.isVisible = type is Personalized && onboardingHelper.isFirstLaunch.not()
        filterFab.setOnClickListener { showFilterDialog() }
    }

    private fun render(viewState: MainViewState) {
        // TODO Move to ViewModel
        isLoadingMore = false

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
        val options = arrayOf(
                getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)
        )

        requireContext().showItemsDialog(
                titleResId = R.string.adjust_recommendations,
                items = options,
                onSelected = { index ->
                    val rating = if (index == 0) Rating.Like(movie) else Rating.Dislike(movie)
                    viewModel.handleRating(rating)
                }
        )
    }

    private fun setToolbarSubtitle() {
        val title = when (val type = type) {
            is Personalized -> getString(R.string.app_name)
            is RecommendationsType.BasedOnMovie -> type.title
            is RecommendationsType.NowPlaying -> getString(R.string.now_playing)
            is RecommendationsType.Upcoming -> getString(R.string.upcoming)
            is RecommendationsType.ByGenre -> type.genre.name
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    }

    private fun openSettings() {
        val intent = Intent(requireContext(), SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                openSettings()
                true
            }
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        val genres = genresRepository.preferredGenres.blockingFirst()
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
                    viewModel.filter(selectedGenres)
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
        ) = MainFragment().apply {
            arguments = bundleOf(KEY_RECOMMENDATIONS_TYPE to type)
        }

    }

}
