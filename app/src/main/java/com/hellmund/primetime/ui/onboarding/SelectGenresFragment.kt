package com.hellmund.primetime.ui.onboarding

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Genre
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.selectgenres.Action
import com.hellmund.primetime.ui.selectgenres.GenreChip
import com.hellmund.primetime.ui.selectgenres.SelectGenresViewModel
import com.hellmund.primetime.ui.selectgenres.SelectGenresViewState
import com.hellmund.primetime.ui.shared.SingleLiveDataEvent
import com.hellmund.primetime.utils.observe
import com.hellmund.primetime.utils.observeNonNull
import kotlinx.android.synthetic.main.fragment_select_genres.button
import kotlinx.android.synthetic.main.fragment_select_genres.chipGroup
import kotlinx.android.synthetic.main.fragment_select_genres.container
import kotlinx.android.synthetic.main.fragment_select_genres.recommendationsProgressBar
import javax.inject.Inject
import javax.inject.Provider

class SelectGenresFragment : Fragment() {

    private var onFinishedAction: () -> Unit = {}
    private val genres = mutableListOf<Genre>()

    @Inject
    lateinit var viewModelProvider: Provider<SelectGenresViewModel>

    private val viewModel: SelectGenresViewModel by lazyViewModel { viewModelProvider }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_select_genres, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        container.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        updateNextButton()
        button.setOnClickListener { saveGenres() }
        viewModel.viewState.observe(viewLifecycleOwner, this::render)
        viewModel.navigation.observeNonNull(viewLifecycleOwner, this::navigate)
    }

    private fun updateNextButton(genres: List<Genre> = emptyList()) {
        val remaining = MIN_COUNT - genres.filter { it.isPreferred }.size
        val hasSelectedEnough = remaining <= 0

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

        recommendationsProgressBar.isVisible = viewState.isLoading
        chipGroup.isVisible = viewState.isLoading.not()

        showGenres(viewState.data)
        updateNextButton(viewState.data)

        // TODO: Error and loading handling (SwipeRefreshLayout)
    }

    private fun navigate(event: SingleLiveDataEvent<Unit>) {
        event.getIfNotHandled()?.let {
            onFinishedAction()
        }
    }

    private fun showGenres(genres: List<Genre>) {
        chipGroup.removeAllViews()
        genres
            .map { genre ->
                GenreChip(requireContext()).also {
                    it.genre = genre
                    it.isChecked = genre.isPreferred
                }
            }
            .forEach {
                it.setOnCheckedChangeListener { chip, _ -> onCheckedChange(chip as GenreChip) }
                chipGroup.addView(it)
            }
    }

    private fun onCheckedChange(chip: GenreChip) {
        viewModel.dispatch(Action.ToggleGenre(chip.genre))
    }

    private fun saveGenres() {
        val checkedItems = chipGroup.children.toList().map { it as Chip }
        val includedGenres = genres.mapIndexed { index, genre ->
            genre.copy(isPreferred = checkedItems[index].isChecked)
        }
        viewModel.dispatch(Action.Store(includedGenres))
    }

    companion object {
        private const val MIN_COUNT = 2
        fun newInstance(onFinished: () -> Unit) = SelectGenresFragment().apply {
            onFinishedAction = onFinished
        }
    }

}
