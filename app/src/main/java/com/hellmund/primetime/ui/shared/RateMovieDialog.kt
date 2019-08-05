package com.hellmund.primetime.ui.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.Rating
import kotlinx.android.synthetic.main.fragment_rate_movie_dialog.negativeButton
import kotlinx.android.synthetic.main.fragment_rate_movie_dialog.negativeButtonText
import kotlinx.android.synthetic.main.fragment_rate_movie_dialog.positiveButton
import kotlinx.android.synthetic.main.fragment_rate_movie_dialog.positiveButtonText
import kotlinx.android.synthetic.main.fragment_rate_movie_dialog.titleTextView

class RateMovieDialog(private val activity: FragmentActivity) {

    private var title: String = ""
    private var positiveText: String = ""
    private var negativeText: String = ""
    private var onItemSelected: (Rating) -> Unit = {}

    fun setTitle(title: String): RateMovieDialog {
        this.title = title
        return this
    }

    fun setPositiveText(resId: Int): RateMovieDialog {
        positiveText = activity.getString(resId)
        return this
    }

    fun setNegativeText(resId: Int): RateMovieDialog {
        negativeText = activity.getString(resId)
        return this
    }

    fun onItemSelected(
        onItemSelected: (Rating) -> Unit
    ): RateMovieDialog {
        this.onItemSelected = onItemSelected
        return this
    }

    fun show() {
        val fragment = RateMovieDialogFragment.newInstance(
            title, positiveText, negativeText, onItemSelected)
        fragment.show(activity.supportFragmentManager, fragment.tag)
    }

    companion object {
        fun make(activity: FragmentActivity) = RateMovieDialog(activity)
    }

}

class RateMovieDialogFragment : RoundedBottomSheetDialogFragment() {

    private lateinit var title: String
    private var positiveText: String = ""
    private var negativeText: String = ""
    private var onItemSelected: (Rating) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_rate_movie_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleTextView.text = title
        positiveButton.setOnClickListener { onItemSelected(Rating.Like) }
        positiveButtonText.text = positiveText
        negativeButton.setOnClickListener { onItemSelected(Rating.Dislike) }
        negativeButtonText.text = negativeText
    }

    companion object {
        fun newInstance(
            title: String,
            positiveText: String,
            negativeText: String,
            onItemSelected: (Rating) -> Unit
        ) = RateMovieDialogFragment().apply {
            this.title = title
            this.positiveText = positiveText
            this.negativeText = negativeText
            this.onItemSelected = onItemSelected
        }
    }

}
