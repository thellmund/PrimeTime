package com.hellmund.primetime.main

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.hellmund.primetime.R
import com.hellmund.primetime.api.ApiClient
import com.hellmund.primetime.database.PrimeTimeDatabase
import com.hellmund.primetime.history.HistoryRepository
import com.hellmund.primetime.main.RecommendationsType.Personalized
import com.hellmund.primetime.settings.SettingsActivity
import com.hellmund.primetime.utils.*
import com.hellmund.primetime.watchlist.WatchlistRepository
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import java.lang.Math.round

class MainFragment : Fragment(), MainActivity.Reselectable, SuggestionFragment.ViewPagerHost {

    private val genreProvider: GenresProvider by lazy {
        RealGenresProvider(defaultSharedPreferences)
    }

    private val repository: MoviesRepository by lazy {
        MoviesRepository(ApiClient.instance, genreProvider)
    }

    private val viewModel: MainViewModel by lazy {
        val historyRepo = HistoryRepository(PrimeTimeDatabase.getInstance(requireContext()))
        val watchlistRepo = WatchlistRepository(PrimeTimeDatabase.getInstance(requireContext()))
        val rankingProcessor = MovieRankingProcessor(historyRepo, watchlistRepo)
        val factory = MainViewModel.Factory(repository, rankingProcessor)
        when (type) {
            Personalized -> ViewModelProviders.of(requireActivity(), factory).get(MainViewModel::class.java)
            else -> ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
        }
    }

    private val type: RecommendationsType by lazy {
        arguments?.getParcelable(KEY_RECOMMENDATIONS_TYPE) as RecommendationsType
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
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarSubtitle(type)
        viewModel.viewState.observe(this, this::render)
    }

    private fun render(viewState: MainViewState) {
        val margin = round(resources.getDimension(R.dimen.default_space))

        val adapter = SuggestionsAdapter(requireFragmentManager(), this, this::retry)
        adapter.movies = viewState.data
        adapter.pageWidth = if (requireContext().isLandscapeMode) 0.5f else 1.0f

        suggestions.adapter = adapter
        suggestions.pageMargin = margin

        progressBar.visibility = if (viewState.isLoading) View.VISIBLE else View.GONE
        suggestions.visibility = if (viewState.isLoading) View.GONE else View.VISIBLE
    }

    private fun retry() {
        // TODO: Use current RecommendationsType
        viewModel.refresh(RecommendationsType.Personalized)
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
        suggestions.scrollToPrevious()
    }

    override fun scrollToNext() {
        suggestions.scrollToNext()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (type == Personalized) {
            inflater?.inflate(R.menu.menu_main, menu)
        }
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

    override fun onReselected() {
        suggestions.scrollToStart()
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
