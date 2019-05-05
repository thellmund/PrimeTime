package com.hellmund.primetime.ui.suggestions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.search.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui.selectgenres.SelectGenresActivity
import com.hellmund.primetime.ui.settings.SettingsActivity
import com.hellmund.primetime.ui.suggestions.RecommendationsType.Personalized
import com.hellmund.primetime.utils.OnboardingHelper
import com.hellmund.primetime.utils.observe
import com.hellmund.primetime.utils.showItemsDialog
import com.hellmund.primetime.utils.showSingleSelectDialog
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.support.v4.runOnUiThread
import java.lang.Math.round
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.concurrent.schedule

class MainFragment : Fragment(), MainActivity.Reselectable, SuggestionFragment.ViewPagerHost {

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var viewModelProvider: Provider<MainViewModel>

    private val viewModel: MainViewModel by lazyViewModel { viewModelProvider }

    private val type: RecommendationsType by lazy {
        arguments?.getParcelable(KEY_RECOMMENDATIONS_TYPE) as RecommendationsType
    }

    private val adapter2: SuggestionsAdapter2 by lazy {
        SuggestionsAdapter2(this::openMovieDetails, this::openRatingDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel.refresh(type)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarSubtitle(type)
        setupPersonalizationBanner()
        setupRecyclerView()
        viewModel.viewState.observe(this, this::render)
    }

    private fun setupPersonalizationBanner() {
        if (onboardingHelper.isFirstLaunch) {
            banner.setOnClickListener {
                val intent = SelectGenresActivity.newIntent(requireContext())
                requireContext().startActivity(intent)
            }
            banner.show()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter2

        val spacing = round(resources.getDimension(R.dimen.default_space))
        recyclerView.addItemDecoration(EqualSpacingGridItemDecoration(spacing))
    }

    private fun render(viewState: MainViewState) {
        adapter2.update(viewState.data)

        progressBar.isVisible = viewState.isLoading
        recyclerView.isVisible = viewState.isLoading.not()

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

    private fun retry() {
        // TODO: Use current RecommendationsType
        viewModel.refresh(Personalized)
    }

    private fun setToolbarSubtitle(type: RecommendationsType) {
        val title = when (type) {
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

    override fun scrollToPrevious() {
        // suggestions.scrollToPrevious()
    }

    override fun scrollToNext() {
        Timer().schedule(350) {
            runOnUiThread {
                // suggestions.scrollToNext()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (type == Personalized) {
            inflater.inflate(R.menu.menu_main, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
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
        val checked = 0 // TODO
        requireContext().showSingleSelectDialog(
                titleResId = R.string.filter_recommendations,
                choices = arrayOf("All", "From my streaming services"),
                checked = checked,
                positiveResId = R.string.done,
                onSelected = { selected ->
                    if (selected != checked) {
                        applyStreamingFilter(selected == 1)
                    }
                }
        )
    }

    private fun applyStreamingFilter(limitToStreamingServices: Boolean) {

    }

    override fun onReselected() {
        recyclerView.smoothScrollToPosition(0)
    }

    companion object {

        private const val KEY_RECOMMENDATIONS_TYPE = "KEY_RECOMMENDATIONS_TYPE"

        @JvmStatic
        fun newInstance(
                type: RecommendationsType = Personalized
        ) = MainFragment().apply {
            arguments = Bundle().apply { putParcelable(KEY_RECOMMENDATIONS_TYPE, type) }
        }

    }

}
