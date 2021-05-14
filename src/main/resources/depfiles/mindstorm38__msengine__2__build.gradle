//
//  [MSEngine] Client side module
//

dependencies {

    val lwjglVersion = "3.2.3"
    val lwjglNatives = listOf("natives-windows", "natives-windows-x86", "natives-linux", "natives-macos")

    "api"(project(":common"))

    "api"(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    "api"("org.lwjgl", "lwjgl")
    "api"("org.lwjgl", "lwjgl-glfw")
    "api"("org.lwjgl", "lwjgl-jemalloc")
    "api"("org.lwjgl", "lwjgl-openal")
    "api"("org.lwjgl", "lwjgl-opengl")
    "api"("org.lwjgl", "lwjgl-stb")
    "api"("org.lwjgl", "lwjgl-vulkan")

    lwjglNatives.forEach { natives ->

        "runtimeOnly"("org.lwjgl", "lwjgl", classifier = natives)
        "runtimeOnly"("org.lwjgl", "lwjgl-glfw", classifier = natives)
        "runtimeOnly"("org.lwjgl", "lwjgl-jemalloc", classifier = natives)
        "runtimeOnly"("org.lwjgl", "lwjgl-openal", classifier = natives)
        "runtimeOnly"("org.lwjgl", "lwjgl-opengl", classifier = natives)
        "runtimeOnly"("org.lwjgl", "lwjgl-stb", classifier = natives)

        if (natives == "natives-macos") {
            "runtimeOnly"("org.lwjgl", "lwjgl-vulkan", classifier = natives)
        }

    }

}

/*configure<PublishingExtension> {

    publications {
        named<MavenPublication>("mavenJar") {

            pom.withXml {

                val dependenciesNode = (asNode().get("dependencies") as groovy.util.NodeList)[0] as groovy.util.Node
                val dependencyCommonNode = dependenciesNode.appendNode("dependency")

                dependencyCommonNode.appendNode("groupId", project.group)
                dependencyCommonNode.appendNode("artifactId", "msengine-common")
                dependencyCommonNode.appendNode("version", project.version)
                dependencyCommonNode.appendNode("scope", "compile")

            }

        }
    }

}*/