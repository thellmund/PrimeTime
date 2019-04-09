package com.hellmund.primetime.main

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.hellmund.primetime.R
import com.hellmund.primetime.api.ApiClient
import com.hellmund.primetime.settings.SettingsActivity
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import java.lang.Math.round

class MainFragment : Fragment(), MainActivity.Reselectable, SuggestionFragment.ViewPagerHost {

    private val genreProvider: GenresProvider by lazy {
        RealGenresProvider(defaultSharedPreferences)
    }

    private val repository: RecommendationsRepository by lazy {
        RecommendationsRepository(ApiClient.instance, genreProvider)
    }

    private val viewModel: MainViewModel by lazy {
        val factory = MainViewModel.Factory(repository)
        ViewModelProviders.of(requireActivity(), factory).get(MainViewModel::class.java)
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
        // presenter.attachView(this)
        // presenter.loadIndices()

        // setToolbarSubtitle()
        setToolbarSubtitle(type)

        viewModel.viewState.observe(this, this::render)

        /*if (intentExtra != null) {
            // setupIntentRecommendations(intentExtra)
        } else if (displaySingleMovieRecommendation()) {
            // setupSingleMovieRecommendations()
        }*/

        // presenter.downloadRecommendationsAsync()
    }

    private fun render(viewState: MainViewState) {
        val viewStateInt = if (viewState.isError) Constants.ERROR_STATE else Constants.IDEAL_STATE
        // setToolbarSubtitle(viewState.recommendationsType)

        val margin = round(resources.getDimension(R.dimen.default_space))
        suggestions.pageMargin = margin

        suggestions.adapter = SuggestionsAdapter(requireFragmentManager(), requireContext(), viewStateInt, this, viewState.data)
        progressBar.visibility = if (viewState.isLoading) View.VISIBLE else View.GONE
        suggestions.visibility = if (viewState.isLoading) View.GONE else View.VISIBLE
    }

    /*private fun onOpenRatingDialog(position: Int) {
        val options = arrayOf(
                getString(com.hellmund.primetime.R.string.show_more_like_this),
                getString(com.hellmund.primetime.R.string.show_less_like_this)
        )

        AlertDialog.Builder(requireContext())
                .setTitle(getString(com.hellmund.primetime.R.string.adjust_recommendations))
                .setItems(options) { _, which ->
                    val rating = if (which == 0) Constants.LIKE else Constants.DISLIKE
                    // presenter.addMovieRating(position, rating)
                }
                .setCancelable(true)
                .show()
    }*/

    private fun setToolbarSubtitle(type: RecommendationsType) {
        val title = when (type) {
            is RecommendationsType.Personalized -> getString(R.string.app_name)
            is RecommendationsType.BasedOnMovie -> type.title
            is RecommendationsType.NowPlaying -> getString(R.string.now_playing)
            is RecommendationsType.Upcoming -> getString(R.string.upcoming)
            is RecommendationsType.ByGenre -> type.genre.name
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title
    }

    /*private fun displaySingleMovieRecommendation(): Boolean {
        val shouldDisplay = requireActivity().intent.getBooleanExtra(Constants.SINGLE_MOVIE, false)
        requireActivity().intent.removeExtra(Constants.SINGLE_MOVIE)
        return shouldDisplay
    }*/

    /*private fun setupSingleMovieRecommendations() {
        val intent = requireActivity().intent
        presenter.setupSingleMovieRecommendations(
                intent.getIntExtra(Constants.MOVIE_ID, 0),
                intent.getStringExtra(Constants.MOVIE_TITLE)
        )
    }

    private fun setupIntentRecommendations(intentExtra: String) {
        presenter.setupCategoryRecommendations(intentExtra)
    }*/

    /*private fun onMovieRatingAdded(id: Int, rating: Int) {
        // displayRatingSnackbar(suggestions.currentItem, id, rating)
        suggestions.scrollToNext()
    }*/

    /*private fun displayRatingSnackbar(position: Int, id: Int, rating: Int) {
        val message = if (rating == Constants.LIKE) {
            getString(R.string.will_more_like_this)
        } else {
            getString(R.string.will_less_like_this)
        }

        val movie = presenter.getMovieAt(position)
        // History.add(movie, rating)

        Snackbar.make(suggestions, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    suggestions.currentItem = position
                    presenter.showUndoToast(id, rating)
                    // History.remove(id);
                }
                .setActionTextColor(UiUtils.getSnackbarColor(requireContext()))
                .show()
    }*/

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
        inflater?.inflate(R.menu.menu_main, menu)
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
                type: RecommendationsType = RecommendationsType.Personalized
        ) = MainFragment().apply {
            arguments = Bundle().apply { putParcelable(KEY_RECOMMENDATIONS_TYPE, type) }
        }

    }

}
