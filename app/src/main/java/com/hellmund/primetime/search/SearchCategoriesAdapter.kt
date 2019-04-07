package com.hellmund.primetime.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class SearchCategoriesAdapter(
        private val categories: List<String>,
        private val listener: (String) -> Unit
) : RecyclerView.Adapter<SearchCategoriesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], listener)
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(category: String, listener: (String) -> Unit) = with(itemView) {
            val textView = findViewById<TextView>(android.R.id.text1)
            textView.text = category
            setOnClickListener { listener(category) }
        }

    }

}
