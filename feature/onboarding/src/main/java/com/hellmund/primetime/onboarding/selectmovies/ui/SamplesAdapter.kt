package com.hellmund.primetime.onboarding.selectmovies.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.onboarding.selectmovies.domain.Sample

class SamplesAdapter(
    imageLoader: ImageLoader,
    onItemClick: (Sample) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val adapterDelegatesManager = AdapterDelegatesManager<List<AdapterItem>>()
    private val items = mutableListOf<AdapterItem>()

    init {
        adapterDelegatesManager += emptyMovieAdapterDelegate()
        adapterDelegatesManager += movieAdapterDelegate(imageLoader, onItemClick)
        adapterDelegatesManager += loadMoreAdapterDelegate()
    }

    val selected: List<Sample>
        get() = items
            .mapNotNull { it as? AdapterItem.Movie.Item }
            .map { it.sample }
            .filter { it.isSelected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return adapterDelegatesManager.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        adapterDelegatesManager.onBindViewHolder(items, position, holder)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(
        position: Int
    ) = adapterDelegatesManager.getItemViewType(items, position)

    fun update(samples: List<Sample>) {
        val newItems = if (samples.isNotEmpty()) {
            samples.map { AdapterItem.Movie.Item(it) } + AdapterItem.LoadingMore
        } else {
            MutableList(25) { AdapterItem.Movie.Empty }
        }

        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private operator fun <T> AdapterDelegatesManager<T>.plusAssign(delegate: AdapterDelegate<T>) {
        addDelegate(delegate)
    }
}
