plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Agregamos el plugin KSP para que Room pueda generar código
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" // Ajusta según la versión de Kotlin de tu proyecto
}

android {
    namespace = "com.example.mecanapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mecanapp"
        minSdk = 26
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dependencias de Room agregadas
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Para usar corrutinas
    add("ksp", "androidx.room:room-compiler:$roomVersion")
}