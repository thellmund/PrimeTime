plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
    id("com.squareup.sqldelight")
}

android {
    compileSdkVersion(BuildConfigVersions.compileSdk)
    defaultConfig {
        minSdkVersion(BuildConfigVersions.minSdk)
        targetSdkVersion(BuildConfigVersions.targetSdk)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    androidExtensions {
        isExperimental = true
    }
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlin)

    implementation(project(":api"))

    // Dagger
    api(Libraries.dagger)
    kapt(Libraries.daggerProcessor)

    // SqlDelight
    implementation(Libraries.sqlDelightDriver)
    implementation(Libraries.sqlDelightCoroutines)
    implementation(Libraries.sqlDelightRuntime)
}
