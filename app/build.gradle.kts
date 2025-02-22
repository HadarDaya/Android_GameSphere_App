plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.gamesphere"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gamesphere"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.lottie)
    implementation (libs.material)
    implementation(libs.glide)

    // Cloudinary- for images upload
    implementation("com.cloudinary:cloudinary-android:1.24.0") {
        exclude(group = "com.android.support", module = "support-compat")
    }

    // Flexbox- for displaying tags of genres
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    // AndroidYoutubePlayer
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
}