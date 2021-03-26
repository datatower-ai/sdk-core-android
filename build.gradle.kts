buildscript {
    rootProject.extra.apply {
        set("applicationId", "com.ironmeta.nocardvpn")
        set("compileSdkVersion", 30)
        set("buildToolsVersion", "30.0.2")
        set("ndkVersion", "21.3.6528147")
        set("minSdkVersion", 21)
        set("targetSdkVersion", 30)

        set("javaVersion", JavaVersion.VERSION_1_8)
        set("androidPlugin", "com.android.tools.build:gradle:4.1.2")
        set("kotlinVersion", "1.4.21")
        set("coroutinesVersion", "1.3.9")
    }

    repositories {
        google()
        jcenter()
        maven("https://www.jitpack.io")
        maven("https://dl.bintray.com/infinum/android")
    }

    dependencies {
        val androidPlugin: String by rootProject.extra
        val kotlinVersion: String by rootProject.extra

        classpath(androidPlugin)
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath("com.kezong:fat-aar:1.3.4")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://www.jitpack.io")
        maven("https://dl.bintray.com/infinum/android")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
