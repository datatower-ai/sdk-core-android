buildscript {
    rootProject.extra.apply {
        set("applicationId", "ai.datatower.sdk.android")
        set("buildToolsVersion", "33.0.2")
        set("compileSdkVersion", 34)
        set("minSdkVersion", 19)
        set("targetSdkVersion", 33)

        set("dtsdkCoreVersionName", "3.0.7")

        set("javaVersion", JavaVersion.VERSION_1_8)
        set("kotlinVersion", "1.8.21")
        set("androidGradlePluginMvnTriple", "com.android.tools.build:gradle:8.0.1")
        set("coroutinesVersion", "1.6.4")
        set("firebaseBomVersion", "31.5.0")
        set("roomDbVersion", "2.5.1")
        set("gradleKspVersion", "${properties["kotlinVersion"]}-1.0.11")
        set("androidxAnnotationVersion", "1.6.0")
    }
//    apply("kt-1_6-compat.gradle.kts")

    repositories {
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        val androidGradlePluginMvnTriple: String by rootProject.extra
        val kotlinVersion: String by rootProject.extra
        val gradleKspVersion: String by rootProject.extra

        classpath(androidGradlePluginMvnTriple)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        // classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.5")
        classpath("com.google.gms:google-services:4.3.15")
        // The version of Dokka could be different from the version of Kotlin.
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.8.10")
        // maven publish
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.28.0")
        classpath("org.yaml:snakeyaml:2.0")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:$gradleKspVersion")
    }
}

allprojects {
    repositories {
//        mavenLocal()
        mavenCentral()
        google()
        maven("https://www.jitpack.io")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
