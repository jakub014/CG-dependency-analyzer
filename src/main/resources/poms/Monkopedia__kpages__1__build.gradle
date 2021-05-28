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
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target.COMMONJS

buildscript {
    repositories {
        maven(url = "https://dl.bintray.com/kotlin/dokka")
    }
}
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka") version "1.4.10.2"

    java
    `maven-publish`
    `signing`
}

group = "com.monkopedia"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlin-js-wrappers")
}

dependencies {
}

kotlin {
    js {
        useCommonJs()
        nodejs()
        browser {
            webpackTask {
                // cssSupport.enabled = true
                output.libraryTarget = COMMONJS
            }
        }
    }
    jvm().withJava()
    sourceSets["commonMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
    }
    sourceSets["jsMain"].dependencies {
        implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-css:1.0.0-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-styled:5.2.0-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react:16.13.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
        implementation("com.ccfraser.muirwik:muirwik-components:0.6.2")
        implementation(npm("codemirror", "5.58.3"))
        implementation(npm("showdown", "1.9.1"))
        implementation(npm("css-loader", "3.5.2"))
        implementation(npm("style-loader", "1.1.3"))
        implementation(npm("bootstrap", "^4.4.1"))
        implementation(npm("react", "~16.13.1"))
        implementation(npm("react-dom", "~16.13.1"))
    }
    sourceSets["jvmMain"].dependencies {
        api(project(":lanterna-ext"))
    }
}
val dokkaJavadoc = tasks.create("dokkaJavadocCustom", org.jetbrains.dokka.gradle.DokkaTask::class) {
    dependencies {
        plugins("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.10.2")
    }
    // outputFormat = "javadoc"
    outputDirectory.set(File(project.buildDir, "javadoc"))
    inputs.dir("src/commonMain/kotlin")
}

val javadocJar = tasks.create("javadocJar", Jar::class) {
    dependsOn(dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(File(project.buildDir, "javadoc"))
}

publishing {
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
