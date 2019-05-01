package com.hellmund.primetime.ui.history

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.list_item_history.view.*

internal class HistoryAdapter(
        private val listener: (HistoryMovieViewEntity) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val items = mutableListOf<HistoryMovieViewEntity>()

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], listener)
    }

    override fun getItemCount(): Int = items.size

    fun canRemove(): Boolean {
        return itemCount > 4
    }

    fun update(newItems: List<HistoryMovieViewEntity>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(items, newItems))
        items.clear()
        items += newItems
        diffResult.dispatchUpdatesTo(this)
    }

    class DiffUtilCallback(
            private val oldItems: List<HistoryMovieViewEntity>,
            private val newItems: List<HistoryMovieViewEntity>
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
                movie: HistoryMovieViewEntity,
                listener: (HistoryMovieViewEntity) -> Unit
        ) = with(itemView) {
            title.text = movie.title
            subtitle.text = movie.detailsText
            button.setOnClickListener { listener(movie) }
        }

    }

}
