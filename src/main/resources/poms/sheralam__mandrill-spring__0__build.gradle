plugins {
    `java-library`
    `maven-publish`
    signing

}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

group = "io.github.sheralam"
version = "0.0.1"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    jcenter()
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    // implementation dependencies
    implementation("org.springframework.boot:spring-boot-starter-web:2.3.1.RELEASE")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.1")
    implementation("org.apache.commons:commons-lang3:3.9")

    // Use JUnit Jupiter API for testing.
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")

    // Use JUnit Jupiter Engine for testing.
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")


    // compile time dependencies
    compileOnly("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.mockito:mockito-core:3.4.4")


    // Test Runtime dependencies
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

val test by tasks.getting(Test::class) {
    // Use junit platform for unit tests
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}


publishing {
    publications {
        create<MavenPublication>("mandrillSpring") {
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
                name.set("Spring Mandrill Library")
                description.set("Spring Mandrill Library build on java 11")
                url.set("http://www.example.com/library")
                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("sheralam")
                        name.set("Khaja Md Sher E Alam")
                        email.set("sajibcse@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/sheralam/mandrill-spring.git")
                    developerConnection.set("scm:git:ssh://github.com/sheralam/mandrill-spring.git")
                    url.set("https://github.com/sheralam/mandrill-spring/")
                }
            }
        }
    }
    repositories {
       maven {
             url = uri("http://192.168.1.199:8082/artifactory/libs-release-local")
            credentials {
                username =  "username"
                password = "password"
            }

        }
    }
}


signing {
    isRequired = !version.toString().endsWith("-SNAPSHOT")
    useGpgCmd()
    sign(publishing.publications["mandrillSpring"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
