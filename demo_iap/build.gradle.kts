import java.util.Properties
import java.io.FileInputStream


val buildProps = Properties()
    .apply { load(FileInputStream(rootProject.file("build.properties"))) }

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    val compileSdkVersion: Int by rootProject.extra
    val minSdkVersion: Int by rootProject.extra
    val targetSdkVersion: Int by rootProject.extra
    val javaVersion: JavaVersion by rootProject.extra
    val buildToolsVersion: String by rootProject.extra

    buildToolsVersion(buildToolsVersion)
    compileSdkVersion(compileSdkVersion)


    defaultConfig {

        applicationId("com.nodetower.demo_iap")
        minSdkVersion(minSdkVersion)
        targetSdkVersion(targetSdkVersion)

        versionName = buildProps["iAPVersionName"]!!.toString()
        versionCode = buildProps["iAPVersionCode"]!!.toString().toInt()

        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")

        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
                proguardFiles(file("proguard-rules.pro"))
            }
        }

        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }

        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }

        kotlinOptions {
            useIR = true
        }

        dependencies {
            implementation(kotlin("stdlib"))

            implementation("androidx.core:core-ktx:1.3.2")
            implementation("androidx.appcompat:appcompat:1.2.0")
            implementation("com.google.android.material:material:1.3.0")
            implementation("androidx.constraintlayout:constraintlayout:2.0.4")

            implementation(project(":lib_iap"))
            implementation(project(":lib_analytics"))
            implementation(project(":lib_cloudconfig"))


            testImplementation("junit:junit:4.13.2")
            androidTestImplementation("androidx.test.ext:junit:1.1.2")
            androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

        }
    }
}

