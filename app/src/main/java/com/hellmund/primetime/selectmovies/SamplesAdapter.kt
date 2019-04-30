package com.hellmund.primetime.selectmovies

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import com.hellmund.primetime.model.Sample
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.showToast
import kotlinx.android.synthetic.main.list_item_samples_list.view.*

class SamplesAdapter(
        private val onItemClick: (Sample) -> Unit
) : RecyclerView.Adapter<SamplesAdapter.ViewHolder>() {

    val items = mutableListOf<Sample>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_samples_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<Sample>) {
        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Sample, onItemClick: (Sample) -> Unit) = with(itemView) {
            container.alpha = if (item.selected) 1f else 0.4f
            ImageLoader.with(context).load(
                    url = item.fullPosterUrl,
                    into = posterImageView
            )

            container.setOnClickListener { onItemClick(item) }
            container.setOnLongClickListener {
                context.showToast(item.title)
                true
            }
        }

    }

}
