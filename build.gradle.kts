buildscript {
    apply(from = "conf.gradle")

    rootProject.extra.apply {
        set("applicationId", "ai.datatower.sdk.android")
        set("compileSdkVersion", 33)
        set("buildToolsVersion", "33.0.2")
        set("minSdkVersion", 23)
        set("targetSdkVersion", 33)

        set("androidGradlePluginMvnTriple", "com.android.tools.build:gradle:8.0.1")
        set("javaVersion", JavaVersion.VERSION_17)
        set("kotlinVersion", "1.8.21")
        set("coroutinesVersion", "1.6.4")
        set("firebaseBomVersion", "31.5.0")
        set("roomDbVersion", "2.5.1")

        set("dtsdkCoreVersionName", "0.1.0")
    }

    repositories {
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
    }

    dependencies {
        val androidGradlePluginMvnTriple: String by rootProject.extra
        val kotlinVersion: String by rootProject.extra

        classpath(androidGradlePluginMvnTriple)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.20")
        // classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.5")
        classpath("com.google.gms:google-services:4.3.15")
        // The version of Dokka could be different from the version of Kotlin.
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.8.10")
        // maven publish
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.25.2")
        classpath("org.yaml:snakeyaml:2.0")
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:$kotlinVersion-1.0.11")
    }

    /* TODO: Unresolved token reference.
    ext{
        // 0 本地
        // 1 maven-snapshot
        // 2 maven-releases
        dependence_type = "0"
    }
     */
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://www.jitpack.io")

        maven("https://repo-public.en2joy.com/repository/maven-releases/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
