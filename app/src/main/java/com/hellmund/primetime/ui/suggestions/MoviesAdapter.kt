package com.hellmund.primetime.ui.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.ui.suggestions.AdapterItem.LoadMore
import com.hellmund.primetime.ui.suggestions.AdapterItem.Movie
import com.hellmund.primetime.utils.ImageLoader

class MoviesAdapter(
    private val imageLoader: ImageLoader,
    private val onClick: (MovieViewEntity) -> Unit,
    private val onMenuClick: (MovieViewEntity) -> Unit
) : ListAdapter<AdapterItem, MoviesAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<AdapterItem>() {
        override fun areItemsTheSame(
            oldItem: AdapterItem,
            newItem: AdapterItem
        ) = when {
            oldItem is Movie.Item && newItem is Movie.Item -> oldItem.movie.id == newItem.movie.id
            else -> oldItem.viewType == newItem.viewType
        }

        override fun areContentsTheSame(
            oldItem: AdapterItem,
            newItem: AdapterItem
        ) = when {
            oldItem is Movie.Item && newItem is Movie.Item -> oldItem.movie == newItem.movie
            else -> oldItem.viewType == newItem.viewType
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).bind(holder, imageLoader, onClick, onMenuClick)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    fun submit(movies: List<MovieViewEntity>) {
        val newItems = if (movies.isNotEmpty()) {
            movies.map { Movie.Item(it) } + LoadMore
        } else {
            MutableList(25) { Movie.Empty }
        }
        submitList(newItems)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
