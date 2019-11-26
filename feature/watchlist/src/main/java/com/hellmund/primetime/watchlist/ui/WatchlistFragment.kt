package com.hellmund.primetime.watchlist.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.viewpager2.widget.ViewPager2
import com.hellmund.primetime.core.AddressableActivity
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.coreComponent
import com.hellmund.primetime.core.createIntent
import com.hellmund.primetime.ui_common.dialogs.RateMovieDialog
import com.hellmund.primetime.ui_common.dialogs.showCancelableDialog
import com.hellmund.primetime.ui_common.viewmodel.lazyViewModel
import com.hellmund.primetime.watchlist.R
import com.hellmund.primetime.watchlist.databinding.FragmentWatchlistBinding
import com.hellmund.primetime.watchlist.di.DaggerWatchlistComponent
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

    private lateinit var binding: FragmentWatchlistBinding

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
        val component = DaggerWatchlistComponent.builder()
            .core(coreComponent)
            .build()
        component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        setupViewPager()
        viewModel.viewState.observe(viewLifecycleOwner, this::render)
    }

    private fun initToolbar() {
        val toolbar = binding.toolbarContainer.toolbar
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
        val viewPager = binding.viewPager
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.indicator.attachTo(viewPager)
    }

    private fun render(viewState: WatchlistViewState) {
        adapter.update(viewState.data)
        binding.indicator.reattach()

        binding.contentContainer.isVisible = viewState.data.isNotEmpty()
        binding.placeholderContainer.isVisible = viewState.data.isEmpty()
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
        val intent = requireContext().createIntent(AddressableActivity.History)
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() = WatchlistFragment()
    }

}
