package com.hellmund.primetime.ui.selectmovies

import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.Transformation
import com.hellmund.primetime.utils.showToast
import kotlinx.android.synthetic.main.list_item_samples_list.view.*

sealed class AdapterItem(val viewType: Int) {

    open fun bind(
            holder: SamplesAdapter.ViewHolder,
            onItemClick: (Sample) -> Unit
    ) = Unit

    object LoadingMore : AdapterItem(R.layout.list_item_load_more)

    sealed class Movie(viewType: Int) : AdapterItem(viewType) {

        object Empty : Movie(R.layout.list_item_samples_list)

        data class Item(
                val sample: Sample
        ) : Movie(R.layout.list_item_samples_list) {

            override fun bind(
                    holder: SamplesAdapter.ViewHolder,
                    onItemClick: (Sample) -> Unit
            ) = with(holder.itemView) {
                container.alpha = if (sample.selected) 1f else 0.4f
                ImageLoader.with(context).load(
                        url = sample.fullPosterUrl,
                        transformations = arrayOf(Transformation.Placeholder(R.drawable.poster_placeholder)),
                        into = posterImageView
                )

                container.setOnClickListener { onItemClick(sample) }
                container.setOnLongClickListener {
                    context.showToast(sample.title)
                    true
                }
            }

        }

    }

}
