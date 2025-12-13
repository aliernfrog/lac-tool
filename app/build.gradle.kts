import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.FileInputStream
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory

val localProperties = Properties()
try {
    localProperties.load(FileInputStream(rootProject.file("local.properties")))
} catch (t: Throwable) {
    logger.warn("Failed to load local.properties: ", t)
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "com.aliernfrog.lactool"
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.aliernfrog.lactool"
        minSdk = 21
        targetSdk = 36
        versionCode = 400000
        versionName = "3.5.1"
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

        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "LAC Tool Debug")
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            optIn.add("kotlin.RequiresOptIn")
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
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

val languages = mutableListOf<String>()
val translationProgresses = mutableListOf<Float>()
val resDirPath = "src/main/res"
val baseStrings = "$resDirPath/values/strings.xml"
var translatableStringsCount = 0

// Get translatable strings count from base strings.xml
val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
val baseStringsDoc = documentBuilder.parse(project.file(baseStrings))
baseStringsDoc.documentElement.normalize()
val baseStringsNodeList = baseStringsDoc.getElementsByTagName("string")
for (i in 0 until baseStringsNodeList.length) {
    val node = baseStringsNodeList.item(i)
    if (node.nodeType == Node.ELEMENT_NODE) {
        val element = node as Element
        val translatable = element.getAttribute("translatable")
        if (translatable.isNullOrEmpty() || !translatable.equals("false", ignoreCase = true)) {
            translatableStringsCount++
        }
    }
}

// Get available languages and save it in "LANGUAGES" field of BuildConfig.
// https://stackoverflow.com/a/36047987
fileTree(resDirPath).visit {
    if (file.path.endsWith("strings.xml")) languages.add(
        file.parentFile.name.let {
            val localeName = if (it == "values") "en-US" else file.parentFile.name
                .removePrefix("values-")
                .replace("-r", "-") // "zh-rCN" -> "zh-CN"

            var translatedStringsCount = 0
            try {
                val doc = documentBuilder.parse(file)
                doc.documentElement.normalize()
                val nodeList = doc.getElementsByTagName("string")
                for (i in 0 until nodeList.length) {
                    val node = nodeList.item(i)
                    if (node.nodeType == Node.ELEMENT_NODE) {
                        translatedStringsCount++
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to resolve translated strings count for locale $localeName", e)
            }

            translationProgresses.add(translatedStringsCount.toFloat() / translatableStringsCount.toFloat())
            localeName
        }
    )
}
android.defaultConfig.buildConfigField("String[]", "LANGUAGES", "new String[]{${
    languages.joinToString(",") { "\"$it\"" }
}}")
android.defaultConfig.buildConfigField("float[]", "TRANSLATION_PROGRESSES", "new float[]{${
    translationProgresses.joinToString(",") { "${it}f" }
}}")


val laclibPath: String? = localProperties.getProperty("laclibPath")
val useLocalLaclib = !laclibPath.isNullOrEmpty()

// Utilities to get git environment information
// Source: https://github.com/vendetta-mod/VendettaManager/blob/main/app/build.gradle.kts
fun getCurrentBranch() = exec("git", "symbolic-ref", "--short", "HEAD")
    ?: exec("git", "describe", "--tags", "--exact-match")
fun getLatestCommit() = exec("git", "rev-parse", "--short", "HEAD")
fun hasLocalChanges(): Boolean {
    val branch = getCurrentBranch()
    val uncommittedChanges = exec("git", "status", "-s")?.isNotEmpty() ?: false
    val unpushedChanges = exec("git", "log", "origin/$branch..HEAD")?.isNotBlank() ?: false
    return uncommittedChanges || unpushedChanges || useLocalLaclib
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
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.lifecycle.ktx)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
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

    implementation(
        if (!useLocalLaclib) libs.laclib else {
            println("Using local LACLib")
            files(laclibPath)
        }
    )

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)

    coreLibraryDesugaring(libs.android.desugar)
}
