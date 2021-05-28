/*
 * Copyright 2021 陈圳佳
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    base
    java
    id("org.jetbrains.kotlin.jvm") version "1.3.70" apply false
    id("org.springframework.boot") version "2.3.0.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
}
allprojects {
    repositories {
        jcenter()
        mavenCentral()
        mavenLocal()
    }
}
dependencies {
    testImplementation("junit:junit:4.12")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(project(":web"))
    implementation(project(":core"))
    implementation(project(":security"))
}
subprojects {

    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    if (project.hasProperty("isCI")) {
        val releaseVersion: String? by project
        version = releaseVersion ?: properties["version"]!!
    }
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }
    configure<JavaPluginExtension> {
        registerFeature("optional") {
            val sourceSets: SourceSetContainer by project
            usingSourceSet(sourceSets["main"])
        }
    }
    dependencies {
        val annotationProcessor by configurations
        val compileOnly by configurations
        val testImplementation by configurations

        "optionalImplementation"("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        "optionalImplementation"("org.jetbrains.kotlin:kotlin-stdlib")
        "optionalImplementation"("org.jetbrains.kotlin:kotlin-reflect")
        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        compileOnly("org.springframework.boot:spring-boot-configuration-processor")

        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        testImplementation("junit:junit:4.12")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
    tasks {
        withType(Javadoc::class).configureEach {
            (options as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
        }
        withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=enable")
            }
        }
        withType(Javadoc::class) {
            isFailOnError = false
            options.encoding = "UTF-8"
        }
    }
    tasks.withType(JavaCompile::class) {
        val processResources by tasks
        dependsOn(processResources)
        options.isIncremental = true
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
    val jar: Jar by tasks
    val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks

    bootJar.enabled = false
    jar.enabled = true
//    afterEvaluate {
//        tasks.withType(PublishToMavenRepository) { task ->
//            if (task.publication.hasProperty('repo') && task.publication.repo != task.repository.name) {
//                task.enabled = false
//                task.group = null
//            }
//        }
//    }

    tasks.register<Jar>("sourcesJar") {
        val sourceSets: SourceSetContainer by project
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }
    tasks.register<Jar>("javadocJar") {
        val javadoc: Javadoc by tasks
        from(javadoc)
        archiveClassifier.set("javadoc")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                pom {
                    name.set("niubi-commons")
                    description.set("基于SpringMvc和Security权限管理")
                    url.set("https://github.com/chenzhenjia/niubi-commons")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("chzhenjia")
                            name.set("陈圳佳")
                            email.set("chzhenjia@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:https://github.com/chenzhenjia/niubi-commons.git")
                        developerConnection.set("scm:https://github.com/chenzhenjia/niubi-commons.git")
                        url.set("https://github.com/chenzhenjia/niubi-commons")
                    }
                }
                from(components["java"])
                val sourcesJar by tasks
                val javadocJar by tasks
                artifact(sourcesJar)
                artifact(javadocJar)
            }
        }
        repositories {
            maven {
                credentials {
                    val mavenCentralUsername: String? by project
                    val mavenCentralPassword: String? by project
                    username = mavenCentralUsername
                    password = mavenCentralPassword
                }
                val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
                url = if ("$version".endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            }
        }
    }
    configure<SigningExtension> {
        if (project.hasProperty("isCI")) {
            val signingKeyId: String? by project
            val signingPassword: String? by project
            val signingKey: String? by project
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        }
        val publishing: PublishingExtension by project
        sign(publishing.publications["mavenJava"])
    }
}
