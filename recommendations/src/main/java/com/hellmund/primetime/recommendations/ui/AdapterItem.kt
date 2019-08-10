package com.hellmund.primetime.recommendations.ui

import androidx.annotation.LayoutRes
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.core.Transformation
import com.hellmund.primetime.moviedetails.ui.MovieViewEntity
import com.hellmund.primetime.recommendations.R
import kotlinx.android.synthetic.main.list_item_movies.view.posterImageView

sealed class AdapterItem(@LayoutRes val viewType: Int) {

    open fun bind(
        holder: MoviesAdapter.ViewHolder,
        imageLoader: ImageLoader,
        onClick: (MovieViewEntity) -> Unit,
        onLongClick: (MovieViewEntity) -> Unit
    ) = Unit

    object LoadMore : AdapterItem(R.layout.list_item_load_more)

    sealed class Movie(viewType: Int) : AdapterItem(viewType) {

        object Empty : Movie(R.layout.list_item_movies) {

            override fun bind(
                holder: MoviesAdapter.ViewHolder,
                imageLoader: ImageLoader,
                onClick: (MovieViewEntity) -> Unit,
                onLongClick: (MovieViewEntity) -> Unit
            ) = with(holder.itemView) {
                posterImageView.setImageResource(0)
            }

        }

        data class Item(val movie: MovieViewEntity) : Movie(R.layout.list_item_movies) {

            override fun bind(
                holder: MoviesAdapter.ViewHolder,
                imageLoader: ImageLoader,
                onClick: (MovieViewEntity) -> Unit,
                onLongClick: (MovieViewEntity) -> Unit
            ) = with(holder.itemView) {
                val transformations: Array<Transformation> =
                    arrayOf(Transformation.Placeholder(R.drawable.poster_placeholder))

                imageLoader.load(
                    url = movie.posterUrl,
                    transformations = transformations,
                    into = posterImageView
                )

                setOnClickListener { onClick(movie) }
                setOnLongClickListener {
                    onLongClick(movie)
                    true
                }
            }

        }

    }

}