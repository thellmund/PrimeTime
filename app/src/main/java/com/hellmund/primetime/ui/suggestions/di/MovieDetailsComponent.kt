package com.hellmund.primetime.ui.suggestions.di

import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.details.MovieDetailsFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface MovieDetailsComponent {

    fun inject(movieDetailsFragment: MovieDetailsFragment)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun movie(movie: MovieViewEntity): Builder

        fun build(): MovieDetailsComponent

    }

}
