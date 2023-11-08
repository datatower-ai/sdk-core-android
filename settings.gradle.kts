rootProject.name = "DataTowerSDK-Core-Android"

include(":datatowerai-core")
include(":demo_analytics")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
    }
}
