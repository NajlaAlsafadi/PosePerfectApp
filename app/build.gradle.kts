plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.poseperfect"
    compileSdk = 34  // updated this line

    defaultConfig {
        applicationId = "com.example.poseperfect"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database-ktx:20.2.2")
    implementation("com.google.firebase:firebase-database:20.2.2")
    implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")
    implementation("com.google.firebase:firebase-auth:22.1.2")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.camera:camera-view:1.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.mlkit:pose-detection:18.0.0-beta3")
    implementation ("com.google.mlkit:pose-detection-accurate:18.0.0-beta3")
    implementation ("androidx.camera:camera-core:1.1.0-alpha05")
    implementation ("androidx.camera:camera-camera2:1.1.0-alpha05")
    implementation ("androidx.camera:camera-lifecycle:1.1.0-alpha05")
    implementation ("androidx.camera:camera-view:1.0.0-alpha27")
    implementation ("com.google.mlkit:pose-detection:17.0.1-beta1")
    implementation ("me.relex:circleindicator:2.1.6")
    implementation ("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.airbnb.android:lottie:3.7.0")
    implementation ("com.google.firebase:firebase-storage-ktx:19.2.2")
    implementation ("androidx.activity:activity-ktx:1.2.3")
    implementation ("androidx.fragment:fragment-ktx:1.3.6")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")



}