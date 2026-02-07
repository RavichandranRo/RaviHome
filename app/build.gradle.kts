
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.ravihome"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ravihome"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }
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
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.recyclerview)
    ksp(libs.androidx.room.compiler)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.mpandroidchart)
    // Apache POI for Excel export
    implementation("org.apache.poi:poi-ooxml:5.2.5") {
        exclude(group = "org.apache.xmlgraphics", module = "batik-anim")
        exclude(group = "org.apache.xmlgraphics", module = "batik-awt-util")
        exclude(group = "org.apache.xmlgraphics", module = "batik-bridge")
        exclude(group = "org.apache.xmlgraphics", module = "batik-css")
        exclude(group = "org.apache.xmlgraphics", module = "batik-dom")
        exclude(group = "org.apache.xmlgraphics", module = "batik-ext")
        exclude(group = "org.apache.xmlgraphics", module = "batik-gvt")
        exclude(group = "org.apache.xmlgraphics", module = "batik-parser")
        exclude(group = "org.apache.xmlgraphics", module = "batik-script")
        exclude(group = "org.apache.xmlgraphics", module = "batik-svg-dom")
        exclude(group = "org.apache.xmlgraphics", module = "batik-svggen")
        exclude(group = "org.apache.xmlgraphics", module = "batik-transcoder")
        exclude(group = "org.apache.xmlgraphics", module = "batik-util")
        exclude(group = "org.apache.xmlgraphics", module = "batik-xml")
        exclude(group = "commons-codec", module = "commons-codec")
        exclude(group = "com.intellij", module = "annotations")
    }

    // Required for POI on Android
    implementation("javax.xml.stream:stax-api:1.0-2")
    implementation("org.apache.xmlbeans:xmlbeans:5.3.0")
    implementation("org.apache.commons:commons-collections4:4.5.0")
    implementation("commons-io:commons-io:2.21.0")
    implementation("xerces:xercesImpl:2.12.2") {
        isTransitive = false
    }
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
    implementation(libs.google.mlkit.text.recognition)
    implementation(libs.google.mlkit.text.recognition.devanagari)
    implementation(libs.google.mlkit.barcode)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
    }
}