import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    `java-library`
    id("io.freefair.lombok")
    id("maven-publish")
    id("com.moowork.node")
    id("com.github.hierynomus.license-report")
    signing
}

group = rootProject.group
version = rootProject.version

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.getByName<Delete>("clean") {
    delete.add("src/main/js/bridge/node_modules")
    delete.add("src/main/js/bridge/dist")
    delete.add("src/main/js/bridge-react/node_modules")
    delete.add("src/main/js/bridge-react/dist")
}

tasks.getByName<Javadoc>("javadoc") {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

val installBridgeDependencies = tasks.register<com.moowork.gradle.node.npm.NpmTask>("installBridgeDependencies") {
    description = "Installs dependencies from package.json for bridge"
    group = "build"
    setWorkingDir(file("src/main/js/bridge"))
    setArgs(listOf("install"))
    inputs.file("src/main/js/bridge/package.json")
    outputs.dir("src/main/js/bridge/node_modules")
}

val installBridgeReactDependencies = tasks.register<com.moowork.gradle.node.npm.NpmTask>("installBridgeReactDependencies") {
    description = "Installs dependencies from package.json for bridge-react"
    group = "build"
    setWorkingDir(file("src/main/js/bridge-react"))
    setArgs(listOf("install"))
    inputs.file("src/main/js/bridge-react/package.json")
    outputs.dir("src/main/js/bridge-react/node_modules")
}

val buildBridge = tasks.register<com.moowork.gradle.node.npm.NpmTask>("buildBridge") {
    dependsOn(installBridgeDependencies)
    group = "build"
    setWorkingDir(file("src/main/js/bridge"))
    inputs.file("src/main/js/bridge/package.json")
    inputs.dir("src/main/js/bridge/src")
    outputs.dir("src/main/js/bridge/dist")
    setArgs(listOf("run", "build"))
}

val buildBridgeReact = tasks.register<com.moowork.gradle.node.npm.NpmTask>("buildBridgeReact") {
    dependsOn(installBridgeReactDependencies)
    group = "build"
    setWorkingDir(file("src/main/js/bridge-react"))
    inputs.file("src/main/js/bridge-react/package.json")
    inputs.dir("src/main/js/bridge-react/src")
    outputs.dir("src/main/js/bridge-react/dist")
    setArgs(listOf("run", "build"))
}

tasks.getByName("build") {
    dependsOn(buildBridge, buildBridgeReact)
}

publishing {
    publications {
        create<MavenPublication>("templating") {
            artifactId = "templating"

            from(components["java"])

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }

            pom {
                name.set("Graal templating engine")
                description.set("Rendering engine using GraalVM.")
                url.set("https://wttech.github.io/graal-templating")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("tokrug")
                        name.set("Tomasz Krug")
                        email.set("tomasz.krug@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/wttech/graal-templating.git")
                    url.set("https://github.com/wttech/graal-templating")
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["templating"])
}

val graalVersion = "20.3.0"
val reactorVersion = "3.4.2"
val reactorPoolVersion = "0.2.1"
val jacksonVersion = "2.12.0"
val slf4jVersion = "1.7.30"

dependencies {
    // GraalVM JS module
    implementation("org.graalvm.sdk:graal-sdk:${graalVersion}")
    implementation("org.graalvm.truffle:truffle-api:${graalVersion}")
    implementation("org.graalvm.js:js:${graalVersion}")
    // Reactor
    api("io.projectreactor:reactor-core:${reactorVersion}")
    // Reactor Pool
    implementation("io.projectreactor.addons:reactor-pool:${reactorPoolVersion}")
    // Jackson
    api("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    // Logging
    implementation("org.slf4j:slf4j-api:${slf4jVersion}")

    // Junit 5
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.junit.platform:junit-platform-runner:1.6.0")
    // Reactor
    testImplementation("io.projectreactor:reactor-test:${reactorVersion}")
    // Assertion
    testImplementation("org.assertj:assertj-core:3.11.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showCauses = true
        showExceptions = true
        showStackTraces = true
        events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks {
    downloadLicenses {
        includeProjectDependencies = true
        dependencyConfiguration = "runtimeClasspath"
    }
}
