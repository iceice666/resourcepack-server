pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()

        maven {
            name = "Fabric Maven"
            url = uri("https://maven.fabricmc.net")
        }

        maven {
            name = "Jitpack Maven"
            url = uri("https://jitpack.io")
        }

        maven {
            name = "Cotton"
            url = uri("https://server.bbkr.space/artifactory/libs-release")
        }
    }
}

//val versions = listOf(
//    "1.19.4",
//    "1.20.1",
//    "1.20.2",
//    "1.20.4",
//    "1.21.0"
//)
//
//versions.forEach { version ->
//    include(":$version")
//    project(":$version").apply {
//        projectDir = file("versions/$version")
//        buildFileName = "../../common.gradle"
//    }
//}
//
//include(":fabricWrapper")
