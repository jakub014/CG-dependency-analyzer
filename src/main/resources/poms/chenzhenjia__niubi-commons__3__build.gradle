/*
 * Copyright 2020 陈圳佳
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//apply(from = "../gradle/publish.gradle.kts")
dependencies {
    api("commons-collections:commons-collections:${properties["commonsCollectionsVersion"]}")
    api("org.springframework.boot:spring-boot-starter-validation")

    optionalImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    optionalImplementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5")
    optionalImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    optionalImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    optionalImplementation("org.springframework.security:spring-security-config")
    optionalImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    optionalImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}