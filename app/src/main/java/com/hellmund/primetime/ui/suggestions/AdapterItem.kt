package com.hellmund.primetime.ui.suggestions

import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.Transformation
import kotlinx.android.synthetic.main.list_item_movies.view.*

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
                menuButton.isVisible = false
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
                menuButton.isVisible = true

                setOnClickListener { onClick(movie) }
                menuButton.setOnClickListener { onLongClick(movie) }
            }

        }

    }

}
