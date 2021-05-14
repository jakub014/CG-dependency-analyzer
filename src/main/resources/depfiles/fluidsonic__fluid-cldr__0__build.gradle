import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.22"
}

fluidLibrary(name = "cldr", version = "0.9.3-37")

fluidLibraryModule(description = "Kotlin library for making CLDR data more easily accessible") {
	targets {
		jvm {
			dependencies {
				implementation(project(":fluid-cldr-data"))
			}
		}
	}
}
