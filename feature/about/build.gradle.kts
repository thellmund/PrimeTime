plugins {
    id("com.android.library")
    id("kotlin-android")
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
    viewBinding {
        isEnabled = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlin)

    implementation(project(":common:ui"))

    implementation(Libraries.appCompat)
    implementation(Libraries.coreKtx)
}
