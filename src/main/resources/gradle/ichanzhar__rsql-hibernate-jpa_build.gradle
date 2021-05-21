import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.0"
    `maven-publish`
    signing
}

group = "com.github.ichanzhar"
version = "0.11"

repositories {
    mavenCentral()
}

dependencies {
    api("cz.jirutka.rsql:rsql-parser:2.1.0")
    api("joda-time:joda-time:2.10.6")
    implementation("org.hibernate:hibernate-core:5.4.21.Final")
    api("org.apache.commons:commons-lang3:3.11")
    implementation("org.springframework.data:spring-data-jpa:2.2.9.RELEASE")
    implementation("org.slf4j:slf4j-ext:1.7.30")
}

java {
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>() {
    kotlinOptions{
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourcesJar.get())
            pom {
                name.set("RSQL Hibernate JPA")
                description.set("RSQL implementation for Hibernate/Spring data with join tables support")
                url.set("https://github.com/ichanzhar/rsql-hibernate-jpa")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                developers {
                    developer {
                        id.set("ichanzhar")
                        name.set("Ihor Chanzhar")
                        email.set("ihor.chanzhar@gmail.com")
                        organization.set("com.github.ichanzhar")
                        organizationUrl.set("https://github.com/ichanzhar")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ichanzhar/rsql-hibernate-jpa.git")
                    developerConnection.set("scm:git:git@github.com:ichanzhar/rsql-hibernate-jpa.git")
                    url.set("https://github.com/ichanzhar/rsql-hibernate-jpa")
                }
            }
            repositories {
                maven {
                    val repositoryUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshotRepositoryUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
                    url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotRepositoryUrl else repositoryUrl)
                    credentials {
                        val nexusUsername: String? by project
                        val nexusPassword: String? by project
                        username = nexusUsername
                        password = nexusPassword
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications.getByName("mavenJava"))
}