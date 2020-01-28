package com.hellmund.primetime.history.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.history.R
import com.hellmund.primetime.history.databinding.ListItemHistoryBinding

class HistoryAdapter(
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

        private val binding = ListItemHistoryBinding.bind(itemView)

        fun bind(
            movie: HistoryMovieViewEntity,
            listener: (HistoryMovieViewEntity) -> Unit
        ) = with(binding) {
            title.text = movie.title
            subtitle.text = movie.detailsText
            button.setOnClickListener { listener(movie) }
        }
    }
}
