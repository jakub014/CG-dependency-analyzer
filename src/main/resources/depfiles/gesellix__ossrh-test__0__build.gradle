import java.text.SimpleDateFormat
import java.util.*

plugins {
  id("java-library")
  id("groovy")
  id("maven-publish")
  id("signing")
  id("io.freefair.maven-central.validate-poms") version "5.3.0"
  id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation("org.codehaus.groovy:groovy-all:2.5.13")
  testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
  testImplementation("junit:junit:4.13")
  api("org.apache.commons:commons-math3:3.6.1")
  implementation("com.google.guava:guava:29.0-jre")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("javadoc")
  from(tasks.javadoc)
}

val sourcesJar by tasks.registering(Jar::class) {
  dependsOn("classes")
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

artifacts {
  add("archives", sourcesJar.get())
  add("archives", javadocJar.get())
}

fun findProperty(s: String) = project.findProperty(s) as String?

val isSnapshot = project.version == "unspecified"
val artifactVersion = if (!isSnapshot) project.version as String else SimpleDateFormat("yyyy-MM-dd\'T\'HH-mm-ss").format(Date())!!
val publicationName = "ossrhTest"
publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/${property("github.package-registry.owner")}/${property("github.package-registry.repository")}")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("github.package-registry.username")
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("github.package-registry.password")
      }
    }
  }
  publications {
    register(publicationName, MavenPublication::class) {
      pom {
        name.set("ossrh-test")
        description.set("Test for an automated artifact publishing to Maven Central")
        url.set("https://github.com/gesellix/ossrh-test")
        licenses {
          license {
            name.set("MIT")
            url.set("https://opensource.org/licenses/MIT")
          }
        }
        developers {
          developer {
            id.set("gesellix")
            name.set("Tobias Gesellchen")
            email.set("tobias@gesellix.de")
          }
        }
        scm {
          connection.set("scm:git:github.com/gesellix/ossrh-test.git")
          developerConnection.set("scm:git:ssh://github.com/gesellix/ossrh-test.git")
          url.set("https://github.com/gesellix/ossrh-test")
        }
      }
      artifactId = "ossrh-test"
      version = artifactVersion
      from(components["java"])
      artifact(sourcesJar.get())
      artifact(javadocJar.get())
    }
  }
}

signing {
  val signingKey: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(signingKey, signingPassword)
  sign(publishing.publications[publicationName])
}

nexusPublishing {
  repositories {
    if (!isSnapshot) {
      sonatype {
        // custom repository name - 'sonatype' is pre-configured
        // for Sonatype Nexus (OSSRH) which is used for The Central Repository
        stagingProfileId.set(System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: findProperty("sonatype.staging.profile.id")) //can reduce execution time by even 10 seconds
        username.set(System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatype.username"))
        password.set(System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatype.password"))
      }
    }
  }
}
