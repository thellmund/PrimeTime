package com.hellmund.primetime.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.list_item_history.view.button
import kotlinx.android.synthetic.main.list_item_history.view.subtitle
import kotlinx.android.synthetic.main.list_item_history.view.title

internal class HistoryAdapter(
    private val listener: (HistoryMovieViewEntity) -> Unit
) : ListAdapter<HistoryMovieViewEntity, HistoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<HistoryMovieViewEntity>() {
        override fun areItemsTheSame(
            oldItem: HistoryMovieViewEntity,
            newItem: HistoryMovieViewEntity
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: HistoryMovieViewEntity,
            newItem: HistoryMovieViewEntity
        ) = oldItem == newItem
    }
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
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
