import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.JavaVersion

plugins {
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

version = project.findProperty("mod_version") as String
group = project.findProperty("maven_group") as String

var gameVersion = project.findProperty("minecraft_version")


base {
    archivesName.set(project.findProperty("archives_base_name") as String)
}

loom {
    splitEnvironmentSourceSets()
    mods {
        create("rps") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }

    }

}

repositories {
    mavenCentral()
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${gameVersion}")
    mappings("net.fabricmc:yarn:${project.findProperty("yarn_mappings") as String}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.findProperty("loader_version") as String}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.findProperty("fabric_version") as String}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.findProperty("fabric_kotlin_version") as String}")

}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}


tasks.build {
    finalizedBy(tasks.named("renameJar"))
}

// Uncomment and configure the maven publication if needed
// publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            artifactId = project.base.archivesName.get()
//            from(components["java"])
//        }
//    }
//
//    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
//    repositories {
//        // Add repositories to publish to here.
//        // Notice: This block does NOT have the same function as the block in the top level.
//        // The repositories here will be used for publishing your artifact, not for
//        // retrieving dependencies.
//    }
//}