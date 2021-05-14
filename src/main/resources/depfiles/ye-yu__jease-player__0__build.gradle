@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("org.jetbrains.dokka") version "1.4.0-rc"
    kotlin("jvm") version Jetbrains.Kotlin.version
    kotlin("plugin.serialization") version Jetbrains.Kotlin.version
    `maven-publish`
    id("maven")
    id("signing")
}

group = Info.group
version = Info.version

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    api(Jetbrains.Kotlin.stdLib)
    api(Jetbrains.Kotlin.annotations)
    api(Jetbrains.Kotlin.reflect)
    api(Jetbrains.Kotlinx.coroutinesCore)
    api(Jetbrains.Kotlinx.coroutines)
    api(Jetbrains.Kotlinx.serialization)
    testImplementation("junit:junit:4.13")
}

tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")

        from(sourceSets["main"].allSource)
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    }

    val javadocJar by creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from(project.tasks["dokkaJavadoc"])
    }

    val buildJava8 by creating(Jar::class) {
        archiveClassifier.set(Jetbrains.TargetContext.JAVA_1_8.classifier ?: "java8")

        from(compileKotlin.also {
            it.get().kotlinOptions {
                jvmTarget = Jetbrains.TargetContext.JAVA_1_8.target
                freeCompilerArgs = listOf(
                    "-Xopt-in=kotlin.RequiresOptIn",
                    "-Xopt-in=kotlin.ExperimentalStdlibApi"
                )
            }
        })

        from(compileJava.also {
            it.get().targetCompatibility = Jetbrains.TargetContext.JAVA_1_8.target
            it.get().sourceCompatibility = Jetbrains.TargetContext.JAVA_1_8.target
        })

        doLast {
            resetCompileJava(compileJava)
            resetCompileKotlin(compileKotlin)
        }
    }

    compileJava {
        targetCompatibility = Jetbrains.TargetContext.JAVA_11.target
        sourceCompatibility = Jetbrains.TargetContext.JAVA_11.target
    }

    compileKotlin {
        kotlinOptions {
            jvmTarget = Jetbrains.TargetContext.JAVA_11.target
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.ExperimentalStdlibApi"
            )
        }
    }
}

// for publishing to maven central
artifacts {
    add("archives", tasks["javadocJar"])
    add("archives", tasks["sourcesJar"])
    add("archives", tasks["buildJava8"])
}

signing {
    sign(configurations.archives.get())
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifacts {
                version += "-LOCAL"
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
                artifact(tasks["buildJava8"])
            }
        }

        repositories {
            mavenLocal()
        }
    }
}

tasks.named<Upload>("uploadArchives") {

    val sonatypeUsername: String = getOrDefault<String?>(project, "sonatypeUsername", null) ?: return@named
    val sonatypePassword: String = getOrDefault<String?>(project, "sonatypePassword", null) ?: return@named

    repositories {
        withConvention(MavenRepositoryHandlerConvention::class) {
            mavenDeployer {
                beforeDeployment {
                    this.addArtifact(signing.sign(this.pomArtifact).singleSignature.apply {
                        this.type = "pom." + this.signatureType.extension
                    })
                }

                withGroovyBuilder {
                    "repository"("url" to "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
                        "authentication"("userName" to sonatypeUsername, "password" to sonatypePassword)
                    }

                    "snapshotRepository"("url" to "https://oss.sonatype.org/content/repositories/snapshots/") {
                        "authentication"("userName" to sonatypeUsername, "password" to sonatypePassword)
                    }
                }
                pom.project {
                    withGroovyBuilder {
                        "name"("Jeasing")
                        "packaging"("jar")
                        // optionally artifactId can be defined here
                        "description"("Java Easing Player Library")
                        "url"("https://github.com/ye-yu/jease-player/tree/master")

                        "scm" {
                            "connection"("scm:git:ssh://git@github.com:ye-yu/jease-player.git")
                            "developerConnection"("scm:git:ssh://git@github.com:ye-yu/jease-player.git")
                            "url"("https://git@github.com:ye-yu/jease-player.git")
                        }

                        "licenses" {
                            "license" {
                                setProperty("name", "GPLv3")
                                setProperty("url", "https://github.com/ye-yu/jease-player/blob/master/LICENSE")
                            }
                        }

                        "developers" {
                            "developer" {
                                setProperty("id", "ye-yu")
                                setProperty("name", "Ye Yu")
                                setProperty("email", "rafolwen98@gmail.com")
                            }
                        }
                    }
                }
            }
        }
    }
}

inline fun <reified T> getOrDefault(of: Project, prop: String, def: T): T {
    if (project.hasProperty(prop)) return of.property(prop) as T
    return def
}

fun resetCompileJava(compileJava: TaskProvider<JavaCompile>) {
    compileJava {
        targetCompatibility = Jetbrains.TargetContext.JAVA_11.target
        sourceCompatibility = Jetbrains.TargetContext.JAVA_11.target
    }
}

fun resetCompileKotlin(compileKotlin: TaskProvider<KotlinCompile>) {
    compileKotlin {
        kotlinOptions {
            jvmTarget = Jetbrains.TargetContext.JAVA_11.target
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.ExperimentalStdlibApi"
            )
        }
    }
}
