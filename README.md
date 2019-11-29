# PrimeTime
PrimeTime is an Android app that provides users with movie recommendations and release notifications. This is a work-in-progress where I try out new libraries and architectures. 

<img src="https://user-images.githubusercontent.com/11819826/58164919-081c1c80-7c87-11e9-8335-d349274d3384.png" width="640" />

## Highlights
* Written entirely in [Kotlin](https://kotlinlang.org)
* Modularized by feature with Dagger component dependencies
* Kotlin [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html)
* [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/) for ViewModels and lifecycle awareness
* [Dagger 2](https://dagger.dev) for dependency injection
* [SqlDelight](https://github.com/cashapp/sqldelight) for persistence
* [Ktor](https://ktor.io) for networking
* [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)

## Background
Developing PrimeTime got me started with Android development. 
* 2015: Started working on PrimeTime and released the app on Google Play
* 2019: Rewrote the entire app in Kotlin and introduced Android Architecture Components, RxJava, and Dagger
* 2019, a little later: Introduced modularization and began to use Coroutines, Flow, SqlDelight, and Ktor
