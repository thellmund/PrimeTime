package com.hellmund.primetime.moviedetails.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.moviedetails.R
import com.hellmund.primetime.moviedetails.databinding.ListItemRecommendationsBinding
import com.hellmund.primetime.ui_common.MovieViewEntity

class RecommendationsAdapter(
    private val imageLoader: ImageLoader,
    private val onClick: (MovieViewEntity.Partial, View) -> Unit
) : RecyclerView.Adapter<RecommendationsAdapter.ViewHolder>() {

    private val movies = mutableListOf<MovieViewEntity.Partial>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_recommendations, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movies[position], imageLoader, onClick)
    }

    override fun getItemCount(): Int = movies.size

    fun update(newItems: List<MovieViewEntity.Partial>) {
        movies.clear()
        movies += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ListItemRecommendationsBinding.bind(itemView)

        fun bind(
            movie: MovieViewEntity.Partial,
            imageLoader: ImageLoader,
            onClick: (MovieViewEntity.Partial, View) -> Unit
        ) = with(binding) {
            imageLoader.load(
                url = movie.posterUrl,
                placeholderResId = R.drawable.poster_placeholder,
                into = posterImageView
            )

            posterImageView.setOnClickListener {
                onClick(movie, posterImageView)
            }
        }
    }
}
