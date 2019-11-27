plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlin)

    api(Libraries.ktorCore)
    api(Libraries.ktorGson)
    api(Libraries.ktorOkHttp)

    api(Libraries.okHttpLogging)

    implementation(Libraries.dagger)
    kapt(Libraries.daggerProcessor)

    api(Libraries.threeTenAbp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
