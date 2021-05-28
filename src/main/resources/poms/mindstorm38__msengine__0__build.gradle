//
//  [MSEngine]
//
//	- Java    : 11
//	- Gradle  : 6.7
//  - Version : 1.1.0
//

// Import from your gradle.properties
val ossrhUsername: String? by project
val ossrhPassword: String? by project

description = "A Java 3D engine on top of LWJGL 3, using OpenGL, GLFW and JOML"

allprojects {
    version = "1.1.0"
    group = "fr.theorozier"
}

subprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }
    }
}

project("common").description = "MSEngine - Common library, containing math utils and resources handling."
project("client").description = "MSEngine - Client side library, containing OpenGL natives."

project("common").mseLibrary()
project("client").mseLibrary()

fun Project.mseLibrary() {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    val snapshot = version.toString().endsWith("SNAPSHOT")

    dependencies {
        "api"("fr.theorozier", "sutil", "1.1.1-SNAPSHOT")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.named<JavaCompile>("compileJava") {
        options.encoding = "UTF-8"
    }

    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>()["main"].allSource)
    }

    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        from(tasks.named<Javadoc>("javadoc"))
    }

    configure<PublishingExtension> {

        publications {
            register<MavenPublication>("mavenJar") {

                from(components["java"])

                if (!snapshot) {
                    artifact(tasks.named<Jar>("sourcesJar").get())
                    artifact(tasks.named<Jar>("javadocJar").get())
                }

                pom {

                    artifactId = "${rootProject.name}-${project.name}"

                    name.set("MSE - ${artifactId}")
                    description.set(project.description)
                    url.set("https://github.com/mindstorm38/msengine")

                    developers {
                        developer {
                            id.set("fr.theorozier")
                            name.set("Théo Rozier")
                            email.set("contact@theorozier.fr")
                            url.set("https://github.com/mindstorm38")
                        }
                    }

                    licenses {
                        license {
                            name.set("GNU General Public License v3.0")
                            url.set("https://opensource.org/licenses/GPL-3.0")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/mindstorm38/msengine.git")
                        developerConnection.set("scm:git:ssh://github.com:mindstorm38/msengine.git")
                        url.set("https://github.com/mindstorm38/msengine/tree/master")
                    }

                }

            }
        }

        repositories {
            maven {

                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }

                val releaseRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                val snapshotRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"

                url = uri(if (snapshot) snapshotRepoUrl else releaseRepoUrl)

            }
        }

    }

    if (!snapshot) {
        configure<SigningExtension> {
            sign(the<PublishingExtension>().publications.named("mavenJar").get())
        }
    }

}