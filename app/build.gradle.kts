plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.miyabi_hiroshi.app.justarray"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.miyabi_hiroshi.app.justarray"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

base {
    archivesName = "JustArray-${android.defaultConfig.versionName}"
}

val downloadCinFiles by tasks.registering {
    description = "Downloads Array30 .cin dictionary files from gontera/array30 into assets."
    val assetsDir = layout.projectDirectory.dir("src/main/assets")
    val baseUrl = "https://raw.githubusercontent.com/gontera/array30/v2023-1.0-20230211/OpenVanilla"
    val cinFiles = listOf(
        "array30-OpenVanilla-big-v2023-1.0-20230211.cin",
        "array-shortcode-20210725.cin",
        "array-special-201509.cin",
    )

    outputs.files(cinFiles.map { assetsDir.file(it) })

    doLast {
        val dir = assetsDir.asFile
        dir.mkdirs()
        cinFiles.forEach { fileName ->
            val target = File(dir, fileName)
            if (!target.exists()) {
                logger.lifecycle("Downloading $fileName ...")
                uri("$baseUrl/$fileName").toURL().openStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                logger.lifecycle("  -> ${target.absolutePath} (${target.length()} bytes)")
            } else {
                logger.lifecycle("$fileName already exists, skipping.")
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn(downloadCinFiles)
}

afterEvaluate {
    tasks.named("assembleDebug") {
        dependsOn("testDebugUnitTest")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    val kotlinClasses = fileTree("build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/*_Impl*",          // Room generated
            "**/*_Factory*",       // Room generated
        )
    }
    classDirectories.setFrom(kotlinClasses)
    sourceDirectories.setFrom("src/main/java")
    executionData.setFrom("build/jacoco/testDebugUnitTest.exec")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    // Logging
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
