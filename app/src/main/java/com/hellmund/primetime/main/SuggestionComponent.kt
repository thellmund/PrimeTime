package com.hellmund.primetime.main

import com.hellmund.primetime.model.MovieViewEntity
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface SuggestionComponent {

    fun inject(suggestionFragment: SuggestionFragment)

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        fun movie(movie: MovieViewEntity): Builder

        fun build(): SuggestionComponent

    }

}
