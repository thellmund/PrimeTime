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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlin)

    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":common:data"))
    implementation(project(":common:ui"))

    implementation(Libraries.browser)
    implementation(Libraries.palette)
    kapt(Libraries.daggerProcessor)
}
