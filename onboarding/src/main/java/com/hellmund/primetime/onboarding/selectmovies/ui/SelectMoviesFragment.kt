package com.hellmund.primetime.onboarding.selectmovies.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.isConnected
import com.hellmund.primetime.onboarding.OnboardingHelper
import com.hellmund.primetime.onboarding.R
import com.hellmund.primetime.ui_common.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui_common.lazyViewModel
import com.hellmund.primetime.ui_common.observe
import com.hellmund.primetime.ui_common.onBottomReached
import com.hellmund.primetime.ui_common.showToast
import kotlinx.android.synthetic.main.fragment_select_movies.button
import kotlinx.android.synthetic.main.fragment_select_movies.error_container
import kotlinx.android.synthetic.main.fragment_select_movies.gridView
import kotlinx.android.synthetic.main.fragment_select_movies.shimmerLayout
import kotlinx.android.synthetic.main.view_samples_error.error_button
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

class SelectMoviesFragment : Fragment() {

    private var onFinishedAction: () -> Unit = {}
    private val adapter: SamplesAdapter by lazy {
        SamplesAdapter(imageLoader) { viewModel.dispatch(Action.ItemClicked(it)) }
    }

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var viewModelProvider: Provider<SelectMoviesViewModel>

    private val viewModel: SelectMoviesViewModel by lazyViewModel { viewModelProvider }

    private var isLoadingMore: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as Injector).injectSelectMoviesFragment(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_select_movies, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        updateNextButton()

        button.setOnClickListener { saveMovies() }
        error_button.setOnClickListener { viewModel.dispatch(Action.Refresh) }

        viewModel.viewState.observe(viewLifecycleOwner, this::render)
    }

    private fun setupRecyclerView() {
        gridView.adapter = adapter
        gridView.itemAnimator = DefaultItemAnimator()
        gridView.layoutManager = GridLayoutManager(requireContext(), 3)

        gridView.onBottomReached {
            if (isLoadingMore.not()) {
                viewModel.dispatch(Action.Refresh)
                isLoadingMore = true
            }
        }

        val spacing = resources.getDimension(R.dimen.default_space).roundToInt()
        gridView.addItemDecoration(EqualSpacingGridItemDecoration(spacing, 3))
    }

    private fun render(viewState: SelectMoviesViewState) {
        if (viewState.isFinished) {
            openNext()
            return
        }

        adapter.update(viewState.data)

        if (viewState.isLoading.not()) {
            shimmerLayout.stopShimmer()
            shimmerLayout.setShimmer(null)
        }

        val selected = viewState.data.filter { it.selected }
        updateNextButton(selected.size)

        gridView.isVisible = viewState.isError.not()
        error_container.isVisible = viewState.isError
        button.isVisible = viewState.isError.not()
    }

    private fun updateNextButton(count: Int = 0) {
        val remaining = MIN_COUNT - count
        val hasSelectedEnough = remaining <= 0

        button.isClickable = hasSelectedEnough
        button.isEnabled = hasSelectedEnough

        if (hasSelectedEnough) {
            button.setText(R.string.finish)
        } else {
            button.text = getString(R.string.select_more_format_string, remaining)
        }
    }

    private fun saveMovies() {
        val context = requireContext()
        if (!button.isEnabled) {
            context.showToast(getString(R.string.select_at_least, MIN_COUNT))
        }

        if (context.isConnected) {
            saveSelection()
        } else {
            context.showToast(getString(R.string.not_connected))
        }
    }

    private fun saveSelection() {
        val selected = adapter.selected
        viewModel.dispatch(Action.Store(selected))
    }

    private fun openNext() {
        onboardingHelper.isFirstLaunch = false
        onFinishedAction()
    }

    interface Injector {
        fun injectSelectMoviesFragment(selectMoviesFragment: SelectMoviesFragment)
    }

    companion object {
        private const val MIN_COUNT = 4
        fun newInstance(onFinished: () -> Unit) = SelectMoviesFragment().apply {
            onFinishedAction = onFinished
        }
    }

}
