import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.22"
    id("com.gradle.plugin-publish") version "1.2.1"
    id("com.epages.restdocs-api-spec") version "0.18.2"
    id("org.hidetake.swagger.generator") version "2.18.2"
}

val projectGroup: String by project
val applicationVersion: String by project

group = projectGroup
version = applicationVersion
java.sourceCompatibility = JavaVersion.VERSION_17

gradlePlugin {
    website = "https://github.com/Hejow/easy-restdocs-generator"
    vcsUrl = "https://github.com/Hejow/easy-restdocs-generator.git"

    plugins {
        create("easyRestdocsPlugin") {
            id = "io.github.hejow.easy-rest-docs"
            version = applicationVersion
            displayName = "easy-restdocs-generator gradle plugin"
            description = "Replacing the boilerplate codes with simple generator gives you an easier way to use rest-docs. And extends restdocs-api-spec to convert into SwaggerUI without any settings."
            tags = listOf("spring", "restdocs", "openapi3", "api", "specification")
            implementationClass = "io.github.hejow.restdocs.gradle.EasyRestdocsPlugin"
        }
    }
}

dependencies {
    swaggerUI("org.webjars:swagger-ui:4.1.3")
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
