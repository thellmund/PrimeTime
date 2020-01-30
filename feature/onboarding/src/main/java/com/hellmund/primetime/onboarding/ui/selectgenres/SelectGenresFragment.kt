package com.hellmund.primetime.onboarding.ui.selectgenres

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.material.chip.Chip
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.onboarding.R
import com.hellmund.primetime.onboarding.databinding.FragmentSelectGenresBinding
import com.hellmund.primetime.onboarding.di.OnboardingComponentProvider
import com.hellmund.primetime.onboarding.ui.OnboardingNavigator
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import com.hellmund.primetime.ui_common.viewmodel.observeSingleEvents
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import javax.inject.Inject
import javax.inject.Provider

class SelectGenresFragment : Fragment() {

    private val genres = mutableListOf<Genre>()

    @Inject
    lateinit var onboardingNavigator: OnboardingNavigator

    @Inject
    lateinit var viewModelProvider: Provider<SelectGenresViewModel>

    private val viewModel: SelectGenresViewModel by lazyViewModel { viewModelProvider }

    private lateinit var binding: FragmentSelectGenresBinding

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
        binding = FragmentSelectGenresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        updateNextButton()
        binding.button.setOnClickListener { saveGenres() }

        binding.button.doOnApplyWindowInsets { v, insets, initialState ->
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = initialState.margins.bottom + insets.systemWindowInsetBottom
            }
        }

        viewModel.viewState.observe(viewLifecycleOwner, this::render)
        viewModel.navigationResults.observeSingleEvents(viewLifecycleOwner, this::navigate)
    }

    private fun updateNextButton(genres: List<Genre> = emptyList()) {
        val remaining = MIN_COUNT - genres.filter { it.isPreferred }.size
        val hasSelectedEnough = remaining <= 0

        val button = binding.button
        button.isClickable = hasSelectedEnough
        button.isEnabled = hasSelectedEnough

        if (hasSelectedEnough) {
            button.setText(R.string.next)
        } else {
            button.text = getString(R.string.select_more_format_string, remaining)
        }
    }

    private fun render(viewState: SelectGenresViewState) {
        genres.clear()
        genres += viewState.data

        binding.recommendationsProgressBar.isVisible = viewState.isLoading
        binding.chipGroup.isVisible = viewState.isLoading.not()

        showGenres(viewState.data)
        updateNextButton(viewState.data)
    }

    private fun navigate(event: NavigationResult) {
        when (event) {
            is NavigationResult.OpenNext -> onboardingNavigator.next()
        }
    }

    private fun showGenres(genres: List<Genre>) {
        binding.chipGroup.removeAllViews()
        genres
            .map { genre ->
                GenreChip(requireContext()).also {
                    it.genre = genre
                    it.isChecked = genre.isPreferred
                }
            }
            .forEach {
                it.setOnCheckedChangeListener { chip, _ -> onCheckedChange(chip as GenreChip) }
                binding.chipGroup.addView(it)
            }
    }

    private fun onCheckedChange(chip: GenreChip) {
        viewModel.dispatch(ViewEvent.ToggleGenre(chip.genre))
    }

    private fun saveGenres() {
        val checkedItems = binding.chipGroup.children.toList().map { it as Chip }
        val includedGenres = genres.mapIndexed { index, genre ->
            Genre.Impl(
                id = genre.id,
                name = genre.name,
                isPreferred = checkedItems[index].isChecked,
                isExcluded = genre.isExcluded
            )
        }
        viewModel.dispatch(ViewEvent.Store(includedGenres))
    }

    companion object {
        private const val MIN_COUNT = 2
        fun newInstance() = SelectGenresFragment()
    }
}
