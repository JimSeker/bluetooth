plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.controllersample"
        minSdk = 32
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.example.controllersample"
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
}
