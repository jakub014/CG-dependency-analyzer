plugins {
    java
    kotlin("jvm")
    id(Libs.spring_boot_plugin_id)
    id(Libs.docker_spring_boot_plugin_id)
}

dependencies {
    implementation(Libs.kotlin_stdlib)
    implementation(Libs.kotlin_jdk8)
    implementation(Libs.kotlinx_coroutines_core)
    implementation(Libs.kotlinx_coroutines_jdk8)
    implementation(Libs.kotlinx_coroutines_reactor)

    implementation(Libs.log4j_kotlin)
    runtimeOnly(Libs.slf4j_over_log4j)

    implementation(Libs.spring_boot_starter_webflux) {
        exclude("org.springframework.boot", "spring-boot-starter-logging")
    }
}

fun getConfigurationProperty(envVar: String, sysProp: String): String? =
    System.getenv(envVar) ?: if (project.hasProperty(sysProp)) { project.property(sysProp) as? String } else null

docker {
    registryCredentials {
        getConfigurationProperty("DOCKER_REGISTRY", "docker.registry")?.let {
            url.set(it)
        }
        getConfigurationProperty("DOCKER_USERNAME", "docker.username")?.let {
            username.set(it)
        }
        getConfigurationProperty("DOCKER_PASSWORD", "docker.password")?.let {
            password.set(it)
        }
        getConfigurationProperty("DOCKER_EMAIL", "docker.email")?.let {
            email.set(it)
        }
    }
    springBootApplication {
        baseImage.set("adoptopenjdk/openjdk11:alpine")
        maintainer.set("Timur Kasatkin <t.kasatkin.o@gmail.com>")
        ports.set(listOf(8080))
        jvmArgs.set(listOf("-Dserver.port=8080", "-Dreactor.netty.ioWorkerCount=1"))
        images.set(
            listOf("${project.version}", "latest").map { "jfix-test-webflux-server:$it" }
        )
    }
}