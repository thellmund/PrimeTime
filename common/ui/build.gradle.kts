plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
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
    viewBinding {
        isEnabled = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlin)

    // Modules
    implementation(project(":core"))
    implementation(project(":common:data"))

    // AndroidX
    implementation(Libraries.appCompat)
    api(Libraries.constraintLayout)
    api(Libraries.coreKtx)
    api(Libraries.fragmentKtx)
    api(Libraries.material)
    api(Libraries.insetter)
    api(Libraries.insetterKtx)

    // Lifecycle & ViewModel
    api(Libraries.lifecycleViewModel)
    api(Libraries.lifecycleLiveData)
    api(Libraries.lifecycleKtx)
    api(Libraries.lifecycleExtensions)
    api(Libraries.lifecycleCommon)

    // Dagger
    kapt(Libraries.daggerProcessor)

    // Helpers
    api(Libraries.roundedImageView)
    api(Libraries.shimmer)
    api(Libraries.threeTenAbp)
}
