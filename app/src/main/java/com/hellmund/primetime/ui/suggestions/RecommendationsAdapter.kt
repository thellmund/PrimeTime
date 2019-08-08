package com.hellmund.primetime.ui.suggestions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.Transformation
import kotlinx.android.synthetic.main.list_item_samples_list.view.posterImageView

class RecommendationsAdapter(
    private val imageLoader: ImageLoader,
    private val onClick: (MovieViewEntity) -> Unit
) : ListAdapter<MovieViewEntity, RecommendationsAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<MovieViewEntity>() {
        override fun areItemsTheSame(
            oldItem: MovieViewEntity,
            newItem: MovieViewEntity
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: MovieViewEntity,
            newItem: MovieViewEntity
        ) = oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_recommendations, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), imageLoader, onClick)
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
