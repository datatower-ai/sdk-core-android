rootProject.name = "DataTowerSDK-Core"

include(":roiquery-core")
include(":demo_analytics")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
    }
}
