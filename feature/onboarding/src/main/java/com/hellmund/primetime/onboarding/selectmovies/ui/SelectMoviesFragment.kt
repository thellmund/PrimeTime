package com.hellmund.primetime.onboarding.selectmovies.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.OnboardingHelper
import com.hellmund.primetime.onboarding.R
import com.hellmund.primetime.onboarding.databinding.FragmentSelectMoviesBinding
import com.hellmund.primetime.onboarding.selectgenres.di.OnboardingComponentProvider
import com.hellmund.primetime.ui_common.EqualSpacingGridItemDecoration
import com.hellmund.primetime.ui_common.util.onBottomReached
import com.hellmund.primetime.ui_common.util.showToast
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import com.hellmund.primetime.ui_common.viewmodel.observeSingleEvents
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

class SelectMoviesFragment : Fragment() {

    private var onFinishedAction: () -> Unit = {}
    private val adapter: SamplesAdapter by lazy {
        SamplesAdapter(imageLoader) { viewModel.dispatch(ViewEvent.ItemClicked(it)) }
    }

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var viewModelProvider: Provider<SelectMoviesViewModel>

    private val viewModel: SelectMoviesViewModel by lazyViewModel { viewModelProvider }

    private var isLoadingMore: Boolean = false

    private lateinit var binding: FragmentSelectMoviesBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val provider = requireActivity() as OnboardingComponentProvider
        provider.provideOnboardingComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        updateNextButton()

        binding.nextButton.setOnClickListener { saveMovies() }
        binding.errorButton.setOnClickListener { viewModel.dispatch(ViewEvent.Refresh) }

        binding.gridView.doOnApplyWindowInsets { v, insets, initialState ->
            v.updatePadding(
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
            )
        }

        binding.nextButtonContainer.doOnApplyWindowInsets { v, insets, initialState ->
            val initialHeight = requireContext().resources.getDimensionPixelSize(R.dimen.large_button)
            v.updateLayoutParams<ViewGroup.LayoutParams> {
                height = initialHeight + insets.systemWindowInsetBottom
            }

            v.updatePadding(
                bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
            )
        }

        viewModel.viewState.observe(viewLifecycleOwner, this::render)
        viewModel.navigationResults.observeSingleEvents(viewLifecycleOwner, this::navigate)
    }

    private fun setupRecyclerView() {
        val gridView = binding.gridView
        gridView.adapter = adapter
        gridView.itemAnimator = DefaultItemAnimator()
        gridView.layoutManager = GridLayoutManager(requireContext(), 3)

        gridView.onBottomReached {
            if (isLoadingMore.not()) {
                viewModel.dispatch(ViewEvent.Refresh)
                isLoadingMore = true
            }
        }

        val spacing = resources.getDimension(R.dimen.default_space).roundToInt()
        gridView.addItemDecoration(EqualSpacingGridItemDecoration(spacing, 3))
    }

    private fun render(viewState: SelectMoviesViewState) {
        adapter.update(viewState.data)

        val selected = viewState.data.filter { it.isSelected }
        updateNextButton(selected.size)

        binding.gridView.isVisible = viewState.isError.not()
        binding.errorContainer.isVisible = viewState.isError
        binding.nextButton.isVisible = viewState.isError.not()
    }

    private fun navigate(result: NavigationResult) {
        when (result) {
            is NavigationResult.OpenNext -> openNext()
        }
    }

    private fun updateNextButton(count: Int = 0) {
        val remaining = MIN_COUNT - count
        val hasSelectedEnough = remaining <= 0

        val button = binding.nextButton
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
        if (!binding.nextButton.isEnabled) {
            context.showToast(getString(R.string.select_at_least, MIN_COUNT))
        }

        saveSelection()
    }

    private fun saveSelection() {
        val selected = adapter.selected
        viewModel.dispatch(ViewEvent.Store(selected))
    }

    private fun openNext() {
        onboardingHelper.markFinished()
        onFinishedAction()
    }

    companion object {
        private const val MIN_COUNT = 4
        fun newInstance(onFinished: () -> Unit) = SelectMoviesFragment().apply {
            onFinishedAction = onFinished
        }
    }
}
