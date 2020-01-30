package com.hellmund.primetime.onboarding.ui.selectmovies

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AdapterDelegatesManager
import com.hellmund.primetime.core.ImageLoader
import com.hellmund.primetime.onboarding.domain.Sample

class SamplesAdapter(
    imageLoader: ImageLoader,
    onItemClick: (Sample) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val adapterDelegatesManager = AdapterDelegatesManager<List<SamplesAdapterItem>>()
    private val items = mutableListOf<SamplesAdapterItem>()

    init {
        adapterDelegatesManager += emptyMovieAdapterDelegate()
        adapterDelegatesManager += movieAdapterDelegate(imageLoader, onItemClick)
        adapterDelegatesManager += loadMoreAdapterDelegate()
    }

    val selected: List<Sample>
        get() = items
            .mapNotNull { it as? SamplesAdapterItem.Movie.Item }
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
            samples.map { SamplesAdapterItem.Movie.Item(it) } + SamplesAdapterItem.LoadingMore
        } else {
            MutableList(25) { SamplesAdapterItem.Movie.Empty }
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
