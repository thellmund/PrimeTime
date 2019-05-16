package com.hellmund.primetime.ui.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.Transformation
import kotlinx.android.synthetic.main.list_item_movies.view.*

sealed class AdapterItem(@LayoutRes val viewType: Int) {

    abstract fun bind(
            holder: MoviesAdapter.ViewHolder,
            onClick: (MovieViewEntity) -> Unit,
            onLongClick: (MovieViewEntity) -> Unit
    )

    data class Movie(val movie: MovieViewEntity) : AdapterItem(R.layout.list_item_movies) {

        override fun bind(
                holder: MoviesAdapter.ViewHolder,
                onClick: (MovieViewEntity) -> Unit,
                onLongClick: (MovieViewEntity) -> Unit
        ) = with(holder.itemView) {
            val transformations: Array<Transformation> =
                    arrayOf(Transformation.Placeholder(R.drawable.poster_placeholder))

            ImageLoader
                    .with(context)
                    .load(
                            url = movie.posterUrl,
                            transformations = transformations,
                            into = posterImageView
                    )

            setOnClickListener { onClick(movie) }
            menuButton.setOnClickListener { onLongClick(movie) }
        }

    }

    object Loading : AdapterItem(R.layout.list_item_load_more) {
        override fun bind(
                holder: MoviesAdapter.ViewHolder,
                onClick: (MovieViewEntity) -> Unit,
                onLongClick: (MovieViewEntity) -> Unit
        ) = Unit
    }

}

class MoviesAdapter(
        private val onClick: (MovieViewEntity) -> Unit,
        private val onMenuClick: (MovieViewEntity) -> Unit
) : RecyclerView.Adapter<MoviesAdapter.ViewHolder>() {

    private val items = mutableListOf<AdapterItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].bind(holder, onClick, onMenuClick)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    fun update(movies: List<MovieViewEntity>) {
        val newItems = if (movies.isNotEmpty()) {
            movies.map { AdapterItem.Movie(it) } + AdapterItem.Loading
        } else emptyList()

        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(items, newItems))

        items.clear()
        items += newItems

        diffResult.dispatchUpdatesTo(this)
    }

    class DiffUtilCallback(
            private val oldItems: List<AdapterItem>,
            private val newItems: List<AdapterItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            return when {
                oldItem is AdapterItem.Movie && newItem is AdapterItem.Movie -> {
                    oldItem.movie.id == newItem.movie.id
                }
                oldItem is AdapterItem.Loading && newItem is AdapterItem.Loading -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]

            return when {
                oldItem is AdapterItem.Movie && newItem is AdapterItem.Movie -> {
                    oldItem.movie == newItem.movie
                }
                oldItem is AdapterItem.Loading && newItem is AdapterItem.Loading -> true
                else -> false
            }
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
