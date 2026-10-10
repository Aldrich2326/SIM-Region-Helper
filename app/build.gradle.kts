// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.github.nrfr"
    compileSdk = 35

    defaultConfig {
        // Keep the public derivative separate from upstream and the earlier K90 test package.
        applicationId = "io.github.nrfr.iqoo15"
        minSdk = 29
        targetSdk = 34
        // Keep the initial package ID so existing users retain their restore snapshots.
        versionCode = 3
        versionName = "1.1.0-preview2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // The instrumentation bridge is invoked by the platform by class name. Ship the
            // audited classes intact until a minified release has its own device coverage.
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        aidl = true
        buildConfig = true
        compose = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED")
            // Forward an explicitly configured build proxy to Robolectric's SDK resolver.
            listOf("https.proxyHost", "https.proxyPort", "http.proxyHost", "http.proxyPort")
                .forEach { key -> System.getProperty(key)?.let { value -> it.systemProperty(key, value) } }
        }
    }
    packaging {
        // Keep the transferable APK compact on API 29+; default uncompressed DEX was 18 MB.
        // Android's installer/runtime supports compressed DEX on the target device.
        dex.useLegacyPackaging = true
    }
    sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/licenseAssets"))
}

val copyLicenseAssets by tasks.registering(Copy::class) {
    from(rootProject.file("LICENSE")) { rename { "LICENSE-Apache-2.0.txt" } }
    from(rootProject.file("licenses/SamsungRegionOverride-MIT.txt")) {
        rename { "LICENSE-SamsungRegionOverride-MIT.txt" }
    }
    into(layout.buildDirectory.dir("generated/licenseAssets"))
    from(rootProject.file("NOTICE-ANDROID17.md"))
    from(rootProject.file("NOTICE"))
    from(rootProject.file("CREDITS.md"))
}
tasks.named("preBuild").configure { dependsOn(copyLicenseAssets) }

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Shizuku
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    testImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
