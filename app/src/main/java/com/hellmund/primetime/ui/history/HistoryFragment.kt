package com.hellmund.primetime.ui.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.observe
import com.hellmund.primetime.utils.showItemsDialog
import com.hellmund.primetime.utils.showSingleSelectDialog
import com.hellmund.primetime.utils.showToast
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
        injector.inject(this)
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
                        0 -> showSimilarMovies(movie)
                        1 -> openEditRatingDialog(movie)
                        2 -> removeFromHistory(movie)
                    }
                }
        )
    }

    private fun getDialogOptions(): Array<String> {
        return arrayOf(
                getString(R.string.show_similar_movies),
                getString(R.string.edit_rating),
                getString(R.string.remove_from_history)
        )
    }

    private fun showSimilarMovies(movie: HistoryMovieViewEntity) {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra(Constants.SINGLE_MOVIE, true)
        intent.putExtra(Constants.MOVIE_ID, movie.id)
        intent.putExtra(Constants.MOVIE_TITLE, movie.title)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
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
        val checked = if (movie.rating == Constants.LIKE) 0 else 1

        requireContext().showSingleSelectDialog(
                titleResId = R.string.edit,
                choices = options,
                checked = checked,
                positiveResId = R.string.save,
                onSelected = {
                    if (it != checked) {
                        val newRating = if (it == 0) Constants.LIKE else Constants.DISLIKE
                        updateRating(movie, newRating)
                    }
                }
        )
    }

    private fun updateRating(movie: HistoryMovieViewEntity, newRating: Int) {
        viewModel.dispatch(Action.Update(movie, newRating))
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }

}
