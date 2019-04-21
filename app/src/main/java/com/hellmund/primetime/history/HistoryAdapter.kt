package com.hellmund.primetime.history

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import com.hellmund.primetime.database.HistoryMovie
import com.hellmund.primetime.utils.showToast
import kotlinx.android.synthetic.main.list_item_history.view.*

internal class HistoryAdapter(
        private val mContext: Context,
        private val mItems: List<HistoryMovie>,
        private val listener: (HistoryMovie) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): HistoryAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
        holder.bind(mItems[position], listener)
    }

    override fun getItemCount(): Int = mItems.size

    private fun canRemove(): Boolean {
        return itemCount > 4
    }

    fun removeItem(position: Int) {
        if (!canRemove()) {
            mContext.showToast(R.string.cant_remove_more_items)
            this.notifyDataSetChanged()
            return
        }

        val movie = mItems[position]

        // TODO
        // mItems.removeAt(position)
        // History.remove(movie.getID());

        notifyItemRemoved(position)
        displayDeleteSnackbar(movie, position)
    }

    private fun displayDeleteSnackbar(movie: HistoryMovie, position: Int) {
        /*Snackbar.make(mParent.getContainer(), R.string.removed_from_history, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) { v ->
                    //History.addHistoryMovie(movie);
                    mItems.add(position, movie)
                    notifyItemInserted(position)
                }
                .setActionTextColor(UiUtils.getSnackbarColor(mContext))
                .show()*/
    }

    interface OnInteractionListener {
        val container: View
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(movie: HistoryMovie, listener: (HistoryMovie) -> Unit) = with(itemView) {
            title.text = movie.title

            if (movie.isUpdating) {
                subtitle.setText(R.string.updating_rating)
                subtitle.setTypeface(null, Typeface.ITALIC)
            } else {
                subtitle.text = movie.getDetailsText(context)
                subtitle.setTypeface(null, Typeface.NORMAL)
            }

            button.setOnClickListener { listener(movie) }
        }

    }

}
