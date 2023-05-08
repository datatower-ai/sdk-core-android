plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("maven-publish")
}

android {
    val compileSdkVersion: Int by rootProject.extra
    val minSdkVersion: Int by rootProject.extra
    val javaVersion: JavaVersion by rootProject.extra
    val buildToolsVersion: String by rootProject.extra
    val dtsdkCoreVersionName: String by rootProject.extra

    this.compileSdk = compileSdkVersion
    this.buildToolsVersion = buildToolsVersion
    this.namespace = "com.roiquery.analytics"

    defaultConfig {
        this.minSdk = minSdkVersion

        javaCompileOptions.annotationProcessorOptions.arguments.also {
            it["room.schemaLocation"] = "$projectDir/schemas"
        }
    }

    buildFeatures {
        this.buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile("${rootProject.path}/proguard-rules/common-rules.pro")
            proguardFile("${rootProject.path}/proguard-rules/core-proguard-rules.pro")
            buildConfigField("String", "VERSION_NAME", "\"$dtsdkCoreVersionName\"")
            /* 上报域名
             * 0 : 测试
             * 1 ：内部
             * 2 ：外部
             */
            // FIXME: Unused build config field.
            // buildConfigField("String", "LINK_SITE", "\"0\"")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            buildConfigField("String", "VERSION_NAME", "\"$dtsdkCoreVersionName\"")
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions.jvmTarget = javaVersion.majorVersion
}

dependencies {
    val kotlinVersion: String by rootProject.extra
    val coroutinesVersion: String by rootProject.extra
    val roomDbVersion: String by rootProject.extra

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.annotation:annotation:1.6.0")

    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")

    implementation("org.slf4j:slf4j-api:2.0.7")

    // Room DB
    implementation("androidx.room:room-ktx:$roomDbVersion")
    kapt("androidx.room:room-compiler:$roomDbVersion")
}
