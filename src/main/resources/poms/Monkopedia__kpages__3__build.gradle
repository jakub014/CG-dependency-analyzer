/*
 * Copyright 2020 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
buildscript {
    repositories {
        maven(url = "https://dl.bintray.com/kotlin/dokka")
    }
}
plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm")
    kotlin("plugin.serialization") version "1.4.10"
    kotlin("kapt")
    id("org.jetbrains.dokka") version "1.4.10.2"

    `maven-publish`
    `signing`
}

group = "com.monkopedia"

java {
    withSourcesJar()
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    mavenLocal()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev/")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap/")
    maven(url = "https://kotlinx.bintray.com/kotlinx/")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("com.monkopedia:ksrpc:0.1.1")

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("io.ktor:ktor-client-core:1.3.2")
    implementation("io.ktor:ktor-client-core-jvm:1.3.2")
    implementation("io.ktor:ktor-client-apache:1.3.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.10.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.10.0")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.0")

    implementation("org.jetbrains.exposed:exposed-core:0.26.2")
    implementation("org.jetbrains.exposed:exposed-dao:0.26.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.26.2")
    implementation("org.xerial:sqlite-jdbc:3.32.3.2")

    implementation("org.apache.lucene:lucene-core:6.4.1")

    implementation("com.vladsch.flexmark:flexmark-all:0.62.2")
    implementation("com.github.ajalt:clikt:2.8.0")
    api("com.googlecode.lanterna:lanterna:3.0.3")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation("com.google.auto.service:auto-service-annotations:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
    dokkaJavadocPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.10.2")
}

val javadocJar = tasks.create("javadocJar", Jar::class) {
    dependsOn(tasks["dokkaJavadoc"])
    archiveClassifier.set("javadoc")
    from(File(project.buildDir, "dokka/javadoc"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
    publications.all {
        if (this !is MavenPublication) return@all
        artifact(javadocJar)
        pom {
            name.set("KPages")
            description.set("A multi-platform library for managing navigation/routing")
            url.set("http://www.github.com/Monkopedia/kpages")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("monkopedia")
                    name.set("Jason Monk")
                    email.set("monkopedia@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/Monkopedia/kpages.git")
                developerConnection.set("scm:git:ssh://github.com/Monkopedia/kpages.git")
                url.set("http://github.com/Monkopedia/kpages/")
            }
        }
    }
    repositories {
        maven(url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
            name = "OSSRH"
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
