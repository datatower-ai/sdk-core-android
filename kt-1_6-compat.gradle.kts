/**
 * NOTE: Compatibility with Kotlin 1.6 will be dropped in the future, we're stuck at Kotlin 1.6
 * until all our users are upgraded their Kotlin dependencies.
 */
buildscript {
    rootProject.extra.apply {
        set("javaVersion", JavaVersion.VERSION_1_9)
        set("kotlinVersion", "1.6.21")
        set("coroutinesVersion", "1.6.4")
        set("roomDbVersion", "2.4.3")
        set("gradleKspVersion", "${properties["kotlinVersion"]}-1.0.6")
        set("androidxAnnotationVersion", "1.4.0")
    }
}
