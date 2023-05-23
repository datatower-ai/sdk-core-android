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
    this.namespace = "com.roiquery.analytics_demo"

    defaultConfig {
        this.minSdk = minSdkVersion
        this.targetSdk = targetSdkVersion
        this.versionCode = 10
        this.versionName = "1.1.10"

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

    kotlinOptions.jvmTarget = javaVersion.majorVersion

    buildFeatures {
        this.viewBinding = true
        this.buildConfig = true
    }
}

dependencies {
    val kotlinVersion: String by rootProject.extra
    val coroutinesVersion: String by rootProject.extra

    implementation("org.jetbrains.kotlin:kotlin-stdlib") {
        version { strictly(kotlinVersion) }
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    /* NOTE: Do NOT update dependencies as it works with Kotlin 1.6, for more information see file
     * 'kt-1_6-compat.gradle.kts'. */
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.annotation:annotation:1.4.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.fragment:fragment-ktx:1.5.7")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("com.google.android.material:material:1.9.0-rc01")

    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")

    implementation("org.greenrobot:eventbus:3.3.1")

    implementation(project(":roiquery-core"))

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
}
