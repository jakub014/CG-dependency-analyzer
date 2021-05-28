plugins {
    `java-library`
    id("io.freefair.lombok")
    id("maven-publish")
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

tasks.getByName<Javadoc>("javadoc") {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

publishing {
    publications {
        create<MavenPublication>("starter") {
            artifactId = "templating-spring-boot-starter"

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
                name.set("Graal templating engine Spring Boot starter")
                description.set("Spring Boot integration for Graal templating")
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
    sign(publishing.publications["starter"])
}

val springBootVersion = "2.4.1"

dependencies {
    api(project(":templating"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    api("org.springframework.boot:spring-boot-starter-webflux")

    annotationProcessor(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    // Junit 5
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.junit.platform:junit-platform-runner:1.6.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

tasks {
    downloadLicenses {
        includeProjectDependencies = true
        dependencyConfiguration = "runtimeClasspath"
    }
}
