package com.hellmund.primetime.ui.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.Transformation
import kotlinx.android.synthetic.main.list_item_recommendations.view.posterImageView

class RecommendationsAdapter(
    private val imageLoader: ImageLoader,
    private val onClick: (MovieViewEntity) -> Unit
) : RecyclerView.Adapter<RecommendationsAdapter.ViewHolder>() {

    private val movies = mutableListOf<MovieViewEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_recommendations, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movies[position], imageLoader, onClick)
    }

    override fun getItemCount(): Int = movies.size

    fun update(newItems: List<MovieViewEntity>) {
        movies.clear()
        movies += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            movie: MovieViewEntity,
            imageLoader: ImageLoader,
            onClick: (MovieViewEntity) -> Unit
        ) = with(itemView) {
            val transformations: Array<Transformation> =
                arrayOf(Transformation.Placeholder(R.drawable.poster_placeholder))

            imageLoader.load(
                url = movie.posterUrl,
                transformations = transformations,
                into = posterImageView
            )

            setOnClickListener { onClick(movie) }
        }

    }

}
