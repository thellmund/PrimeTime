package com.hellmund.primetime.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import kotlinx.android.synthetic.main.list_item_search_results.view.*

class SearchAdapter(
        private val context: Context,
        private val onShowSimilar: (SearchResult) -> Unit,
        private val onWatched: (SearchResult) -> Unit
) : BaseAdapter() {

    private val items = mutableListOf<SearchResult>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_search_results, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        holder.bind(context, items[position], onShowSimilar, onWatched)

        return view!!
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun update(newItems: List<SearchResult>) {
        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(private val view: View) {

        fun bind(
                context: Context,
                searchResult: SearchResult,
                onShowSimilar: (SearchResult) -> Unit,
                onWatchedIt: (SearchResult) -> Unit
        ) = with(view) {
            loadImage(context, searchResult)
            title.text = searchResult.title
            description.text = searchResult.description
            similarMoviesButton.setOnClickListener { onShowSimilar(searchResult) }
            watchedItButton.setOnClickListener { onWatchedIt(searchResult) }

        }

        private fun loadImage(context: Context, searchResult: SearchResult) = with(view) {
            ImageLoader.with(context).load(searchResult.fullPosterPath, posterImageView)
        }
    }

}
