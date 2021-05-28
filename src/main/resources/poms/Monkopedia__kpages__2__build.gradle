/*
 * Copyright 2020 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput.Target.COMMONJS

plugins {
    kotlin("multiplatform")
    application
}

version = "0.1"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlin-js-wrappers")
}

dependencies {
}

kotlin {
    js {
        useCommonJs()
        nodejs()
        browser {
            webpackTask {
                output.libraryTarget = COMMONJS
            }
        }
    }
    jvm {
        withJava()
    }
    sourceSets["commonMain"].dependencies {
        implementation(project(":kpages"))
    }
    sourceSets["jvmMain"].dependencies {
        implementation(project(":lanterna-ext"))
        implementation("org.slf4j:slf4j-api:1.6.1")
        implementation("ch.qos.logback:logback-classic:1.2.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    }
    sourceSets["jsMain"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
        implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-css:1.0.0-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-styled:5.2.0-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react:16.13.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.124-kotlin-1.4.10")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
        implementation("com.ccfraser.muirwik:muirwik-components:0.6.2")
        implementation(project(":kpages"))
        implementation(npm("codemirror", "5.58.3"))
        implementation(npm("showdown", "1.9.1"))
        implementation(npm("css-loader", "3.5.2"))
        implementation(npm("style-loader", "1.1.3"))
        implementation(npm("bootstrap", "^4.4.1"))
        implementation(npm("react", "~16.13.1"))
        implementation(npm("react-dom", "~16.13.1"))
    }
    sourceSets["jsTest"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-test-js")
    }
}

application {
    mainClassName = "com.monkopedia.kpages.demo.DemoKt"
}
