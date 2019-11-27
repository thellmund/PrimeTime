package com.hellmund.primetime.ui_common.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellmund.primetime.data.model.Rating
import com.hellmund.primetime.ui_common.R
import com.hellmund.primetime.ui_common.databinding.FragmentRateMovieDialogBinding

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

    private lateinit var binding: FragmentRateMovieDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRateMovieDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        titleTextView.text = title

        positiveButtonText.text = positiveText
        positiveButton.setOnClickListener {
            onItemSelected(Rating.Like)
            dismiss()
        }

        negativeButtonText.text = negativeText
        negativeButton.setOnClickListener {
            onItemSelected(Rating.Dislike)
            dismiss()
        }
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

open class RoundedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }
}
