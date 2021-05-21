plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"

    id("com.github.johnrengelman.shadow") version "6.1.0"

    signing
    maven

    id("io.codearte.nexus-staging") version "0.22.0"
}

group = "com.zp4rker"
version = "1.1.8"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.fusesource.jansi:jansi:2.1.1")

    testImplementation("junit:junit:4.11")
}

tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.getByName("javadoc"))
}

tasks.create<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

artifacts {
    add("archives", tasks.getByName("javadocJar"))
    add("archives", tasks.getByName("sourcesJar"))
}

signing {
    sign(configurations["archives"])
}

tasks.named<Upload>("uploadArchives") {
    repositories {
        withConvention(MavenRepositoryHandlerConvention::class) {
            mavenDeployer {
                beforeDeployment {
                    signing.signPom(this)
                }

                withGroovyBuilder {
                    "repository"("url" to "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
                        "authentication"("userName" to properties["ossrhUsername"], "password" to properties["ossrhPassword"])
                    }
                    "snapshotRepository"("url" to "https://oss.sonatype.org/content/repositories/snapshots/") {
                        "authentication"("userName" to properties["ossrhUsername"], "password" to properties["ossrhPassword"])
                    }
                }

                pom.project {
                    setProperty("name", "Log4Kt")
                    setProperty("artifactId", "log4kt")
                    setProperty("packaging", "jar")
                    setProperty("description", "A very simple SLF4J implementation written in Kotlin.")
                    setProperty("url", "https://github.com/zp4rker/log4kt")

                    withGroovyBuilder {
                        "name"("Log4Kt")
                        "artifactId"("log4kt")
                        "packaging"("jar")
                        "description"("A very simple SLF4J implementation written in Kotlin.")
                        "url"("https://github.com/zp4rker/log4kt")

                        "scm" {
                            "connection"("scm:git:git://github.com/zp4rker/log4kt.git")
                            "url"("https://github.com/zp4rker/log4kt")
                        }

                        "licenses" {
                            "license" {
                                "name"("The Apache License, Version 2.0")
                                "url"("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }

                        "developers" {
                            "developer" {
                                "id"("zp4rker")
                                "name"("Zaeem Parker")
                                "email"("iam@zp4rker.com")
                            }
                        }
                    }
                }
            }
        }
    }
}