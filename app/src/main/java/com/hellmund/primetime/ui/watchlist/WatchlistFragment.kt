package com.hellmund.primetime.ui.watchlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.history.HistoryActivity
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.observe
import com.hellmund.primetime.utils.showCancelableDialog
import com.hellmund.primetime.utils.showItemsDialog
import kotlinx.android.synthetic.main.fragment_watchlist.content
import kotlinx.android.synthetic.main.fragment_watchlist.indicator
import kotlinx.android.synthetic.main.fragment_watchlist.placeholder
import kotlinx.android.synthetic.main.fragment_watchlist.viewPager
import javax.inject.Inject
import javax.inject.Provider

class WatchlistFragment : Fragment() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<WatchlistViewModel>

    private val viewModel: WatchlistViewModel by lazyViewModel { viewModelProvider }

    private val adapter: WatchlistAdapter by lazy {
        WatchlistAdapter(
            imageLoader = imageLoader,
            onWatchedIt = this::onWatchedIt,
            onRemove = this::onRemove,
            onNotificationToggle = this::onNotificationToggle
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_watchlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        setupViewPager()
        viewModel.viewState.observe(viewLifecycleOwner, this::render)
    }

    private fun initToolbar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.watchlist)
    }

    private fun setupViewPager() {
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        indicator.attachTo(viewPager)
    }

    private fun render(viewState: WatchlistViewState) {
        adapter.update(viewState.data)
        indicator.reattach()

        content.isVisible = viewState.data.isNotEmpty()
        placeholder.isVisible = viewState.data.isEmpty()
    }

    private fun onNotificationToggle(movie: WatchlistMovieViewEntity) {
        viewModel.toggleNotification(movie)
    }

    private fun onRemove(movie: WatchlistMovieViewEntity) {
        requireContext().showCancelableDialog(
            messageResId = R.string.remove_from_watchlist_header,
            positiveResId = R.string.remove,
            onPositive = { viewModel.remove(movie) })
    }

    private fun onWatchedIt(movie: WatchlistMovieViewEntity) {
        val header = getString(R.string.rate_movie, movie.title)
        val options = arrayOf(getString(R.string.like), getString(R.string.dislike))

        requireContext().showItemsDialog(
            title = header,
            items = options,
            onSelected = { index ->
                val rating = if (index == 0) Constants.LIKE else Constants.DISLIKE
                viewModel.onMovieRated(movie, rating)
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_watchlist, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                openHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
