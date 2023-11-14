rootProject.name = "datatower_ai-core-android"

include(":datatowerai-core")
include(":demo_analytics")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
    }
}
