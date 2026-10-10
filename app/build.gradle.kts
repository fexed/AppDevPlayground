plugins {
    // AGP supplies Kotlin support; the remaining plugins enable Compose, code generation, and DI.
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.kotlin.compose)
    alias(libs.plugins.android.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.fexed.playground"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.fexed.playground"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    // Keep Java and Kotlin targets aligned for predictable Android runtime compatibility.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        // Enables Compose compiler integration and related Android build tasks.
        compose = true
    }
}

// Exporting the Room schema makes database evolution reviewable and supports migration testing.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // BOM aligns Compose libraries to a compatible family of versions.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Lifecycle integration connects Flow collection and ViewModels to Android lifecycle owners.
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Room provides local persistence; KSP generates and validates its DAO implementation.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // Hilt composes dependencies between layers; KSP generates its injection graph code.
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}