package com.hellmund.primetime.ui.watchlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.ui.shared.RateMovieDialog
import com.hellmund.primetime.ui_common.lazyViewModel
import com.hellmund.primetime.ui_common.observe
import com.hellmund.primetime.ui_common.showCancelableDialog
import com.hellmund.primetime.core.ImageLoader
import kotlinx.android.synthetic.main.fragment_watchlist.content
import kotlinx.android.synthetic.main.fragment_watchlist.indicator
import kotlinx.android.synthetic.main.fragment_watchlist.placeholder
import kotlinx.android.synthetic.main.fragment_watchlist.viewPager
import kotlinx.android.synthetic.main.view_toolbar.toolbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Provider

@FlowPreview
@ExperimentalCoroutinesApi
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
        toolbar.setTitle(R.string.watchlist)
        toolbar.inflateMenu(R.menu.menu_watchlist)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_history -> {
                    openHistory()
                    true
                }
                else -> super.onOptionsItemSelected(menuItem)
            }
        }
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
        viewModel.dispatch(Action.ToggleNotification(movie))
    }

    private fun onRemove(movie: WatchlistMovieViewEntity) {
        requireContext().showCancelableDialog(
            messageResId = R.string.remove_from_watchlist_header,
            positiveResId = R.string.remove,
            onPositive = { viewModel.dispatch(Action.Remove(movie)) })
    }

    private fun onWatchedIt(movie: WatchlistMovieViewEntity) {
        val header = getString(R.string.rate_movie, movie.title)
        RateMovieDialog
            .make(requireActivity())
            .setTitle(header)
            .setPositiveText(R.string.like)
            .setNegativeText(R.string.dislike)
            .onItemSelected { rating ->
                val ratedMovie = movie.apply(rating)
                viewModel.dispatch(Action.RateMovie(ratedMovie))
            }
            .show()
    }

    private fun openHistory() {
        val intent = Intent(requireContext(), com.hellmund.primetime.history.HistoryActivity::class.java)
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() = WatchlistFragment()
    }

}
