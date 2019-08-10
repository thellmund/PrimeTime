package com.hellmund.primetime.history.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.history.R
import com.hellmund.primetime.ui_common.lazyViewModel
import com.hellmund.primetime.ui_common.observe
import com.hellmund.primetime.ui_common.dialogs.showItemsDialog
import com.hellmund.primetime.ui_common.dialogs.showSingleSelectDialog
import com.hellmund.primetime.ui_common.showToast
import kotlinx.android.synthetic.main.fragment_history.progressBar
import kotlinx.android.synthetic.main.fragment_history.recyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Provider

@FlowPreview
@ExperimentalCoroutinesApi
class HistoryFragment : Fragment() {

    @Inject
    lateinit var viewModelProvider: Provider<HistoryViewModel>

    private val viewModel: HistoryViewModel by lazyViewModel { viewModelProvider }

    private val adapter: HistoryAdapter by lazy {
        HistoryAdapter(this::onOpenDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as Injector).injectHistoryFragment(this)
        // injector.injectHistoryFragment(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        viewModel.viewState.observe(viewLifecycleOwner, this::render)
    }

    private fun setupRecyclerView() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
    }

    private fun render(viewState: HistoryViewState) {
        recyclerView.isVisible = viewState.isLoading.not()
        progressBar.isVisible = viewState.isLoading
        adapter.update(viewState.data)
    }

    private fun onOpenDialog(movie: HistoryMovieViewEntity) {
        val options = getDialogOptions()

        requireContext().showItemsDialog(
            title = movie.title,
            items = options,
            onSelected = { index ->
                when (index) {
                    0 -> openEditRatingDialog(movie)
                    1 -> removeFromHistory(movie)
                }
            }
        )
    }

    private fun getDialogOptions(): Array<String> {
        return arrayOf(
            getString(R.string.edit_rating),
            getString(R.string.remove_from_history)
        )
    }

    private fun removeFromHistory(movie: HistoryMovieViewEntity) {
        if (adapter.canRemove()) {
            viewModel.dispatch(Action.Remove(movie))
        } else {
            requireContext().showToast(R.string.cant_remove_more_items)
        }
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
                    updateRating(ratedMovie)
                }
            }
        )
    }

    private fun updateRating(ratedMovie: RatedHistoryMovie) {
        viewModel.dispatch(Action.Update(ratedMovie))
    }

    interface Injector {
        fun injectHistoryFragment(historyFragment: HistoryFragment)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }

}
