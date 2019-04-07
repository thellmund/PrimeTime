package com.hellmund.primetime.history

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import com.hellmund.primetime.main.MainActivity
import com.hellmund.primetime.model.HistoryMovie
import com.hellmund.primetime.utils.Constants
import kotlinx.android.synthetic.main.fragment_history.*
import java.util.*

class HistoryFragment : Fragment(), HistoryAdapter.OnInteractionListener {

    private val history = mutableListOf<HistoryMovie>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayHistory()
    }

    private fun displayHistory() {
        fillListViewContent()
        toggleProgressBar()
    }

    private fun toggleProgressBar() {
        /*if (progress_container.visibility == View.GONE) {
            recycler_view.visibility = View.GONE
            progress_container.visibility = View.VISIBLE
        } else {
            recycler_view.visibility = View.VISIBLE
            progress_container.visibility = View.GONE
        }*/
    }

    private fun fillListViewContent() {
        val adapter = HistoryAdapter(requireContext(), this, history as ArrayList<HistoryMovie>)
        recycler_view.setHasFixedSize(true)
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.itemAnimator = DefaultItemAnimator()

        val helper = ItemTouchHelper(HistoryItemTouchHelper(requireContext(), adapter))
        helper.attachToRecyclerView(recycler_view)
    }

    override fun onOpenDialog(position: Int) {
        val movie = history[position]
        val options = getDialogOptions(position)

        AlertDialog.Builder(requireContext())
                .setTitle(movie.title)
                .setItems(options) { _, which ->
                    if (which == 0) {
                        openEditRatingDialog(position)
                    } else if (which == 1) {
                        showSimilarMovies(movie)
                    }
                }.create().show()
    }

    private fun getDialogOptions(position: Int): Array<String> {
        return if (history[position].isUpdating) {
            arrayOf(getString(R.string.show_similar_movies))
        } else {
            arrayOf(getString(R.string.edit_rating), getString(R.string.show_similar_movies))
        }
    }

    private fun showSimilarMovies(movie: HistoryMovie) {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra(Constants.SINGLE_MOVIE, true)
        intent.putExtra(Constants.MOVIE_ID, movie.id)
        intent.putExtra(Constants.MOVIE_TITLE, movie.title)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun openEditRatingDialog(position: Int) {
        val movie = history.get(position)
        val options = arrayOf(getString(R.string.like), getString(R.string.dislike))
        val checked = if (movie.rating == Constants.LIKE) 0 else 1

        AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_rating)
                .setSingleChoiceItems(options, checked, null)
                .setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
                .setPositiveButton(R.string.save) { dialog, which ->
                    val dialogListView = (dialog as AlertDialog).listView
                    val selected = dialogListView.checkedItemPosition

                    if (selected != checked) {
                        val newRating = if (selected == 0) Constants.LIKE else Constants.DISLIKE
                        updateRating(movie, position, newRating)
                    }

                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
    }

    private fun updateRating(movie: HistoryMovie, position: Int, newRating: Int) {
        movie.isUpdating = true
        movie.rating = newRating
        recycler_view.adapter.notifyItemChanged(position)

        Handler().postDelayed({
            movie.isUpdating = false
            recycler_view.adapter.notifyItemChanged(position)
        }, 500)
    }

    override fun getContainer(): View {
        return recycler_view
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryFragment()
    }

}
