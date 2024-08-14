plugins {
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.android")
}

android {
    val compileSdkVersion: Int by rootProject.extra
    val minSdkVersion: Int by rootProject.extra
    val targetSdkVersion: Int by rootProject.extra
    val javaVersion: JavaVersion by rootProject.extra
    val buildToolsVersion: String by rootProject.extra
    val dtsdkCoreVersionName: String by rootProject.extra


    this.compileSdk = compileSdkVersion
    this.buildToolsVersion = buildToolsVersion
    this.namespace = "ai.datatower.analytics_demo"

    defaultConfig {
        this.minSdk = minSdkVersion
        this.targetSdk = targetSdkVersion
        this.versionCode = 10
        this.versionName = "1.1.10"

        multiDexEnabled = true

        javaCompileOptions.annotationProcessorOptions.arguments.also {
            it["room.schemaLocation"] = "$projectDir/schemas"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile("${rootProject.rootDir}/proguard-rules/common-rules.pro")
            proguardFile("${rootProject.rootDir}/proguard-rules/core-proguard-rules.pro")
            proguardFile("$projectDir/r8-config/eventbus.pro")
            buildConfigField("String", "VERSION_NAME", "\"$dtsdkCoreVersionName\"")
        }
        getByName("debug") {
            isMinifyEnabled = false
            buildConfigField("String", "VERSION_NAME", "\"$dtsdkCoreVersionName\"")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = "111111"
            keyPassword = "111111"
            keyAlias = "key0"
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("public") {
            dimension = "distribution"
            signingConfig = signingConfigs.getByName("release")
            missingDimensionStrategy("slf4jLogging", "public")
        }
        create("internal") {
            dimension = "distribution"
            signingConfig = signingConfigs.getByName("debug")
            missingDimensionStrategy("slf4jLogging", "internal")
        }
    }

    compileOptions {
        this.sourceCompatibility = javaVersion
        this.targetCompatibility = javaVersion
    }

    buildFeatures {
        this.compose = true
        this.viewBinding = true
        this.buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0-rc02"
    }
}

dependencies {
    val kotlinVersion: String by rootProject.extra
    val coroutinesVersion: String by rootProject.extra

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    /* NOTE: Do NOT update dependencies as it works with Kotlin 1.6, for more information see file
     * 'kt-1_6-compat.gradle.kts'. */
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.annotation:annotation:1.4.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.5.7")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.material:material:1.9.0-rc01")

    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")

    implementation("org.greenrobot:eventbus:3.3.1")
    implementation(project(":datatowerai-core"))
//    implementation("ai.datatower:core:3.0.+")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.6.0")
    implementation(platform("androidx.compose:compose-bom:2022.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    androidTestImplementation(platform("androidx.compose:compose-bom:2022.10.00"))

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")

    implementation("androidx.multidex:multidex:2.0.1")
}
