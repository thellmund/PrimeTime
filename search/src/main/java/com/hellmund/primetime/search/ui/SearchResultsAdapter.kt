package com.hellmund.primetime.search.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.Transformation
import com.hellmund.primetime.search.R
import kotlinx.android.synthetic.main.list_item_search_results.view.description
import kotlinx.android.synthetic.main.list_item_search_results.view.genres
import kotlinx.android.synthetic.main.list_item_search_results.view.posterImageView
import kotlinx.android.synthetic.main.list_item_search_results.view.title

class SearchResultsAdapter(
    private val imageLoader: ImageLoader,
    private val onItemClick: (SearchViewEntity) -> Unit,
    private val onWatched: (SearchViewEntity) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private val items = mutableListOf<SearchViewEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_search_results, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageLoader, items[position], onItemClick, onWatched)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<SearchViewEntity>) {
        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            imageLoader: ImageLoader,
            searchResult: SearchViewEntity,
            onItemClick: (SearchViewEntity) -> Unit,
            onWatchedIt: (SearchViewEntity) -> Unit
        ) = with(itemView) {
            loadImage(imageLoader, searchResult.posterUrl)

            title.text = searchResult.title
            genres.isVisible = searchResult.formattedGenres.isNotBlank()
            genres.text = searchResult.formattedGenres
            description.text = searchResult.description

            setOnClickListener { onItemClick(searchResult) }
            setOnLongClickListener {
                onWatchedIt(searchResult)
                true
            }
        }

        private fun loadImage(imageLoader: ImageLoader, url: String) = with(itemView) {
            val transformations = arrayOf<Transformation>(
                Transformation.Placeholder(R.drawable.poster_placeholder))
            imageLoader.load(url = url, transformations = transformations, into = posterImageView)
        }

    }

}
