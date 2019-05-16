package com.hellmund.primetime.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_category.view.*

class SearchCategoriesAdapter(
        private val categories: List<String>,
        private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SearchCategoriesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(com.hellmund.primetime.R.layout.list_item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], onItemClick)
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(category: String, listener: (String) -> Unit) = with(itemView) {
            categoryName.text = category
            setOnClickListener { listener(category) }
        }

    }

}
