buildscript {
    rootProject.extra.apply {
        set("compileSdkVersion", 30)
        set("buildToolsVersion", "30.0.2")
        set("ndkVersion", "21.3.6528147")
        set("minSdkVersion", 21)
        set("targetSdkVersion", 30)

        set("javaVersion", JavaVersion.VERSION_1_8)
        set("androidPlugin", "com.android.tools.build:gradle:4.1.3")
        set("kotlinVersion", "1.4.21")
        set("coroutinesVersion", "1.3.9")
    }

    repositories {
        google()
        jcenter()
        maven("https://www.jitpack.io")
        maven("https://dl.bintray.com/infinum/android")
        maven("https://plugins.gradle.org/m2/")

    }

    dependencies {
        val androidPlugin: String by rootProject.extra
        val kotlinVersion: String by rootProject.extra

        classpath(androidPlugin)
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath("com.kezong:fat-aar:1.3.4")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://www.jitpack.io")
        maven("https://dl.bintray.com/infinum/android")

        maven(){
            url = java.net.URI( "http://localhost:8081/repository/roiquery-sdk-snapshot/")
            credentials {
                username = "shijunxing"
                password = "5201314sjx"
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

