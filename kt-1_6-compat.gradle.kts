buildscript {
    rootProject.extra.apply {
        set("javaVersion", JavaVersion.VERSION_1_8)
        set("kotlinVersion", "1.6.21")
        set("roomDbVersion", "2.4.7")
        set("gradleKspVersion", "${properties["kotlinVersion"]}-1.0.6")
    }
}
