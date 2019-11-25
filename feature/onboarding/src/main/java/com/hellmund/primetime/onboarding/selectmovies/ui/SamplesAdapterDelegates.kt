package com.hellmund.primetime.onboarding.selectmovies.ui

import android.widget.ImageView
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.hellmund.api.model.Sample
import com.hellmund.primetime.ui_common.util.ImageLoader
import com.hellmund.primetime.ui_common.util.Transformation
import com.hellmund.primetime.onboarding.R
import com.hellmund.primetime.ui_common.util.showToast

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
    containerView.setOnClickListener { onItemClick(item.sample) }
    containerView.setOnLongClickListener {
        context.showToast(item.sample.title)
        true
    }

    bind {
        val sample = item.sample
        containerView.alpha = if (sample.selected) 1f else 0.4f

        val posterImageView = containerView.findViewById<ImageView>(R.id.posterImageView)
        imageLoader.load(
            url = sample.fullPosterUrl,
            placeholderResId = R.drawable.poster_placeholder,
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