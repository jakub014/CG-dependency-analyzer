plugins {
    groovy
    id("java-library")
    id("maven-publish")
    id("signing")
    id("com.github.ben-manes.versions") version ("0.36.0")
}

val projectGroupId: String by project
val projectVersion: String by project

group = projectGroupId
version = projectVersion

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

fun getBuildProperty(property: String): String {
    val prop: String? = project.findProperty(property) as String?
    if(prop != null) {
        return prop
    }

    val env: String? = System.getenv(property)
    if (env != null) {
        return env
    }

    return "n/a"
}

fun getBuildSignKey(property: String): String {
    val prop: String? = project.findProperty(property) as String?
    if(prop != null) {
        return prop
    }

    val env: String? = System.getenv(property)
    if (env != null) {
        return env.replace("\\n", "\n")
    }

    return "n/a"
}

fun isReleaseVersion(): Boolean {
    return !(project.version.toString().endsWith("SNAPSHOT"))
}

ext {
    set("publishUser", getBuildProperty("PUBLISH_USER"))
    set("publishKey", getBuildProperty("PUBLISH_KEY"))
    set("signKey", getBuildSignKey("SIGN_KEY"))
    set("signPwd", getBuildProperty("SIGN_PWD"))
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://oss.sonatype.org/content/repositories/snapshots")
        mavenContent {
            snapshotsOnly()
        }
    }
}

dependencies {
    compileOnly("io.openapiprocessor:openapi-processor-api:2021.1")

    implementation("org.codehaus.groovy:groovy:3.0.7")
    implementation("org.codehaus.groovy:groovy-nio:3.0.7")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.0")
    implementation("io.github.java-diff-utils:java-diff-utils:4.9")
    implementation("com.google.jimfs:jimfs:1.1") {
        exclude("com.google.guava")
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("javadoc")
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

artifacts {
    archives(sourcesJar)
    archives(javadocJar)
}

//apply(from = "${rootProject.rootDir}/gradle/publishing.gradle.kts")

val projectTitle: String by project
val projectDesc: String by project
val projectUrl: String by project
val projectGithubRepo: String by project

// does not work on oss.sonatype.org
tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("OpenApiProcessor") {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set(projectTitle)
                description.set(projectDesc)
                url.set(projectUrl)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("hauner")
                        name.set("Martin Hauner")
                    }
                }

                scm {
                   url.set("https://github.com/${projectGithubRepo}")
                }
            }
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
            url = uri(if (isReleaseVersion()) releasesRepoUrl else snapshotsRepoUrl)

            credentials {
                username = project.extra["publishUser"].toString()
                password = project.extra["publishKey"].toString()
            }
        }
    }
}

//tasks.withType<Sign>().configureEach {
//    onlyIf { isReleaseVersion() }
//}

signing {
    useInMemoryPgpKeys(
        project.extra["signKey"].toString(),
        project.extra["signPwd"].toString())

    sign(publishing.publications["OpenApiProcessor"])
}
