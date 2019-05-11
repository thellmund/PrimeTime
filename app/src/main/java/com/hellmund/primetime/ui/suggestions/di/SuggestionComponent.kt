package com.hellmund.primetime.ui.suggestions.di

import com.hellmund.primetime.ui.suggestions.details.MovieDetailsFragment
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface SuggestionComponent {

    fun inject(movieDetailsFragment: MovieDetailsFragment)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun movie(movie: MovieViewEntity): Builder

        fun build(): SuggestionComponent

    }

}
