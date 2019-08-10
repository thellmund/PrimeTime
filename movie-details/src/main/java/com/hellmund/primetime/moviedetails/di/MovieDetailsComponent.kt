package com.hellmund.primetime.moviedetails.di

import com.hellmund.primetime.moviedetails.data.RealMovieDetailsRepository
import com.hellmund.primetime.moviedetails.data.MovieDetailsRepository
import com.hellmund.primetime.moviedetails.ui.MovieDetailsFragment
import com.hellmund.primetime.moviedetails.ui.MovieViewEntity
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@Subcomponent
interface MovieDetailsComponent {

    fun inject(movieDetailsFragment: MovieDetailsFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance movie: MovieViewEntity): MovieDetailsComponent
    }

}

@Module
interface MovieDetailsModule {

    @Binds
    fun bindVideosRepository(impl: RealMovieDetailsRepository): MovieDetailsRepository

}
