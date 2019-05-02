package com.hellmund.primetime.ui.selectstreamingservices

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.list_item_streaming_service.view.*

class StreamingServicesAdapter(
        private val onItemSelected: (streamingService: StreamingService) -> Unit
) : RecyclerView.Adapter<StreamingServicesAdapter.ViewHolder>() {

    val items = mutableListOf<StreamingService>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_streaming_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onItemSelected)
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<StreamingService>) {
        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
                streamingService: StreamingService,
                onItemSelected: (streamingService: StreamingService) -> Unit
        ) = with(itemView) {
            container.isSelected = streamingService.isSelected
            checkBox.isChecked = streamingService.isSelected
            serviceName.text = streamingService.name

            serviceName.typeface = if (streamingService.isSelected) {
                Typeface.create("sans-serif-medium", Typeface.NORMAL)
            } else {
                Typeface.create("sans-serif", Typeface.NORMAL)
            }

            setOnClickListener { onItemSelected(streamingService) }
        }

    }

}
