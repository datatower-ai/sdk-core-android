import java.util.Properties
import java.io.FileInputStream


val buildProps = Properties()
    .apply { load(FileInputStream(rootProject.file("build.properties"))) }

plugins {
    id("com.android.library")
    id("com.kezong.fat-aar")
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

        minSdkVersion(minSdkVersion)
        targetSdkVersion(targetSdkVersion)

        versionName = buildProps["analyticsVersionName"]!!.toString()
        versionCode = buildProps["analyticsVersionCode"]!!.toString().toInt()

        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")

        buildConfigField("String", "VERSION_NAME", "\"${buildProps["analyticsVersionName"]!!}\"")

        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
                proguardFiles(file("proguard-rules.pro"))
            }
            getByName("debug") {
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
            //stander
            implementation(kotlin("stdlib"))
            implementation("androidx.annotation:annotation:1.1.0")
            embed("com.android.installreferrer:installreferrer:2.2")

            //oaid
            embed("com.github.gzu-liyujiang.Android_CN_OAID:OAID_ASUS:2.1.0") //华硕
            embed("com.github.gzu-liyujiang.Android_CN_OAID:OAID_BUN:2.1.0") //中兴、卓易
            embed("com.github.gzu-liyujiang.Android_CN_OAID:OAID_HEYTAP:2.1.0") //欧珀、一加
            embed("com.github.gzu-liyujiang.Android_CN_OAID:OAID_SAMSUNG:2.1.0") //三星
            embed("com.github.gzu-liyujiang.Android_CN_OAID:OAID_UODIS:2.1.0") //华为
            embed("com.github.gzu-liyujiang.Android_CN_OAID:OAID_ZUI:2.1.0") //联想、摩托罗拉
            embed("com.github.gzu-liyujiang.Android_CN_OAID:OAID_IMPL:2.1.0") //具体实现
            embed("com.github.gzu-liyujiang:Logger:1.2.2") //具体实现

            //cloudConfig
            implementation(project(":lib_cloudconfig"))

            //test
            testImplementation("junit:junit:4.13.2")
            androidTestImplementation("androidx.test.ext:junit:1.1.2")
            androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

        }
    }

}
val GROUP_ID = "com.roiquery.sdk"
val ARTIFACT_ID = "analytics"
val VERSION = buildProps["analyticsVersionName"]!!.toString()

apply("../pack-upload.gradle")
