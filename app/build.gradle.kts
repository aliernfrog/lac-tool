plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val composeVersion = "1.5.0-beta01"
val composeCompilerVersion = "1.4.7"

android {
    namespace = "com.aliernfrog.lactool"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.aliernfrog.lactool"
        minSdk = 23
        targetSdk = 33
        versionCode = 202
        versionName = "3.0.0-alpha02"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("androidx.compose.material3:material3:1.2.0-alpha02")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.1")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.31.2-alpha")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.2-alpha")
    implementation("com.github.aliernfrog:top-toast-compose:1.3.1")
    implementation("com.github.aliernfrog:laclib:1.1.0")
    implementation("com.lazygeniouz:dfc:1.0.4")
    implementation("io.coil-kt:coil-compose:2.3.0")
    implementation("com.github.jeziellago:compose-markdown:0.3.3")
}