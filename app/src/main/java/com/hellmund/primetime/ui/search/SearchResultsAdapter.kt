package com.hellmund.primetime.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.Transformation
import com.hellmund.primetime.utils.showInfoBox
import kotlinx.android.synthetic.main.list_item_search_results.view.*

class SearchResultsAdapter(
        private val imageLoader: ImageLoader,
        private val onShowSimilar: (MovieViewEntity) -> Unit,
        private val onWatched: (MovieViewEntity) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private val items = mutableListOf<MovieViewEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_search_results, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageLoader, items[position], onShowSimilar, onWatched)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<MovieViewEntity>) {
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
                searchResult: MovieViewEntity,
                onShowSimilar: (MovieViewEntity) -> Unit,
                onWatchedIt: (MovieViewEntity) -> Unit
        ) = with(itemView) {
            loadImage(imageLoader, searchResult.posterUrl)
            title.text = searchResult.title
            genres.isVisible = searchResult.formattedGenres.isNotBlank()
            genres.text = searchResult.formattedGenres
            description.text = searchResult.description
            similarMoviesButton.setOnClickListener { onShowSimilar(searchResult) }
            watchedItButton.setOnClickListener { onWatchedIt(searchResult) }
            setOnClickListener { context.showInfoBox(searchResult.description) }
        }

        private fun loadImage(imageLoader: ImageLoader, url: String) = with(itemView) {
            val transformations = arrayOf<Transformation>(
                    Transformation.Placeholder(R.drawable.poster_placeholder))
            imageLoader.load(url = url, transformations = transformations, into = posterImageView)
        }

    }

}
