plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val composeMaterialVersion = "1.6.0"
val composeMaterial3Version = "1.2.0-rc01"
val composeCompilerVersion = "1.5.8"

android {
    namespace = "com.aliernfrog.lactool"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aliernfrog.lactool"
        minSdk = 23
        targetSdk = 34
        versionCode = 31000
        versionName = "3.1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    androidResources {
        generateLocaleConfig = true
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
        buildConfig = true
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

// Get available languages and save it in "LANGUAGES" field of BuildConfig.
// https://stackoverflow.com/a/36047987
val languages = mutableListOf<String>()
fileTree("src/main/res").visit {
    if (file.path.endsWith("strings.xml")) languages.add(
        file.parentFile.name.let {
            if (it == "values") "en-US"
            else file.parentFile.name
                .removePrefix("values-")
                .replace("-r","-") // "zh-rCN" -> "zh-CN"
        }
    )
}
android.defaultConfig.buildConfigField("String[]", "LANGUAGES", "new String[]{${
    languages.joinToString(",") { "\"$it\"" }
}}")

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.compose.ui:ui:$composeMaterialVersion")
    implementation("androidx.compose.material:material:$composeMaterialVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeMaterialVersion")
    implementation("androidx.compose.material3:material3:$composeMaterial3Version")
    implementation("androidx.compose.material3:material3-window-size-class:$composeMaterial3Version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")
    implementation("aliernfrog:top-toast-compose:1.4.0-alpha04")
    implementation("com.github.aliernfrog:laclib:1.1.0")
    implementation("com.lazygeniouz:dfc:1.0.8")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.github.jeziellago:compose-markdown:0.3.7")
}