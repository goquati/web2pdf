import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("org.openapi.generator") version "7.6.0"
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    kotlin("plugin.spring") version "2.0.10"
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
}

group = "io.github.goquati"
version = "0.0.1"

project.group

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks {
    jar {
        enabled = true
    }
    bootJar {
        archiveFileName.set("web2pdf.jar")
        mainClass.set("${project.group}.Web2PdfApplicationKt")
    }
}


val buildDir = layout.buildDirectory.get()
val projectDir = layout.projectDirectory

sourceSets {
    main {
        kotlin {
            srcDir("$buildDir/generated/oas/src/main/kotlin")
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        freeCompilerArgs.add("-Xcontext-receivers")
        jvmTarget.set(JvmTarget.JVM_21)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
    }
}

dependencies {
    implementation("org.jetbrains:markdown:0.7.3")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("org.hildan.chrome:chrome-devtools-kotlin:6.3.0-1340018")
}

val openApiGenDstRoot = "de.smart.nexus.orchestrator"

openApiGenerate {
    generatorName.set("kotlin-spring")
    library.set("spring-boot")
    inputSpec.set("$rootDir/src/main/resources/oas.yaml")
    outputDir.set("$buildDir/generated/oas")
    apiPackage.set("$openApiGenDstRoot.api")
    invokerPackage.set("$openApiGenDstRoot.invoker")
    modelPackage.set("$openApiGenDstRoot.oas_model")
    validateSpec.set(true)
    modelNameSuffix.set("Dto")
    additionalProperties.set(mapOf("removeEnumValuePrefix" to "false"))
    configOptions.set(
        mapOf(
            "appendRequestToHandler" to "true",
            "interfaceOnly" to "true",
            "sourceFolder" to "src/main/kotlin",
            "useTags" to "true",
            "reactive" to "true",
            "skipDefaultInterface" to "true",
            "useSpringBoot3" to "true",
            "enumPropertyNaming" to "UPPERCASE",
        )
    )
}

tasks.compileKotlin { dependsOn(tasks.openApiGenerate) }

// start chrome for local development start:
// docker container run --rm -p 9222:9222 zenika/alpine-chrome --no-sandbox --remote-debugging-address=0.0.0.0 --remote-debugging-port=9222 about:blank
