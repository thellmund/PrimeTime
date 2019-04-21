package com.hellmund.primetime.watchlist

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.*
import android.view.animation.AlphaAnimation
import com.hellmund.primetime.R
import com.hellmund.primetime.database.PrimeTimeDatabase
import com.hellmund.primetime.database.WatchlistMovie
import com.hellmund.primetime.history.HistoryActivity
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.observe
import kotlinx.android.synthetic.main.fragment_watchlist.*

class WatchlistFragment : Fragment(),
        WatchlistMovieFragment.OnInteractionListener, ViewPager.OnPageChangeListener {

    // TODO
    private val movies = mutableListOf<WatchlistMovie>()

    private val viewModel: WatchlistViewModel by lazy {
        val repository = WatchlistRepository(PrimeTimeDatabase.getInstance(requireContext()))
        val factory = WatchlistViewModel.Factory(repository)
        ViewModelProviders.of(requireActivity(), factory).get(WatchlistViewModel::class.java)
    }

    private val adapter: WatchlistAdapter by lazy {
        WatchlistAdapter(requireFragmentManager(), this)
    }

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

        viewPager.adapter = adapter
        indicator.setViewPager(viewPager)

        viewModel.viewState.observe(this, this::render)
        viewPager.setOnPageChangeListener(this)
    }

    private fun render(viewState: WatchlistViewState) {
        adapter.update(viewState.data)

        if (viewState.data.isEmpty()) {
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

        // TODO
        /*if (prev >= 0 && movies[prev].isDeleted) {
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
        }*/
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
        // movies[position].delete()
        val newPosition = computePositionOfNextItem(position)
        scrollToNextPosition(newPosition)

        val message: String = if (rating == Constants.LIKE) {
            getString(R.string.will_more_like_this)
        } else {
            getString(R.string.will_less_like_this)
        }

        Snackbar.make(viewPager, message, Snackbar.LENGTH_LONG)
                .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                .setAction(R.string.undo) { _ ->
                    // movie.undelete()
                    restoreInViewPager(movie, position)
                }
                .show()
    }

    private fun restoreInViewPager(movie: WatchlistMovie, position: Int) {
        movies.add(position, movie)
        // TODO toggleListAndPlaceholder()
        viewPager.currentItem = position
    }

    private fun computePositionOfNextItem(position: Int): Int {
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
            // toggleListAndPlaceholder()
        } else {
            viewPager.currentItem = newPosition
        }
    }

    override fun onRemove(movie: WatchlistMovie, position: Int) {
        val newPosition = computePositionOfNextItem(position)
        // movie.delete()

        viewModel.remove(movie)

        scrollToNextPosition(newPosition)
        displayRemoveSnackbar(movie, position)
    }

    private fun displayRemoveSnackbar(movie: WatchlistMovie, position: Int) {
        Snackbar.make(content, R.string.watchlist_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    // movie.undelete()
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
