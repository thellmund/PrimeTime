package com.hellmund.primetime.onboarding.ui.selectmovies

import android.widget.ImageView
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateLayoutContainer
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.onboarding.R
import com.hellmund.primetime.onboarding.domain.Sample
import com.hellmund.primetime.ui_common.util.showToast

fun loadMoreAdapterDelegate() = adapterDelegate<SamplesAdapterItem.LoadingMore, SamplesAdapterItem>(
    R.layout.list_item_load_more
) { /* Free ad space */ }

fun emptyMovieAdapterDelegate() = adapterDelegate<SamplesAdapterItem.Movie.Empty, SamplesAdapterItem>(
    R.layout.list_item_samples_list
) { /* Free ad space */ }

fun movieAdapterDelegate(
    imageLoader: ImageLoader,
    onItemClick: (Sample) -> Unit
) = adapterDelegateLayoutContainer<SamplesAdapterItem.Movie.Item, SamplesAdapterItem>(
    R.layout.list_item_samples_list
) {
    containerView.setOnClickListener { onItemClick(item.sample) }
    containerView.setOnLongClickListener {
        context.showToast(item.sample.title)
        true
    }

    bind {
        val sample = item.sample
        containerView.alpha = if (sample.isSelected) 1f else 0.4f

        val posterImageView = containerView.findViewById<ImageView>(R.id.posterImageView)
        imageLoader.load(
            url = sample.posterUrl,
            placeholderResId = R.drawable.poster_placeholder,
            into = posterImageView
        )
    }
}

sealed class SamplesAdapterItem {

    object LoadingMore : SamplesAdapterItem()

    sealed class Movie : SamplesAdapterItem() {
        object Empty : Movie()
        data class Item(val sample: Sample) : Movie()
    }
}
