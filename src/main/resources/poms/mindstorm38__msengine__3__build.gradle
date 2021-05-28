//
//  [MSEngine] Examples module
//

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

description = "MSEngine - Examples."

dependencies {
    "implementation"(project(":client"))
}

tasks.register<JavaExec>("window") {
    group = "Examples"
    description = "Test MSE windows"
    classpath = project.the<SourceSetContainer>()["main"].runtimeClasspath
    main = "io.msengine.example.window.WindowExample"
}

tasks.register<JavaExec>("gui") {
    group = "Examples"
    description = "Test MSE GUI framework"
    classpath = project.the<SourceSetContainer>()["main"].runtimeClasspath
    main = "io.msengine.example.gui.GuiExample"
}


tasks.named<ShadowJar>("shadowJar") {
    archiveBaseName.set("gui-shadow")
    manifest {
        attributes(mapOf("Main-Class" to "io.msengine.example.gui.GuiExample"))
    }
}