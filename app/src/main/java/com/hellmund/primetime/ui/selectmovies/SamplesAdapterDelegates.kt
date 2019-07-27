package com.hellmund.primetime.ui.selectmovies

import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.Transformation
import com.hellmund.primetime.utils.showToast
import kotlinx.android.synthetic.main.list_item_samples_list.container
import kotlinx.android.synthetic.main.list_item_samples_list.posterImageView

fun loadMoreAdapterDelegate() = adapterDelegate<AdapterItem.LoadingMore, AdapterItem>(
    R.layout.list_item_load_more
) { /* Free ad space */ }

fun emptyMovieAdapterDelegate() = adapterDelegate<AdapterItem.Movie.Empty, AdapterItem>(
    R.layout.list_item_samples_list
) { /* Free ad space */ }

fun movieAdapterDelegate(
    imageLoader: ImageLoader,
    onItemClick: (Sample) -> Unit
) = adapterDelegateLayoutContainer<AdapterItem.Movie.Item, AdapterItem>(
    R.layout.list_item_samples_list
) {
    container.setOnClickListener { onItemClick(item.sample) }
    container.setOnLongClickListener {
        context.showToast(item.sample.title)
        true
    }

    bind {
        val sample = item.sample
        container.alpha = if (sample.selected) 1f else 0.4f

        imageLoader.load(
            url = sample.fullPosterUrl,
            transformations = arrayOf(Transformation.Placeholder(R.drawable.poster_placeholder)),
            into = posterImageView
        )
    }
}

sealed class AdapterItem {

    object LoadingMore : AdapterItem()

    sealed class Movie : AdapterItem() {
        object Empty : Movie()
        data class Item(val sample: Sample) : Movie()
    }

}
