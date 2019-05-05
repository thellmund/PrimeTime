package com.hellmund.primetime.ui.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import kotlinx.android.synthetic.main.list_item_samples_list.view.*

class SuggestionsAdapter2(
        private val onClick: (MovieViewEntity) -> Unit,
        private val onLongClick: (MovieViewEntity) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter2.ViewHolder>() {

    private val items = mutableListOf<MovieViewEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_samples_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onClick, onLongClick)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<MovieViewEntity>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(items, newItems))
        items.clear()
        items += newItems
        diffResult.dispatchUpdatesTo(this)
    }

    class DiffUtilCallback(
            private val oldItems: List<MovieViewEntity>,
            private val newItems: List<MovieViewEntity>
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
                movie: MovieViewEntity,
                onClick: (MovieViewEntity) -> Unit,
                onLongClick: (MovieViewEntity) -> Unit
        ) = with(itemView) {
            ImageLoader.with(context).load(movie.posterUrl, into = posterImageView)
            setOnClickListener { onClick(movie) }
            setOnLongClickListener {
                onLongClick(movie)
                true
            }
        }

    }

}
