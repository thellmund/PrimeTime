package com.hellmund.primetime.recommendations.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.ui_common.MovieViewEntity

class MoviesAdapter(
    private val imageLoader: ImageLoader,
    private val onClick: (MovieViewEntity.Partial, startView: View) -> Unit,
    private val onLongClick: (MovieViewEntity.Partial) -> Unit
) : RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

    private val items = mutableListOf<MoviesAdapterItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].bind(holder, imageLoader, onClick, onLongClick)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    fun update(movies: List<MovieViewEntity.Partial>) {
        val newItems = if (movies.isNotEmpty()) {
            movies.map { MoviesAdapterItem.Movie.Item(it) } + MoviesAdapterItem.LoadMore
        } else {
            MutableList(25) { MoviesAdapterItem.Movie.Empty }
        }

        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(items, newItems))

        items.clear()
        items += newItems

        diffResult.dispatchUpdatesTo(this)
    }

    class DiffUtilCallback(
        private val oldItems: List<MoviesAdapterItem>,
        private val newItems: List<MoviesAdapterItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            return when {
                oldItem is MoviesAdapterItem.Movie.Item && newItem is MoviesAdapterItem.Movie.Item -> {
                    oldItem.movie.id == newItem.movie.id
                }
                oldItem is MoviesAdapterItem.Movie.Empty && newItem is MoviesAdapterItem.Movie.Empty -> true
                oldItem is MoviesAdapterItem.LoadMore && newItem is MoviesAdapterItem.LoadMore -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            return when {
                oldItem is MoviesAdapterItem.Movie.Item && newItem is MoviesAdapterItem.Movie.Item -> {
                    oldItem.movie == newItem.movie
                }
                oldItem is MoviesAdapterItem.Movie.Empty && newItem is MoviesAdapterItem.Movie.Empty -> true
                oldItem is MoviesAdapterItem.LoadMore && newItem is MoviesAdapterItem.LoadMore -> true
                else -> false
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
