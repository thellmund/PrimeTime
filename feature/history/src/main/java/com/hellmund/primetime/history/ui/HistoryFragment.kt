package com.hellmund.primetime.history.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.history.R
import com.hellmund.primetime.history.databinding.FragmentHistoryBinding
import com.hellmund.primetime.history.di.DaggerHistoryComponent
import com.hellmund.primetime.ui_common.dialogs.showItemsDialog
import com.hellmund.primetime.ui_common.dialogs.showSingleSelectDialog
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class HistoryFragment : Fragment() {

    @Inject
    lateinit var viewModelProvider: Provider<HistoryViewModel>

    private val viewModel: HistoryViewModel by lazyViewModel { viewModelProvider }

    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter(this::openOptionsDialog)
    }

    private lateinit var binding: FragmentHistoryBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val component = DaggerHistoryComponent.builder()
            .core(coreComponent)
            .build()
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        viewModel.viewState.observe(viewLifecycleOwner, this::render)
    }

    private fun setupRecyclerView() = with(binding.recyclerView) {
        itemAnimator = DefaultItemAnimator()
        adapter = historyAdapter
    }

    private fun render(viewState: HistoryViewState) {
        binding.recyclerView.isVisible = viewState.isLoading.not()
        binding.progressBar.isVisible = viewState.isLoading
        historyAdapter.update(viewState.data)
    }

    private fun openOptionsDialog(movie: HistoryMovieViewEntity) {
        val options = arrayOf(
            getString(R.string.edit_rating),
            getString(R.string.remove_from_history)
        )

        requireContext().showItemsDialog(
            title = movie.title,
            items = options,
            onSelected = { index ->
                when (index) {
                    0 -> openEditRatingDialog(movie)
                    1 -> viewModel.dispatch(Action.Remove(movie))
                }
            }
        )
    }

    private fun openEditRatingDialog(movie: HistoryMovieViewEntity) {
        val options = arrayOf(getString(R.string.like), getString(R.string.dislike))
        val checked = if (movie.rating == Rating.Like) 0 else 1

        requireContext().showSingleSelectDialog(
            titleResId = R.string.edit,
            choices = options,
            checked = checked,
            positiveResId = R.string.save,
            onSelected = {
                if (it != checked) {
                    val newRating = if (it == 0) Rating.Like else Rating.Dislike
                    val ratedMovie = movie.apply(newRating)
                    viewModel.dispatch(Action.Update(ratedMovie))
                }
            }
        )
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }
}
