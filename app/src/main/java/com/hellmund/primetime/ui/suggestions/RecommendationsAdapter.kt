package com.hellmund.primetime.ui.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import kotlinx.android.synthetic.main.list_item_samples_list.view.*

// TODO: Make this into single MoviesAdapter, used in multiple places
class RecommendationsAdapter(
        private val onClick: (MovieViewEntity) -> Unit
) : RecyclerView.Adapter<RecommendationsAdapter.ViewHolder>() {

    private val movies = mutableListOf<MovieViewEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_recommendations, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movies[position], onClick)
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
                onClick: (MovieViewEntity) -> Unit
        ) = with(itemView) {
            ImageLoader.with(context).load(movie.posterUrl, into = posterImageView)
            setOnClickListener { onClick(movie) }
        }

    }

}
