package com.hellmund.primetime.ui.selectmovies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SamplesAdapter(
        private val onItemClick: (Sample) -> Unit
) : RecyclerView.Adapter<SamplesAdapter.ViewHolder>() {

    private val items = mutableListOf<AdapterItem>()

    val selected: List<Sample>
        get() = items
                .mapNotNull { it as? AdapterItem.Movie }
                .map { it.sample }
                .filter { it.selected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].bind(holder, onItemClick)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    fun update(samples: List<Sample>) {
        val newItems = if (samples.isNotEmpty()) {
            samples.map { AdapterItem.Movie(it) } + AdapterItem.Loading
        } else emptyList()

        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
