package com.hellmund.primetime.watchlist.ui

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.Preferences
import com.hellmund.primetime.watchlist.R
import com.hellmund.primetime.watchlist.databinding.ListItemWatchlistBinding

private const val COLLAPSED_LINES = 2

class WatchlistAdapter(
    private val imageLoader: ImageLoader,
    private val onWatchedIt: (WatchlistMovieViewEntity) -> Unit,
    private val onRemove: (WatchlistMovieViewEntity) -> Unit,
    private val onNotificationToggle: (WatchlistMovieViewEntity) -> Unit
) : RecyclerView.Adapter<WatchlistAdapter.ViewHolder>() {

    private val movies = mutableListOf<WatchlistMovieViewEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_watchlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movies[position], imageLoader, onWatchedIt, onRemove, onNotificationToggle)
    }

    override fun getItemCount(): Int = movies.size

    fun update(newItems: List<WatchlistMovieViewEntity>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(movies, newItems))
        movies.clear()
        movies += newItems
        diffResult.dispatchUpdatesTo(this)
    }

    class DiffUtilCallback(
        private val oldItems: List<WatchlistMovieViewEntity>,
        private val newItems: List<WatchlistMovieViewEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].id == newItems[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ListItemWatchlistBinding.bind(itemView)

        fun bind(
            movie: WatchlistMovieViewEntity,
            imageLoader: ImageLoader,
            onWatchedIt: (WatchlistMovieViewEntity) -> Unit,
            onRemove: (WatchlistMovieViewEntity) -> Unit,
            onNotificationToggle: (WatchlistMovieViewEntity) -> Unit
        ) = with(binding) {
            imageLoader.load(
                url = movie.posterUrl,
                placeholderResId = R.drawable.poster_placeholder,
                into = posterImageView
            )

            description.text = movie.description
            runtime.text = movie.formattedRuntime

            val releaseDate = movie.formattedReleaseDate
            if (movie.isUnreleased) {
                release.text = root.context.getString(R.string.release_on, releaseDate)
            } else {
                release.text = releaseDate
            }

            watchedButton.isVisible = movie.isUnreleased.not()
            watchedButton.setOnClickListener { onWatchedIt(movie) }
            removeButton.setOnClickListener { onRemove(movie) }
            notificationIcon.setOnClickListener { onNotificationToggle(movie) }

            infoContainer.setOnClickListener {
                TransitionManager.beginDelayedTransition(cardView)
                if (description.maxLines == COLLAPSED_LINES) {
                    description.maxLines = Int.MAX_VALUE
                    description.ellipsize = null
                } else {
                    description.maxLines = COLLAPSED_LINES
                    description.ellipsize = TextUtils.TruncateAt.END
                }
            }

            if (movie.isUnreleased) {
                setNotificationIcon(movie)
                setMovieOverlay(movie)
            }
        }

        private fun setNotificationIcon(movie: WatchlistMovieViewEntity) = with(binding) {
            notificationIcon.isVisible = root.context.areNotificationsEnabled

            if (movie.notificationsActivated) {
                notificationIcon.setImageResource(R.drawable.ic_notifications_active_white_24dp)
            } else {
                notificationIcon.setImageResource(R.drawable.ic_notifications_none_white_24dp)
            }
        }

        private fun setMovieOverlay(movie: WatchlistMovieViewEntity) = with(binding) {
            val releaseDate = movie.formattedReleaseDate
            runtime.text = root.context.getString(R.string.release_on, releaseDate)
            watchedButton.isVisible = false
        }

        private val Context.areNotificationsEnabled: Boolean
            get() {
                val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
                return sharedPrefs.getBoolean(Preferences.KEY_NOTIFICATIONS, true)
            }
    }
}
