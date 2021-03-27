
// pack-upload.gradle

// 指定编码
tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

// 打包源码
task sourcesJar(type: Jar) {
    from android.sourceSets.main.java.srcDirs
    archiveClassifier.set("sources")
}

tasks.register<Jar>("sourcesJar"){
    from(android.sourceSets.main.java.srcDirs)
    archiveClassifier.set("sources")
}


tasks.register<Javadoc>("javadoc"){
    from(android.sourceSets.main.java.srcDirs)
    archiveClassifier.set("sources")
}

task javadoc(type: Javadoc) {
    failOnError  false
    source = android.sourceSets.main.java.sourceFiles
    classpath(+= project.files(android.getBootClasspath().join(File.pathSeparator)))
    classpath(+= configurations.compile)
}

// 制作文档(Javadoc)
task javadocJar(type: Jar, dependsOn: javadoc) {
    archiveClassifier.set("javadoc")
    from javadoc.destinationDir
}

artifacts {
    archives sourcesJar
    archives javadocJar
}


apply(plugin = "maven")

// 对应的仓库地址
val URL_PUCBLIC = "http://lib.gcssloop.com:8081/repository/gcssloop-central/"
val URL_LOCAL = "http://localhost:8081/repository/roiquery-sdk-snapshot/"

// 上传到公共仓库
task uploadToPublic(type: Upload) {
    group = "upload"
    configuration = configurations.archives
    uploadDescriptor = true
    repositories{
        mavenDeployer {
            repository(url: URL_PUCBLIC) {
                authentication(userName = "shijunxing", password = "5201314sjx")
            }
            pom.version = "1.2.0"
            pom.artifactId = "com.roiquery.sdk"
            pom.groupId = "analytics"
        }
    }
}

// 上传到本机仓库
task uploadToLocal(type: Upload) {
    group = "upload"
    configuration = configurations.archives
    uploadDescriptor = true
    repositories{
        mavenDeployer {
            repository(url: URL_LOCAL) {
                authentication(userName = "shijunxing", password = "5201314sjx")
            }
            pom.version = "1.2.0-SNAPSHOT"
            pom.artifactId = "com.roiquery.sdk"
            pom.groupId = "analytics"
        }
    }
}

// 压缩生成归档文件
task pack(type: Zip) {
    group = "pack"
    archiveFileName = rootProject.getRootDir().getName() + "_v" + "1.2.0" + ".zip";
    destinationDirectory = rootProject.getRootDir().getParentFile();
    from rootProject.getRootDir().getAbsolutePath();
    exclude("**.zip")
    exclude("**.iml")
    exclude("**/**.iml")
    exclude("build/**")
    exclude(".idea/**")
    exclude(".gradle/**")
    exclude("gradle/**")
    exclude("**/build/**")
}