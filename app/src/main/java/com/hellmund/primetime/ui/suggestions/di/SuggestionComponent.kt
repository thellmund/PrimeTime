package com.hellmund.primetime.ui.suggestions.di

import com.hellmund.primetime.ui.suggestions.MovieDetailsFragment
import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.SuggestionFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface SuggestionComponent {

    fun inject(movieDetailsFragment: MovieDetailsFragment)
    fun inject(suggestionFragment: SuggestionFragment)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun movie(movie: MovieViewEntity): Builder

        fun build(): SuggestionComponent

    }

}
