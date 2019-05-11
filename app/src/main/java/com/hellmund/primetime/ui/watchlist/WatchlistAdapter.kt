package com.hellmund.primetime.ui.watchlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.NotificationUtils
import com.hellmund.primetime.utils.Transformation
import kotlinx.android.synthetic.main.list_item_watchlist.view.*

class WatchlistAdapter(
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
        holder.bind(movies[position], onWatchedIt, onRemove, onNotificationToggle)
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

        fun bind(
                movie: WatchlistMovieViewEntity,
                onWatchedIt: (WatchlistMovieViewEntity) -> Unit,
                onRemove: (WatchlistMovieViewEntity) -> Unit,
                onNotificationToggle: (WatchlistMovieViewEntity) -> Unit
        ) = with(itemView) {
            ImageLoader
                    .with(context)
                    .load(
                            url = movie.posterUrl,
                            transformations = arrayOf(
                                    Transformation.CenterCrop,
                                    Transformation.Placeholder(R.drawable.poster_placeholder)
                            ),
                            into = posterImageView
                    )

            title.text = movie.title
            runtime_text.text = movie.formattedRuntime

            val releaseDate = movie.formattedReleaseDate
            if (movie.isUnreleased) {
                release_text.text = context.getString(R.string.release_on, releaseDate)
            } else {
                release_text.text = releaseDate
            }

            watched_button.isVisible = movie.isUnreleased.not()
            watched_button.setOnClickListener { onWatchedIt(movie) }
            remove_button.setOnClickListener { onRemove(movie) }
            notification_icon.setOnClickListener { onNotificationToggle(movie) }

            if (movie.isUnreleased) {
                setNotificationIcon(movie)
                setMovieOverlay(movie)
            }
        }

        private fun setNotificationIcon(movie: WatchlistMovieViewEntity) = with(itemView) {
            val isEnabled = NotificationUtils.areNotificationsEnabled(context)
            notification_icon.isVisible = isEnabled

            if (movie.notificationsActivated) {
                notification_icon.setImageResource(R.drawable.ic_notifications_active_white_24dp)
            } else {
                notification_icon.setImageResource(R.drawable.ic_notifications_none_white_24dp)
            }
        }

        private fun setMovieOverlay(movie: WatchlistMovieViewEntity) = with(itemView) {
            val releaseDate = movie.formattedReleaseDate
            runtime_text.text = context.getString(R.string.release_on, releaseDate)
            runtime_icon.setImageResource(R.drawable.ic_today_white_24dp)
            watched_button.isVisible = false
        }

    }

}
