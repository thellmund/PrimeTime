package com.hellmund.primetime.ui.watchlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.history.HistoryActivity
import com.hellmund.primetime.ui.watchlist.details.WatchlistMovieFragment
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.observe
import com.hellmund.primetime.utils.showItemsDialog
import kotlinx.android.synthetic.main.fragment_watchlist.*
import javax.inject.Inject
import javax.inject.Provider

class WatchlistFragment : Fragment(), WatchlistMovieFragment.OnInteractionListener {

    @Inject
    lateinit var viewModelProvider: Provider<WatchlistViewModel>

    private val viewModel: WatchlistViewModel by lazyViewModel { viewModelProvider }

    private val adapter: WatchlistAdapter by lazy {
        WatchlistAdapter(requireFragmentManager(), this)
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
    ): View {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()

        viewPager.adapter = adapter
        indicator.setViewPager(viewPager)

        viewModel.viewState.observe(this, this::render)
    }

    private fun initToolbar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.watchlist)
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

    override fun onRemove(movie: WatchlistMovieViewEntity) {
        viewModel.remove(movie)
    }

    override fun onWatchedIt(movie: WatchlistMovieViewEntity) {
        val header = getString(R.string.rate_movie, movie.title)
        val options = arrayOf(getString(R.string.like), getString(R.string.dislike))

        requireContext().showItemsDialog(
                title = header,
                items = options,
                onSelected = { index ->
                    val rating = if (index == 0) Constants.LIKE else Constants.DISLIKE
                    onMovieRated(movie, rating)
                }
        )
    }

    private fun onMovieRated(movie: WatchlistMovieViewEntity, rating: Int) {
        viewModel.onMovieRated(movie, rating)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_watchlist, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().onBackPressed()
            R.id.action_history -> openHistory()
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
