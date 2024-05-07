import org.gradle.api.plugins.ExtraPropertiesExtension.UnknownPropertyException
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

        /*
         * config_dt.gradle.kts
         */
        try {
            apply(rootProject.file("config_dt.gradle.kts"))
        } catch (_: Throwable) {}
        fun buildStringConfigFunc(name: String, key: String = name, default: String = "") {
            try {
                buildConfigField("String", name, "\"${extra[key]}\"")
            } catch (_: UnknownPropertyException) {
                buildConfigField("String", name, "\"$default\"")
            }
        }
        buildStringConfigFunc("DEFAULT_SERVER_URL")
        buildStringConfigFunc("EVENT_REPORT_PATH")
        buildStringConfigFunc("ERROR_REPORTING_URL")
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
                url.set("https://github.com/datatower-ai/sdk-core-android")

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
                    connection.set("scm:git:github.com/datatower-ai/sdk-core-android.git")
                    developerConnection.set("scm:git:ssh://github.com/datatower-ai/sdk-core-android.git")
                    url.set("https://github.com/datatower-ai/sdk-core-android/tree/main")
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

        repositories {
            maven {
                name = "MavenCentral"
                url = if (dtsdkCoreVersionName.endsWith("-SNAPSHOT")) {
                    URI.create("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    URI.create("https://s01.oss.sonatype.org/content/repositories/releases/")
                }
                credentials.username = props["ossrhUsername"].toString()
                credentials.password = props["ossrhPassword"].toString()
            }
        }
    }
}

signing {
    // TODO: sign(publishing.publications)
}

tasks.withType(PublishToMavenRepository::class.java) {
    doLast {
        println("Published ${publication.groupId}:${publication.artifactId}:${publication.version} to ${repository.url}")
    }
}

tasks.withType(PublishToMavenLocal::class.java) {
    doLast {
        println("Published \u001B[1m${publication.groupId}:${publication.artifactId}:\u001B[1;38;2;79;175;83m${publication.version}\u001B[0m to MavenLocal")
    }
}

tasks.create("copyProguardMappingFiles", Copy::class) {
    from("$buildDir/outputs/mapping/publicRelease/")
    into(File(projectDir, "proguard-mapping"))
}
