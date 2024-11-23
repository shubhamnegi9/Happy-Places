plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.shubham.happyplaces"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shubham.happyplaces"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // To use the material designs
    implementation("com.google.android.material:material:1.12.0")
    // Dexter Library
    implementation("com.karumi:dexter:6.2.3")
    // Circular Image View Library
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Google Places SDK
    implementation("com.google.android.libraries.places:places:3.3.0")
    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    // To get the location
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Kotlin extension for coroutine support with activities
    implementation("androidx.activity:activity-ktx:1.9.3")
    // AdMob dependency
    implementation("com.google.android.gms:play-services-ads:23.5.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}