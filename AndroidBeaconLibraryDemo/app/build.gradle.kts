plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.androidbeaconlibrary"
        minSdk = 32
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "edu.cs4730.androidbeaconlibrary"
    buildFeatures {
        viewBinding = true
    }
//    lint {
//        // POST_NOTIFICATIONS is declared in the manifest; the flagged usage is inside
//        // the org.altbeacon library's compiled code, which lint cannot verify further.
//        disable += "NotificationPermission"
//    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // https://mvnrepository.com/artifact/org.altbeacon/android-beacon-library  to find newer version.
    implementation(libs.altbeacon.android.beacon.library)
}
