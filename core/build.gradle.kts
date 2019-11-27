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
        consumerProguardFile("consumer-rules.pro")
        buildConfigField("String", "TMDB_API_KEY", "${properties["TMDB_API_KEY"]}")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlin)

    implementation(project(":api"))
    implementation(project(":common:data"))

    // AndroidX
    api(Libraries.appCompat)
    implementation(Libraries.preference)

    // Dagger
    api(Libraries.dagger)
    kapt(Libraries.daggerProcessor)

    // Coroutines
    api(Libraries.coroutines)
    api(Libraries.coroutinesAndroid)

    // Helpers
    implementation(Libraries.picasso)
    implementation(Libraries.timber)
    api(Libraries.threeTenAbp)

    // WorkManager
    implementation(Libraries.workManager)
    implementation(Libraries.workManagerKtx)
}
