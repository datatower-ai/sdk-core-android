import java.util.Properties
import java.net.URI
import org.gradle.jvm.tasks.Jar

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
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
    this.namespace = "com.roiquery.analytics"

    defaultConfig {
        this.minSdk = minSdkVersion

        javaCompileOptions.annotationProcessorOptions.arguments.also {
            it["room.schemaLocation"] = "$projectDir/schemas"
        }
        buildConfigField("Boolean", "IS_INTERNAL_BUILD", "false")
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

    flavorDimensions += "logging"
    productFlavors {
        create("public") {
            dimension = "logging"
            buildConfigField("Boolean", "IS_INTERNAL_BUILD", "false")
        }
        create("internal") {
            dimension = "logging"
            buildConfigField("Boolean", "IS_INTERNAL_BUILD", "true")
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

tasks.create("sourcesJarToPublish", Jar::class) {
    from(android.sourceSets.getByName("main").java.getSourceFiles())
    archiveClassifier.set("sources")
}

publishing {
    val groupId = "com.lovinjoy"
    val artifactId = "datatowerai-core"
    val dtsdkCoreVersionName: String by rootProject.extra

    val props = rootProject.file("local.properties").inputStream().use { inStream ->
        Properties().also { it.load(inStream) }
    }

    publications {
        create<MavenPublication>("Release") {
            this.groupId = groupId
            this.artifactId = artifactId
            version = dtsdkCoreVersionName

            artifact("$buildDir/outputs/aar/${project.name}-public-release.aar")
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
                        id.set("lovinjoy")
                        name.set("lovinjoy")
                        email.set("develop@nodetower.com")
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

    repositories {
        maven {
            name = "LocalReposilite"
            url = URI.create("http://localhost:8080/releases/")
            isAllowInsecureProtocol = true
            credentials.username = props["maven.repo.local.username"].toString()
            credentials.password = props["maven.repo.local.password"].toString()
        }
        maven {
            name = "En2joyNexus3"
            url = URI.create("https://repo-public.en2joy.com/repository/maven-releases/")
            credentials.username = props["maven.repo.en2joy-nexus3.username"].toString()
            credentials.password = props["maven.repo.en2joy-nexus3.password"].toString()
        }
        maven {
            name = "MavenCentral"
            url = URI.create("https://s01.oss.sonatype.org/content/repositories/releases/")
            credentials.username = props["maven.repo.central.username"].toString()
            credentials.password = props["maven.repo.central.password"].toString()
        }
    }
}

signing {
    // TODO: sign(publishing.publications)
}
