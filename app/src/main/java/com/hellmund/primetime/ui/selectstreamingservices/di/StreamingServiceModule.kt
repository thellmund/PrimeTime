package com.hellmund.primetime.ui.selectstreamingservices.di

import com.hellmund.primetime.ui.selectstreamingservices.RealStreamingServicesStore
import com.hellmund.primetime.ui.selectstreamingservices.StreamingServicesStore
import dagger.Binds
import dagger.Module

@Module
interface StreamingServiceModule {

    @Binds
    fun bindStreamingServicesStore(impl: RealStreamingServicesStore): StreamingServicesStore

}
