import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.FileInputStream
import java.util.Properties
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

val localProperties = Properties()
try {
    localProperties.load(FileInputStream(rootProject.file("local.properties")))
} catch (_: java.io.FileNotFoundException) {
    // ignore
} catch (t: Throwable) {
    logger.warn("Failed to load local.properties: ", t)
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.aboutlibraries.android)
}

android {
    namespace = "com.aliernfrog.lactool"
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.aliernfrog.lactool"
        minSdk = 23
        targetSdk = 36
        versionCode = 400000
        versionName = "4.0.0"
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


// Utilities to get git environment information
// Source: https://github.com/vendetta-mod/VendettaManager/blob/main/app/build.gradle.kts
var usingLocalLibraries = false
fun getCurrentBranch() = exec("git", "symbolic-ref", "--short", "HEAD")
    ?: exec("git", "describe", "--tags", "--exact-match")
fun getLatestCommit() = exec("git", "rev-parse", "--short", "HEAD")
fun hasLocalChanges(): Boolean {
    val branch = getCurrentBranch()
    val uncommittedChanges = exec("git", "status", "-s")?.isNotEmpty() ?: false
    val unpushedChanges = exec("git", "log", "origin/$branch..HEAD")?.isNotBlank() ?: false
    return uncommittedChanges || unpushedChanges || usingLocalLibraries
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

    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.core)
    implementation(libs.coil)
    implementation(libs.coil.okhttp)
    implementation(libs.dfc)
    implementation(libs.koin)
    implementation(libs.markdown)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.toptoast)
    implementation(libs.zoomable)

    listOf(
        "laclibPath" to libs.laclib,
        "pftoolSharedBaseLibPath" to libs.pftool.shared.base,
        "pftoolSharedExtraLibPath" to libs.pftool.shared.extra
    ).forEach { (name, defaultLib) ->
        val localPath = localProperties.getProperty(name)
        implementation(
            if (localPath.isNullOrEmpty()) defaultLib
            else {
                usingLocalLibraries = true
                println("Using local dependency: $name")
                files(localPath)
            }
        )
    }

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)

    coreLibraryDesugaring(libs.android.desugar)
}

android.defaultConfig.run {
    buildConfigField("String", "GIT_BRANCH", "\"${getCurrentBranch()}\"")
    buildConfigField("String", "GIT_COMMIT", "\"${getLatestCommit()}\"")
    buildConfigField("boolean", "GIT_LOCAL_CHANGES", "${hasLocalChanges()}")
}

val sharedStringLibs = listOf(
    "pftool-shared" to "pftoolSharedExtraLibPath",
    "shared" to "pftoolSharedBaseLibPath"
)
tasks.register("checkSharedStrings") {
    group = "verification"
    outputs.upToDateWhen { false }

    doLast {
        val stringsFile = project.projectDir.resolve("src/main/res/values/strings.xml")
        val stringsContent = stringsFile.readText()
        val missingKeys = mutableListOf<Pair<String, String>>()

        val aars = mutableListOf<Pair<String, File>>()

        configurations.all {
            if (!isCanBeResolved || aars.size >= sharedStringLibs.size) return@all
            try {
                sharedStringLibs.forEach { (libName, libPathLocalProp) ->
                    val localPath = localProperties.getProperty(libPathLocalProp)
                    val aar = if (localPath.isNullOrEmpty()) resolvedConfiguration.resolvedArtifacts.find { artifact ->
                        artifact.moduleVersion.id.let {
                            it.group == "com.github.aliernfrog.pf-tool" && it.name == libName
                        }
                    }?.file else File(localPath)
                    aar?.let { aars.add(libName to it) }
                }
            } catch (_: Exception) {}
        }

        if (aars.isEmpty()) throw GradleException("Failed to find shared libraries in the project")

        aars.forEach { (libName, aar) ->
            val zipFile = ZipFile(aar)
            val entry = "res/raw/shared_strings.txt".let {
                zipFile.getEntry(it)
                    ?: throw GradleException("Could not find '$it' inside '${aar.name}'")
            }
            val stringKeys = zipFile.getInputStream(entry).bufferedReader().readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
            zipFile.close()

            if (stringKeys.isEmpty()) {
                logger.warn("No string keys found in $libName:shared_strings.txt, skipping shared string check")
                return@forEach
            }

            val missingKeysForLib = stringKeys.filter { key ->
                !stringsContent.contains("<string name=\"$key\"")
            }.map { key -> libName to key }

            missingKeys.addAll(missingKeysForLib)

            if (missingKeysForLib.isNotEmpty()) project.logger.warn("Strings required by $libName are missing: ${missingKeysForLib.joinToString(", ")}")
            else project.logger.lifecycle("All required strings for $libName are present in ${stringsFile.path}")
        }

        if (missingKeys.isNotEmpty())
            throw GradleException("Strings required by shared libraries are missing: ${
                missingKeys.joinToString("\n") { (lib, key) -> "$lib -> $key" }
            }")

        project.logger.lifecycle("All required strings are present in ${stringsFile.path}")
    }
}

tasks.named("preBuild") {
    dependsOn(tasks.named("checkSharedStrings"))
}