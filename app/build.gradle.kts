plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.todoapp"
    compileSdk = 35



    defaultConfig {
        applicationId = "com.example.todoapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures{
        viewBinding = true
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
    kotlinOptions {
        jvmTarget = "11"
    }



}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.mediation.test.suite)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.espresso.core)
    implementation(libs.play.services.fido)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    implementation (libs.firebase.auth.ktx.v2101)
    implementation (libs.play.services.auth)
    implementation (libs.play.services.ads)
    androidTestImplementation(libs.androidx.junit)
    implementation (libs.firebase.auth.ktx.v2105) // Firebase Auth
    implementation(libs.firebase.bom.v3121 )// Firebase BoM
    androidTestImplementation(libs.androidx.espresso.core)
    implementation (libs.androidx.work.runtime.ktx)
        // Import the BoM for the Firebase platform
    implementation(libs.google.firebase.bom)

        // Add the dependency for the Firebase Authentication library
        // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.auth)


    // ... other dependencies

    implementation (libs.androidx.credentials)
    implementation (libs.androidx.credentials.play.services.auth)
    implementation (libs.googleid)
    implementation (libs.androidx.credentials.v120)
    implementation (libs.googleid.v110)


    implementation (libs.play.services.auth.v2010)


    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation(libs.androidx.credentials.v130)
    implementation(libs.androidx.credentials.play.services.auth.v130)
    implementation(libs.googleid.v111)




}
apply(plugin = "com.google.gms.google-services")

