package com.hellmund.primetime.di

import com.hellmund.primetime.core.App
import com.hellmund.primetime.core.di.ActivityScope
import com.hellmund.primetime.core.di.CoreComponent
import com.hellmund.primetime.core.notifications.NotificationReceiver
import com.hellmund.primetime.ui.MainActivity
import dagger.Component

@ActivityScope
@Component(
    dependencies = [CoreComponent::class]
)
interface AppComponent {
    fun inject(app: App)
    fun inject(mainActivity: MainActivity)
    fun inject(notificationReceiver: NotificationReceiver)
}
