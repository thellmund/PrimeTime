package com.hellmund.primetime.ui.history

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import com.hellmund.primetime.data.database.HistoryMovie
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.isVisible
import com.hellmund.primetime.utils.observe
import kotlinx.android.synthetic.main.fragment_history.*
import javax.inject.Inject
import javax.inject.Provider

class HistoryFragment : Fragment() {

    @Inject
    lateinit var viewModelProvider: Provider<HistoryViewModel>

    private val viewModel: HistoryViewModel by lazyViewModel { viewModelProvider }

    private val adapter: HistoryAdapter by lazy {
        HistoryAdapter(this::onOpenDialog)
    }

    override fun onAttach(context: Context?) {
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
        recycler_view.adapter = adapter
        viewModel.viewState.observe(this, this::render)
    }

    private fun render(viewState: HistoryViewState) {
        recycler_view.isVisible = viewState.isLoading.not()
        progress_bar.isVisible = viewState.isLoading

        adapter.update(viewState.data)
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.itemAnimator = DefaultItemAnimator()
    }

    private fun onOpenDialog(movie: HistoryMovieViewEntity) {
        val options = getDialogOptions()
        AlertDialog.Builder(requireContext())
                .setTitle(movie.title)
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openEditRatingDialog(movie)
                        1 -> showSimilarMovies(movie)
                        2 -> removeFromHistory(movie)
                    }
                }.create().show()
    }

    private fun getDialogOptions(): Array<String> {
        val options = mutableListOf(
                getString(R.string.show_similar_movies),
                getString(R.string.edit_rating)
        )

        if (adapter.canRemove()) {
            options += getString(R.string.remove_from_history)
        }

        return options.toTypedArray()
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
            viewModel.remove(movie)
        }
    }

    private fun openEditRatingDialog(movie: HistoryMovieViewEntity) {
        val options = arrayOf(getString(R.string.like), getString(R.string.dislike))
        val checked = if (movie.rating == Constants.LIKE) 0 else 1

        AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_rating)
                .setSingleChoiceItems(options, checked, null)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save) { dialog, _ ->
                    val dialogListView = (dialog as AlertDialog).listView
                    val selected = dialogListView.checkedItemPosition

                    if (selected != checked) {
                        val newRating = if (selected == 0) Constants.LIKE else Constants.DISLIKE
                        // TODO updateRating(movie, position, newRating)
                    }
                }
                .setCancelable(true)
                .show()
    }

    private fun updateRating(movie: HistoryMovie, position: Int, newRating: Int) {
        movie.isUpdating = true
        movie.rating = newRating
        recycler_view.adapter?.notifyItemChanged(position)

        Handler().postDelayed({
            movie.isUpdating = false
            recycler_view.adapter?.notifyItemChanged(position)
        }, 500)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }

}
