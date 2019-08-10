package com.hellmund.primetime.ui.selectstreamingservices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.hellmund.primetime.R
import com.hellmund.primetime.di.injector
import com.hellmund.primetime.ui.shared.EqualSpacingItemDecoration
import com.hellmund.primetime.ui.MainActivity
import com.hellmund.primetime.onboarding.OnboardingHelper
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

    private val streamingServices: MutableList<StreamingService> by lazy {
        streamingServicesStore.all.toMutableList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_streaming_services)
        injector.inject(this)

        setupRecyclerView()
        adapter.update(streamingServices)
        updateButton()

        button.setOnClickListener {
            storeSelection()
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
        val services = streamingServices

        val index = services.indexOf(streamingService)
        services[index] = newStreamingService
        adapter.update(services)

        updateButton()
    }

    private fun updateButton() {
        val selected = streamingServices.filter { it.isSelected }
        button.isSelected = selected.isNotEmpty()

        when (selected.size) {
            0 -> button.setText(R.string.skip)
            else -> button.setText(R.string.finish)
        }
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
