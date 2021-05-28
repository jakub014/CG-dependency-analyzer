plugins {
    id("java")
    id("java-library")
    kotlin("multiplatform") version "1.4.20"
    id("org.jetbrains.dokka")
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.4.20")
}

kotlin {
    targets {
        jvm {
            compilations.all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/main/kotlin")
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        val jvmTest by getting {
            kotlin.srcDir("src/test/kotlin")
            dependsOn(jvmMain)
            dependencies {
                implementation("io.kotest:kotest-runner-junit5-jvm:4.3.1")
                implementation("io.kotest:kotest-assertions-core-jvm:4.3.1")
                implementation("io.kotest:kotest-property-jvm:4.3.1")
            }

        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

apply(from = "../publish-sonatype.gradle.kts")
