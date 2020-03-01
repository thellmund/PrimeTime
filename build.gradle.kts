// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:${Versions.ktlint}")
        classpath("com.github.ben-manes:gradle-versions-plugin:${Versions.gradleVersionsPlugin}")
    }
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.ben-manes.versions")
    repositories {
        jcenter()
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}
