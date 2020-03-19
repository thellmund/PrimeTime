package com.hellmund.primetime.search.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.search.R
import com.hellmund.primetime.search.databinding.ListItemSearchResultsBinding
import com.hellmund.primetime.ui_common.MovieViewEntity

class SearchResultsAdapter(
    private val imageLoader: ImageLoader,
    private val onItemClick: (MovieViewEntity.Partial) -> Unit,
    private val onWatched: (MovieViewEntity.Partial) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private val items = mutableListOf<MovieViewEntity.Partial>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_search_results, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageLoader, items[position], onItemClick, onWatched)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<MovieViewEntity.Partial>) {
        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ListItemSearchResultsBinding.bind(itemView)

        fun bind(
            imageLoader: ImageLoader,
            searchResult: MovieViewEntity.Partial,
            onItemClick: (MovieViewEntity.Partial) -> Unit,
            onWatchedIt: (MovieViewEntity.Partial) -> Unit
        ) = with(binding) {
            loadImage(imageLoader, searchResult.posterUrl)

            title.text = searchResult.title
            genres.isVisible = searchResult.formattedGenres.isNotBlank()
            genres.text = searchResult.formattedGenres
            description.text = searchResult.description

            root.setOnClickListener { onItemClick(searchResult) }
            root.setOnLongClickListener {
                onWatchedIt(searchResult)
                true
            }
        }

        private fun loadImage(imageLoader: ImageLoader, url: String) = with(binding) {
            imageLoader.load(
                url = url,
                placeholderResId = R.drawable.poster_placeholder,
                into = posterImageView
            )
        }
    }
}
