plugins {
	`java-library`
	signing
	`maven-publish`
	`project-report`
	`build-dashboard`
	pmd
	checkstyle
	jacoco
	id("com.github.spotbugs")
	id("org.danilopianini.git-sensitive-semantic-versioning")
	id("org.danilopianini.javadoc.io-linker")
	id("org.danilopianini.publish-on-central")
}

gitSemVer {
    version = computeGitSemVer()
}

repositories {
    listOf("", "-eu", "-asia").forEach {
        maven(url = "https://maven-central$it.storage-download.googleapis.com/repos/central/data/")
    }
    mavenCentral()
}

dependencies {
    api("com.google.guava:guava:_")
    compileOnly("com.github.spotbugs:spotbugs-annotations:_")
    testCompileOnly(Libs.spotbugs_annotations)
    testImplementation("junit:junit:_")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports.create("html") {
        enabled = true
    }
    ignoreFailures = false
    setEffort("max")
    setReportLevel("low")
    File("${project.rootProject.projectDir}/findbugsExcludes.xml")
        .takeIf { it.exists() }
        ?.also { excludeFilter.set(it) }
}

pmd {
    ruleSets = listOf()
    ruleSetConfig = resources.text.fromFile("${project.rootProject.projectDir}/config/pmd/pmd.xml")
}

publishOnCentral {
    projectDescription.set(extra["projectDescription"].toString())
    projectLongName.set("Java boilerplate code")
}

if (System.getenv("CI") == true.toString()) {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Danilo Pianini")
                        email.set("danilo.pianini@gmail.com")
                        url.set("http://www.danilopianini.org/")
                    }
                }
            }
        }
    }
}
