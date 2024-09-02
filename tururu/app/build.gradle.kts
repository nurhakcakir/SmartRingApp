plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Bu satırı ekleyin
}

android {
    namespace = "com.example.tururu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tururu"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.google.material)
    implementation(libs.firebase.database)
    implementation(libs.annotation.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.yarolegovich:sliding-root-nav:1.1.1")

    implementation(files("libs/android-smartband-sdk-sxrblejy2aidl-release.aar"))
    implementation(files("libs/android-smartband-sdk-sxrblejy2library-release.aar"))
}
