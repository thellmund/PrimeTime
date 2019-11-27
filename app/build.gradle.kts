plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(BuildConfigVersions.compileSdk)
    defaultConfig {
        applicationId = "com.hellmund.primetime"
        minSdkVersion(BuildConfigVersions.minSdk)
        targetSdkVersion(BuildConfigVersions.targetSdk)
        versionCode = 300
        versionName = "3.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
        exclude("META-INF/services/javax.annotation.processing.Processor")
        exclude("META-INF/*.kotlin_module")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlin)

    // Modules
    implementation(project(":api"))
    implementation(project(":core"))
    implementation(project(":common:data"))
    implementation(project(":common:ui"))
    implementation(project(":feature:about"))
    implementation(project(":feature:history"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:movie-details"))
    implementation(project(":feature:recommendations"))
    implementation(project(":feature:search"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:watchlist"))

    // AndroidX
    implementation(Libraries.palette)
    implementation(Libraries.viewPager)

    // Dagger
    implementation(Libraries.dagger)
    kapt(Libraries.daggerProcessor)

    implementation(Libraries.bottomNavigator)
    implementation(Libraries.preference)

    // WorkManager
    implementation(Libraries.workManager)
    implementation(Libraries.workManagerKtx)

    // Testing
    testImplementation(Libraries.jUnit)
    testImplementation(Libraries.mockitoCore)
    testImplementation(Libraries.mockitoInline)
}
