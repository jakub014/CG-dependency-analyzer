plugins {
	signing
	id("java-library")
	id("org.jetbrains.dokka") version "1.4.10"
	id("maven-publish")
	kotlin("jvm") version "1.4.10"
	kotlin("plugin.serialization") version "1.4.10"
}

/* ---------------------------------------------------------
 *
 * Project config
 *
 * --------------------------------------------------------- */


// Base configuration
group = "com.github.ushiosan23"
version = "0.0.4"

// Repositories
repositories {
	mavenCentral()
	jcenter()
}

// Java configuration
java {
	withJavadocJar()
	withSourcesJar()
}

tasks {
	compileKotlin.configure {
		kotlinOptions.jvmTarget = "11"
	}

	compileTestKotlin.configure {
		kotlinOptions.jvmTarget = "11"
	}
}

// Library Dependencies
dependencies {
	/* kotlin */
	implementation(kotlin("stdlib"))
	implementation(kotlin("reflect"))
	/* serialization */
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
	/* coroutines */
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0-M1")
	/* test */
	implementation("junit", "junit", "4.12")
}

/* ---------------------------------------------------------
 *
 * Publish section
 *
 * --------------------------------------------------------- */

publishing {
	val stringVersion = rootProject.version as String
	val versionName = stringVersion.replace(".", "_")

	/* Configure publications */
	publications {

		/* Create maven publication */
		create<MavenPublication>("mavenJava") {
			groupId = rootProject.group as String
			artifactId = rootProject.name
			version = rootProject.version as String

			/* Set java components */
			from(components["java"])

			/* POM document */
			pom {
				name.set("${rootProject.name}_$versionName")
				description.set("Network utilities for JVM platform")
				url.set("https://github.com/Ushiosan23/JVM_NetworkUtils")

				// License
				licenses {
					license {
						name.set("MIT License")
						url.set("https://raw.githubusercontent.com/Ushiosan23/JVM_NetworkUtils/master/LICENSE")
					}
				}
				developers {
					developer {
						id.set("Ushiosan23")
						name.set("Ushiosan23")
						email.set("haloleyendee@outlook.com")
					}
				}
				scm {
					connection.set("scm:git:git://github.com/Ushiosan23/JVM_NetworkUtils.git")
					developerConnection.set("scm:git:ssh://github.com/Ushiosan23/JVM_NetworkUtils.git")
					url.set("https://github.com/Ushiosan23/JVM_NetworkUtils")
				}
			}

			/* Dependencies */
			versionMapping {
				usage("java-api") {
					fromResolutionOf("runtimeClasspath")
				}
				usage("java-runtime") {
					fromResolutionResult()
				}
			}
		}

	}

	/* Define repositories */
	repositories {
		/* maven repositories */
		maven {
			val targetRepoURL = if (stringVersion.endsWith("SNAPSHOT"))
				"https://oss.sonatype.org/content/repositories/snapshots"
			else
				"https://oss.sonatype.org/service/local/staging/deploy/maven2"

			name = "MavenCentralRepository"
			url = uri(targetRepoURL)

			/* credentials */
			credentials {
				username = rootProject.properties["mavenUser"] as String? ?: ""
				password = rootProject.properties["mavenPass"] as String? ?: ""
			}
		}
	}
}

signing {
	sign(publishing.publications["mavenJava"])
}

/* ---------------------------------------------------------
 *
 * Configure tasks
 *
 * --------------------------------------------------------- */

tasks.javadoc {
	if (JavaVersion.current().isJava9Compatible) {
		(options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
	}
}
