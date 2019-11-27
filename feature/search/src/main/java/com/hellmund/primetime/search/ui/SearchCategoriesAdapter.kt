package com.hellmund.primetime.search.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.search.R
import com.hellmund.primetime.search.databinding.ListItemCategoryBinding

class SearchCategoriesAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SearchCategoriesAdapter.ViewHolder>() {

    private val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<String>) {
        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ListItemCategoryBinding.bind(itemView)

        fun bind(category: String, listener: (String) -> Unit) = with(binding) {
            categoryName.text = category
            root.setOnClickListener { listener(category) }
        }
    }
}
