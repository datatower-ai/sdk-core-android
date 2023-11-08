import java.util.Properties
import java.net.URI
import org.gradle.jvm.tasks.Jar

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("maven-publish")
    id("signing")
}

android {
    val compileSdkVersion: Int by rootProject.extra
    val minSdkVersion: Int by rootProject.extra
    val javaVersion: JavaVersion by rootProject.extra
    val buildToolsVersion: String by rootProject.extra
    val dtsdkCoreVersionName: String by rootProject.extra

    this.compileSdk = compileSdkVersion
    this.buildToolsVersion = buildToolsVersion
    this.namespace = "ai.datatower.analytics"

    defaultConfig {
        this.minSdk = minSdkVersion

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        buildConfigField("Boolean", "IS_LOGGING_ENABLED", "false")
    }

    buildFeatures {
        this.buildConfig = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            proguardFile("${rootProject.rootDir}/proguard-rules/common-rules.pro")
            proguardFile("${rootProject.rootDir}/proguard-rules/core-proguard-rules.pro")
            buildConfigField("String", "VERSION_NAME", "\"$dtsdkCoreVersionName\"")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFile(getDefaultProguardFile("proguard-android-optimize.txt"))
            buildConfigField("String", "VERSION_NAME", "\"$dtsdkCoreVersionName\"")
        }
    }

    flavorDimensions += "slf4jLogging"
    productFlavors {
        create("public") {
            dimension = "slf4jLogging"
            buildConfigField("Boolean", "IS_LOGGING_ENABLED", "false")
            missingDimensionStrategy("slf4jLogging", "public")
        }
        create("internal") {
            dimension = "slf4jLogging"
            buildConfigField("Boolean", "IS_LOGGING_ENABLED", "true")
            missingDimensionStrategy("slf4jLogging", "internal")
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions.jvmTarget = javaVersion.toString()
}

dependencies {
    val kotlinVersion: String by rootProject.extra
    val coroutinesVersion: String by rootProject.extra
    val roomDbVersion: String by rootProject.extra
    val androidxAnnotationVersion: String by rootProject.extra

    implementation("org.jetbrains.kotlin:kotlin-stdlib") {
        version { strictly(kotlinVersion) }
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.annotation:annotation:$androidxAnnotationVersion")

    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")

    // Room DB
    implementation("androidx.room:room-ktx:$roomDbVersion")
    ksp("androidx.room:room-compiler:$roomDbVersion")
}

tasks.create("sourcesJarToPublish", Jar::class) {
    from(android.sourceSets.getByName("main").java.getSourceFiles())
    archiveClassifier.set("sources")
}

publishing {
    val groupId = "ai.datatower"
    val artifactId = "datatowerai-core"
    val dtsdkCoreVersionName: String by rootProject.extra

    publications {
        create<MavenPublication>("Release") {
            this.groupId = groupId
            this.artifactId = artifactId
            version = dtsdkCoreVersionName

            from(components["release"])
            artifact(tasks.getByName("sourcesJarToPublish"))

            pom {
                name.set(artifactId)
                description.set("DataTower.ai Android SDK")
                url.set("https://github.com/lovinjoy/datatower.ai-core-android")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("datatower")
                        name.set("datatower.ai")
                        email.set("develop@datatower.ai")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/lovinjoy/datatower.ai-core-android.git")
                    developerConnection.set("scm:git:ssh://github.com/lovinjoy/datatower.ai-core-android.git")
                    url.set("https://github.com/lovinjoy/datatower.ai-core-android/tree/main")
                }
            }

            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")

                configurations.implementation.get().dependencies.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.group)
                    dependencyNode.appendNode("artifactId", it.name)
                    dependencyNode.appendNode("version", it.version)
                }
            }
        }
    }
}

signing {
    // TODO: sign(publishing.publications)
}

tasks.create("copyProguardMappingFiles", Copy::class) {
    from("$buildDir/outputs/mapping/publicRelease/")
    into(File(projectDir, "proguard-mapping"))
}
