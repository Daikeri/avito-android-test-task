    plugins {
        alias(libs.plugins.android.application)
        alias(libs.plugins.kotlin.android)
        alias(libs.plugins.kotlin.compose)
        // сериализация для навигации
        kotlin("plugin.serialization") version "2.0.21"
        // аннотации
        id("com.google.devtools.ksp")
        // hilt + dagger
        id("com.google.dagger.hilt.android")
        // firebase
        id("com.google.gms.google-services")
        // хранение ключей
        id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    }

    android {
        namespace = "com.example.avitotask"
        compileSdk = 35

        defaultConfig {
            applicationId = "com.example.avitotask"
            minSdk = 26
            targetSdk = 35
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
        kotlinOptions {
            jvmTarget = "11"
        }
        buildFeatures {
            buildConfig = true
            compose = true
        }
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.media3.ui)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)

        //hilt + navigation
        implementation("com.google.dagger:hilt-android:2.56.2")
        ksp("com.google.dagger:hilt-android-compiler:2.56.2")
        implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

        implementation(project(":feature:auth"))
        implementation(project(":core:util"))
        implementation(project(":domain:auth"))
        implementation(project(":data:firebaseauth"))
        implementation(project(":feature:uploadbooks"))
        implementation(project(":feature:listofbooks"))
        implementation(project(":feature:userprofile"))


        implementation("androidx.navigation:navigation-compose:2.9.6")

        implementation("androidx.compose.material:material-icons-extended")
    }