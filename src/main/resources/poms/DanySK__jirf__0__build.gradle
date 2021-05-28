import org.danilopianini.gradle.mavencentral.mavenCentral
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("org.danilopianini.git-sensitive-semantic-versioning")
    `java-library`
    jacoco
    id("com.github.spotbugs")
    pmd
    checkstyle
    id("org.jlleitschuh.gradle.ktlint")
    signing
    `maven-publish`
    id("org.danilopianini.publish-on-central")
}

repositories {
    mavenCentral()
}

gitSemVer {
    version = computeGitSemVer()
}

dependencies {
    implementation("com.google.guava:guava:_")
    implementation("org.apache.commons:commons-lang3:_")
    implementation("org.danilopianini:boilerplate:_")
    implementation("org.jgrapht:jgrapht-core:_")
    testImplementation("junit:junit:_")
}

tasks.withType<Test> {
    failFast = true
    testLogging {
        events("passed", "skipped", "failed", "standardError")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

spotbugs {
    setEffort("max")
    setReportLevel("low")
    showProgress.set(true)
    val excludeFile = File("${project.rootProject.projectDir}/config/spotbugs/excludes.xml")
    if (excludeFile.exists()) {
        excludeFilter.set(excludeFile)
    }
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports {
        create("html") { enabled = true }
    }
}

pmd {
    ruleSets = listOf()
    ruleSetConfig = resources.text.fromFile("${project.rootProject.projectDir}/config/pmd/pmd.xml")
}
checkstyle {
    maxErrors = 0
    maxWarnings = 0
}

group = "org.danilopianini"
publishOnCentral {
    projectDescription = "An advanced factory supporting implicit type conversions"
    projectLongName = "Java Implicit Reflective Factory"
    repository("https://maven.pkg.github.com/danysk/jirf") {
        user = "DanySK"
        password = System.getenv("GITHUB_TOKEN")
    }
    repository("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/", "CentralS01") {
        user = mavenCentral().user()
        password = mavenCentral().password()
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

if (System.getenv("CI") == true.toString()) {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}
