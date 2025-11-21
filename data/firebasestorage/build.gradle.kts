plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // аннотации
    id("com.google.devtools.ksp")
    // hilt + dagger
    id("com.google.dagger.hilt.android")
    //firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.firebasestorage"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val accessKey: String = providers.gradleProperty("YC_ACCESS_KEY_ID").get()
        val secretKey: String = providers.gradleProperty("YC_SECRET_KEY").get()

        buildConfigField("String", "YC_ACCESS_KEY_ID", "\"$accessKey\"")
        buildConfigField("String", "YC_SECRET_KEY", "\"$secretKey\"")
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
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //hilt + navigation
    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // firebase storage
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation(project(":core:util"))
    implementation(project(":domain:books"))

    implementation("aws.sdk.kotlin:s3:1.3.6")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.8.0")
}