package com.hellmund.primetime.ui.search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.list_item_category.view.*

class SearchCategoriesAdapter(
        private val categories: List<String>,
        private val listener: (String) -> Unit
) : RecyclerView.Adapter<SearchCategoriesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], listener)
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(category: String, listener: (String) -> Unit) = with(itemView) {
            categoryName.text = category
            setOnClickListener { listener(category) }
        }

    }

}
