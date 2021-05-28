plugins {
    java
    id("org.springframework.boot")
    id("com.moowork.node")
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

val installNpmDependencies = tasks.register<com.moowork.gradle.node.npm.NpmTask>("installNpmDependencies") {
    description = "Installs dependencies from package.json"
    group = "build"
    setWorkingDir(file("src/main/js"))
    setArgs(listOf("install"))
    inputs.file("src/main/js/package.json")
    outputs.dir("src/main/js/node_modules")
}

val buildProd = tasks.register<com.moowork.gradle.node.npm.NpmTask>("buildProd") {
    dependsOn(installNpmDependencies)
    group = "build"
    setWorkingDir(file("src/main/js"))
    inputs.file("src/main/js/package.json")
    inputs.dir("src/main/js/src")
    outputs.files("src/main/resources/public/bundle.js")
    setArgs(listOf("run", "build:prod"))
}

val buildDev = tasks.register<com.moowork.gradle.node.npm.NpmTask>("buildDev") {
    dependsOn(installNpmDependencies)
    group = "build"
    setWorkingDir(file("src/main/js"))
    inputs.file("src/main/js/package.json")
    inputs.dir("src/main/js/src")
    outputs.files("src/main/resources/public/bundle.js")
    setArgs(listOf("run", "build:dev"))
}

tasks {
    getByName("processResources") {
        dependsOn(buildProd)
    }
    getByName<Test>("test") {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
    }
    getByName<Delete>("clean") {
        delete.add("src/main/js/node_modules")
        delete.add("src/main/resources/public/bundle.js")
    }
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation(project(":spring-boot-starter"))

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.junit.vintage", "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
}
