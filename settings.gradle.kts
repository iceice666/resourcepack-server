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