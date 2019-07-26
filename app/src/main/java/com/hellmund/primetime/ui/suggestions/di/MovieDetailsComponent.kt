package com.hellmund.primetime.ui.suggestions.di

import com.hellmund.primetime.ui.suggestions.MovieViewEntity
import com.hellmund.primetime.ui.suggestions.details.MovieDetailsFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface MovieDetailsComponent {

    fun inject(movieDetailsFragment: MovieDetailsFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance movie: MovieViewEntity): MovieDetailsComponent
    }

}
