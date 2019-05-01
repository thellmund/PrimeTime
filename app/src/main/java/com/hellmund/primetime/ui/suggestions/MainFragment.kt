package com.hellmund.primetime.ui.suggestions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.settings.SettingsActivity
import com.hellmund.primetime.ui.suggestions.RecommendationsType.Personalized
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.Math.round
import javax.inject.Inject
import javax.inject.Provider

class MainFragment : Fragment(), MainActivity.Reselectable, SuggestionFragment.ViewPagerHost {

    @Inject
    lateinit var viewModelProvider: Provider<MainViewModel>

    private val viewModel: MainViewModel by lazyViewModel { viewModelProvider }

    private val type: RecommendationsType by lazy {
        arguments?.getParcelable(KEY_RECOMMENDATIONS_TYPE) as RecommendationsType
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

        // TODO Error handling
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
        suggestions.scrollToPrevious()
    }

    override fun scrollToNext() {
        suggestions.scrollToNext()
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
                choices = arrayOf("All", "Only from my streaming services"),
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
