package com.hellmund.primetime.watchlist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.*
import android.view.animation.AlphaAnimation
import com.hellmund.primetime.R
import com.hellmund.primetime.model.WatchlistMovie
import com.hellmund.primetime.ui.history.HistoryActivity
import com.hellmund.primetime.ui.watchlist.WatchlistAdapter
import com.hellmund.primetime.ui.watchlist.WatchlistMovieFragment
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.UiUtils
import kotlinx.android.synthetic.main.fragment_watchlist.*

class WatchlistFragment : Fragment(), WatchlistMovieFragment.OnInteractionListener, ViewPager.OnPageChangeListener {

    private val movies = mutableListOf<WatchlistMovie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleListAndPlaceholder()

        viewPager.setOnPageChangeListener(this)
    }

    private fun toggleListAndPlaceholder() {
        val adapter = WatchlistAdapter(requireFragmentManager(), requireContext(), movies.size)
        viewPager.adapter = adapter
        indicator.setViewPager(viewPager)

        if (movies.isEmpty()) {
            val animation = AlphaAnimation(1.0f, 0.0f)
            animation.duration = 300
            content.animation = animation
            content.animate()
            placeholder.visibility = View.VISIBLE
        } else {
            val animation = AlphaAnimation(0.0f, 1.0f)
            animation.duration = 300
            content.animation = animation
            content.animate()
            placeholder.visibility = View.GONE
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            return
        }

        val position = viewPager.currentItem
        val prev = position - 1
        val next = position + 1

        if (prev >= 0 && movies[prev].isDeleted) {
            movies.removeAt(prev)
            viewPager.adapter = WatchlistAdapter(requireFragmentManager(), requireContext(), movies.size)
            viewPager.currentItem = prev
            indicator.setViewPager(viewPager)
        }

        if (next < movies.size && movies.get(next).isDeleted()) {
            movies.removeAt(next)
            viewPager.adapter = WatchlistAdapter(requireFragmentManager(), requireContext(), movies.size)
            viewPager.currentItem = next
            indicator.setViewPager(viewPager)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

    override fun onPageSelected(position: Int) = Unit

    override fun onWatchedIt(position: Int) {
        val movie = movies[position]
        val title = movie.title
        val header = String.format(getString(R.string.rate_movie), title)
        val options = arrayOf(getString(R.string.like), getString(R.string.dislike))

        AlertDialog.Builder(requireContext())
                .setTitle(header)
                .setItems(options) { _, which ->
                    val rating = if (which == 0) Constants.LIKE else Constants.DISLIKE
                    rateMovie(movie, position, rating)
                }.show()
    }

    private fun rateMovie(movie: WatchlistMovie, position: Int, rating: Int) {
        movies[position].delete()
        val newPosition = getPositionOfNextItem(position)
        scrollToNextPosition(newPosition)

        val message: String = if (rating == Constants.LIKE) {
            getString(R.string.will_more_like_this)
        } else {
            getString(R.string.will_less_like_this)
        }

        Snackbar.make(viewPager, message, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                .setAction(R.string.undo) { _ ->
                    movie.undelete()
                    restoreInViewPager(movie, position)
                }
                .show()
    }

    private fun restoreInViewPager(movie: WatchlistMovie, position: Int) {
        movies.add(position, movie)
        toggleListAndPlaceholder()
        viewPager.currentItem = position
    }

    private fun getPositionOfNextItem(position: Int): Int {
        val size = movies.size

        if (size == 1) {
            return -1
        }

        return if (position == size - 1) {
            position - 1
        } else {
            position + 1
        }
    }

    private fun scrollToNextPosition(newPosition: Int) {
        if (newPosition == -1) {
            movies.removeAt(0)
            toggleListAndPlaceholder()
        } else {
            viewPager.currentItem = newPosition
        }
    }

    override fun onRemove(position: Int) {
        val movie = movies[position]
        val newPosition = getPositionOfNextItem(position)
        movie.delete()

        scrollToNextPosition(newPosition)
        displayRemoveSnackbar(movie, position)
    }

    private fun displayRemoveSnackbar(movie: WatchlistMovie, position: Int) {
        Snackbar.make(content, R.string.watchlist_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    movie.undelete()
                    restoreInViewPager(movie, position)
                }
                .setActionTextColor(UiUtils.getSnackbarColor(requireContext()))
                .show()
    }

    override fun onGetMovie(position: Int): WatchlistMovie {
        return movies[position]
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_watchlist, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
            R.id.history -> openHistory()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun openHistory() {
        val intent = Intent(requireContext(), HistoryActivity::class.java)
        startActivity(intent)

    }

    companion object {
        @JvmStatic
        fun newInstance() = WatchlistFragment()
    }

}
