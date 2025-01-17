import com.vanniktech.maven.publish.SonatypeHost
import kotlin.io.path.*
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries

plugins {
    kotlin("jvm") version "2.0.10"
    id("org.openapi.generator") version "7.6.0"
    kotlin("plugin.serialization") version "2.0.10"
    id("com.vanniktech.maven.publish") version "0.28.0"
}

val githubUser = "goquati"
val githubProject = "web2pdf"
val artifactId = githubProject
val groupStr = "io.github.$githubUser"
val versionStr = System.getenv("GIT_TAG_VERSION") ?: "1.0-SNAPSHOT"
group = groupStr
version = versionStr

repositories {
    mavenCentral()
}
dependencies {
    val ktorVersion = "3.0.3"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    testImplementation(kotlin("test"))
}
kotlin {
    jvmToolchain(21)
}
sourceSets {
    main {
        kotlin {
            srcDir("$rootDir/kotlin-sdk/build/generated/oas/src/main/kotlin")
        }
    }
}

val openApiOutputDir = Path("$rootDir/kotlin-sdk/build/generated/oas")
openApiGenerate {
    generatorName.set("kotlin")
    library.set("jvm-ktor")
    inputSpec.set("$rootDir/oas.yaml")
    outputDir.set(openApiOutputDir.toString())
    // generate templates
    // docker run --rm -v ${PWD}:/out openapitools/openapi-generator-cli:v7.6.0 author template -g kotlin --library jvm-ktor
    templateDir.set("$rootDir/kotlin-sdk/src/main/resources/templates")
    packageName.set("$groupStr.$artifactId")
    validateSpec.set(true)
    configOptions.set(
        mapOf(
            "serializationLibrary" to "kotlinx_serialization",
            "dateLibrary" to "kotlinx-datetime",
            "modelMutable" to "true",
        )
    )
}

mavenPublishing {
    coordinates(
        groupId = project.group as String,
        artifactId = artifactId,
        version = project.version as String
    )
    pom {
        name = artifactId
        description =
            "SDK for converting HTML, Markdown, and web content to PDFs with customizable options using a Kotlin DSL."
        url = "https://github.com/$githubUser/$githubProject"
        licenses {
            license {
                name = "MIT License"
                url = "https://github.com/$githubUser/$githubProject/blob/main/LICENSE"
            }
        }
        developers {
            developer {
                id = githubUser
                name = githubUser
                url = "https://github.com/$githubUser"
            }
        }
        scm {
            url = "https://github.com/${githubUser}/${githubProject}"
            connection = "scm:git:https://github.com/${githubUser}/${githubProject}.git"
            developerConnection = "scm:git:git@github.com:${githubUser}/${githubProject}.git"
        }
    }
    publishToMavenCentral(
        SonatypeHost.CENTRAL_PORTAL,
        automaticRelease = true,
    )
    signAllPublications()
}

tasks.named("compileKotlin") {
    dependsOn("openApiGenerate")
}
tasks.named("kotlinSourcesJar") {
    dependsOn("openApiGenerate")
}
tasks.named("openApiGenerate") {
    doLast {
        val packagePath = "$openApiOutputDir/src/main/kotlin/io/github/goquati/web2pdf"
        Path("$packagePath/auth").apply {
            listDirectoryEntries().forEach { it.deleteExisting() }
            deleteExisting()
        }
        Path("$packagePath/infrastructure/HttpResponse.kt").deleteExisting()
    }
}

tasks.test {
    useJUnitPlatform()
}