package com.hellmund.primetime.ui.main.di

import com.hellmund.primetime.data.model.MovieViewEntity
import com.hellmund.primetime.ui.main.SuggestionFragment
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
