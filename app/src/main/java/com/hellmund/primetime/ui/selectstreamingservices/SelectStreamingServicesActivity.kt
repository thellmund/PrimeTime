package com.hellmund.primetime.ui.selectstreamingservices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.ui.suggestions.MainActivity
import com.hellmund.primetime.utils.OnboardingHelper
import kotlinx.android.synthetic.main.activity_select_streaming_services.*
import javax.inject.Inject

class SelectStreamingServicesActivity : AppCompatActivity() {

    @Inject
    lateinit var onboardingHelper: OnboardingHelper

    @Inject
    lateinit var streamingServicesStore: StreamingServicesStore

    private val adapter: StreamingServicesAdapter by lazy {
        StreamingServicesAdapter(this::onItemSelected)
    }

    private val streamingServices = mutableListOf(
            StreamingService("Amazon", false),
            StreamingService("iTunes", false),
            StreamingService("Netflix", false)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_streaming_services)
        injector.inject(this)

        setupRecyclerView()
        adapter.update(streamingServices)

        finishButton.setOnClickListener {
            // TODO Save
            storeSelection()
            finishIntro()
        }

        skipButton.setOnClickListener {
            finishIntro()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = LinearLayoutManager(this)

        val spacing = Math.round(resources.getDimension(R.dimen.small_space))
        recyclerView.addItemDecoration(EqualSpacingItemDecoration(spacing))
    }

    private fun onItemSelected(streamingService: StreamingService) {
        val newStreamingService = streamingService.toggled()
        val index = streamingServices.indexOf(streamingService)
        streamingServices[index] = newStreamingService
        adapter.update(streamingServices)

        val selected = streamingServices.filter { it.isSelected }
        finishButton.isEnabled = selected.isNotEmpty()
    }

    private fun storeSelection() {
        streamingServicesStore.store(streamingServices)
    }

    private fun markIntroDone() {
        onboardingHelper.isFirstLaunch = false
    }

    private fun openRecommendations() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun finishIntro() {
        markIntroDone()
        openRecommendations()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SelectStreamingServicesActivity::class.java)
    }

}
