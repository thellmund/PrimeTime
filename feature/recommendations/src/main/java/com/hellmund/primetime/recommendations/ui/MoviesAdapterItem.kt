package com.hellmund.primetime.recommendations.ui

import android.view.View
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.recommendations.R
import com.hellmund.primetime.ui_common.MovieViewEntity

sealed class MoviesAdapterItem(@LayoutRes val viewType: Int) {

    open fun bind(
        holder: MoviesAdapter.ViewHolder,
        imageLoader: ImageLoader,
        onClick: (MovieViewEntity.Partial, startView: View) -> Unit,
        onLongClick: (MovieViewEntity.Partial) -> Unit
    ) = Unit

    object LoadMore : MoviesAdapterItem(R.layout.list_item_load_more)

    sealed class Movie(viewType: Int) : MoviesAdapterItem(viewType) {

        object Empty : Movie(R.layout.list_item_movies) {

            override fun bind(
                holder: MoviesAdapter.ViewHolder,
                imageLoader: ImageLoader,
                onClick: (MovieViewEntity.Partial, startView: View) -> Unit,
                onLongClick: (MovieViewEntity.Partial) -> Unit
            ) = with(holder.itemView) {
                val posterImageView = findViewById<ImageView>(R.id.posterImageView)
                posterImageView.setImageResource(0)
            }
        }

        data class Item(val movie: MovieViewEntity.Partial) : Movie(R.layout.list_item_movies) {

            override fun bind(
                holder: MoviesAdapter.ViewHolder,
                imageLoader: ImageLoader,
                onClick: (MovieViewEntity.Partial, startView: View) -> Unit,
                onLongClick: (MovieViewEntity.Partial) -> Unit
            ) = with(holder.itemView) {
                val posterImageView = findViewById<ImageView>(R.id.posterImageView)
                imageLoader.load(
                    url = movie.posterUrl,
                    placeholderResId = R.drawable.poster_placeholder,
                    into = posterImageView
                )

                setOnClickListener { onClick(movie, posterImageView) }
                setOnLongClickListener {
                    onLongClick(movie)
                    true
                }
            }
        }
    }
}
