import org.gradle.kotlin.dsl.annotationProcessor
import org.gradle.kotlin.dsl.implementation



plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.aulahub"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.aulahub"
        minSdk = 24
        targetSdk = 36
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
     //BomMde Firebase: fija versiones de todos  los modulos de Firebase
    implementation(platform(libs.firebase.bom))

    //Módulos Firebase SIN version lo pone el BoM
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)

    // Implementacion de la base de datos
    implementation(libs.firebase.firestore)

    //Implementacion de FirebaseStorage
    implementation(libs.firebase.storage)

    //Implementacion de FirebaseMessaging
    implementation(libs.firebase.messaging)


    // AndroidX y tests
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.android.material:material:1.13.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.cardview)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}