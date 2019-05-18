package com.hellmund.primetime.ui.watchlist.details

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.di.lazyViewModel
import com.hellmund.primetime.ui.watchlist.WatchlistMovieViewEntity
import com.hellmund.primetime.utils.*
import kotlinx.android.synthetic.main.fragment_watchlist_item.*
import javax.inject.Inject
import javax.inject.Provider

private const val KEY_WATCHLIST_MOVIE = "KEY_WATCHLIST_MOVIE"

class WatchlistMovieFragment : Fragment() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var viewModelProvider: Provider<WatchlistMovieViewModel>

    private val viewModel: WatchlistMovieViewModel by lazyViewModel { viewModelProvider }

    private lateinit var listener: OnInteractionListener
    private var removeDialog: Dialog? = null

    private val movie: WatchlistMovieViewEntity by lazy {
        checkNotNull(arguments?.getParcelable<WatchlistMovieViewEntity>(KEY_WATCHLIST_MOVIE))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        injector.watchlistMovieComponent()
                .movie(movie)
                .build()
                .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        viewModel.viewState.observe(viewLifecycleOwner, this::render)
        viewModel.movieEvents.observe(viewLifecycleOwner, this::handleMovieEvent)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_watchlist_item, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationIcon.setOnClickListener { viewModel.onNotificationClick() }
        watchedButton.setOnClickListener { viewModel.onWatched() }
        removeButton.setOnClickListener { viewModel.onRemove() }
    }

    private fun render(viewState: WatchlistMovieViewState) {
        val movie = viewState.movie

        imageLoader.load(movie.posterUrl, posterImageView)

        title.text = movie.title
        runtime.text = movie.formattedRuntime

        val releaseDate = movie.formattedReleaseDate
        if (movie.isUnreleased) {
            release.text = requireContext().getString(R.string.release_on, releaseDate)
        } else {
            release.text = releaseDate
        }

        watchedButton.isVisible = movie.isUnreleased.not()

        if (movie.isUnreleased) {
            setNotificationIcon(movie)
            setMovieOverlay(movie)
        }

        if (viewState.showToast) {
            showToast(movie)
        }

        if (viewState.showRemoveDialog) {
            removeDialog = requireContext().showCancelableDialog(
                    messageResId = R.string.remove_from_watchlist_header,
                    positiveResId = R.string.remove,
                    onPositive = { viewModel.onConfirmRemove() })
        } else {
            removeDialog?.dismiss()
        }
    }

    private fun handleMovieEvent(event: MovieEvent) {
        when (event) {
            is MovieEvent.Remove -> listener.onRemove(event.movie)
            is MovieEvent.MarkWatched -> listener.onWatchedIt(event.movie)
        }
    }

    private fun showToast(movie: WatchlistMovieViewEntity) {
        if (movie.notificationsActivated) {
            val text = getString(R.string.showing_notification_on, movie.formattedReleaseDate)
            requireContext().showToast(text)
        } else {
            requireContext().showToast(R.string.notification_off)
        }
    }

    private fun setNotificationIcon(movie: WatchlistMovieViewEntity) {
        val isEnabled = NotificationUtils.areNotificationsEnabled(requireContext())
        notificationIcon.isVisible = isEnabled

        if (movie.notificationsActivated) {
            notificationIcon.setImageResource(R.drawable.ic_notifications_active_white_24dp)
        } else {
            notificationIcon.setImageResource(R.drawable.ic_notifications_none_white_24dp)
        }
    }

    private fun setMovieOverlay(movie: WatchlistMovieViewEntity) {
        val releaseDate = movie.formattedReleaseDate
        runtime.text = requireContext().getString(R.string.release_on, releaseDate)
        runtimeIcon.setImageResource(R.drawable.ic_today_white_24dp)
        watchedButton.isVisible = false
    }

    interface OnInteractionListener {
        fun onWatchedIt(movie: WatchlistMovieViewEntity)
        fun onRemove(movie: WatchlistMovieViewEntity)
    }

    companion object {

        fun newInstance(
                movie: WatchlistMovieViewEntity
        ): WatchlistMovieFragment {
            return WatchlistMovieFragment().apply {
                arguments = bundleOf(KEY_WATCHLIST_MOVIE to movie)
            }
        }

    }

}
