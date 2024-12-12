plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "com.aliernfrog.lactool"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aliernfrog.lactool"
        minSdk = 21
        targetSdk = 35
        versionCode = 34300
        versionName = "3.4.3"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }

    buildFeatures {
        aidl = true
        buildConfig = true
        compose = true
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

// Utilities to get git environment information
// Source: https://github.com/vendetta-mod/VendettaManager/blob/main/app/build.gradle.kts
fun getCurrentBranch() = exec("git", "symbolic-ref", "--short", "HEAD")
    ?: exec("git", "describe", "--tags", "--exact-match")
fun getLatestCommit() = exec("git", "rev-parse", "--short", "HEAD")
fun hasLocalChanges(): Boolean {
    val branch = getCurrentBranch()
    val uncommittedChanges = exec("git", "status", "-s")?.isNotEmpty() ?: false
    val unpushedChanges = exec("git", "log", "origin/$branch..HEAD")?.isNotBlank() ?: false
    return uncommittedChanges || unpushedChanges
}

android.defaultConfig.run {
    buildConfigField("String", "GIT_BRANCH", "\"${getCurrentBranch()}\"")
    buildConfigField("String", "GIT_COMMIT", "\"${getLatestCommit()}\"")
    buildConfigField("boolean", "GIT_LOCAL_CHANGES", "${hasLocalChanges()}")
}

fun exec(vararg command: String) = try {
    val process = ProcessBuilder(command.toList())
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    val stdout = process.inputStream.bufferedReader().readText()
    val stderr = process.errorStream.bufferedReader().readText()
    if (stderr.isNotEmpty()) throw Error(stderr)
    stdout.trim()
} catch (_: Throwable) {
    null
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.lifecycle.ktx)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.splashscreen)

    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.window)

    implementation(libs.aboutlibraries)
    implementation(libs.coil)
    implementation(libs.coil.okhttp)
    implementation(libs.dfc)
    implementation(libs.koin)
    implementation(libs.markdown)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.toptoast)
    implementation(libs.zoomable)

    implementation(project.properties["laclibPath"]?.toString().let {
        if (it.isNullOrEmpty()) libs.laclib else files(it)
    })

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)

    coreLibraryDesugaring(libs.android.desugar)
}
