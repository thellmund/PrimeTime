package com.hellmund.primetime.ui.suggestions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.fragment_discover_more.*

class DiscoverMoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_discover_more, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        discoverMoreButton.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.openSearchFromIntent()
        }
    }

    companion object {
        fun newInstance(): DiscoverMoreFragment = DiscoverMoreFragment()
    }

}
