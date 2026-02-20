import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("groovy")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "network.delay.ui"
version = "1.2.1-STABLE"

repositories {
    mavenCentral()
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.web")
}

val targetPlatforms = mapOf(
    "windows-x64" to "win",
    "mac-x64" to "mac",
    "mac-arm64" to "mac-aarch64",
    "linux-x64" to "linux",
    "linux-arm64" to "linux-aarch64"
)

dependencies {
    implementation("org.apache.groovy:groovy-all:4.0.21")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.auth0:java-jwt:4.4.0")

    targetPlatforms.values.forEach { classifier ->
        val jfxVersion = "21"
        listOf("controls", "graphics", "base", "fxml").forEach { mod ->
            runtimeOnly("org.openjfx:javafx-$mod:$jfxVersion:$classifier")
        }
    }
}

application {
    mainClass.set("network.delay.ui.Launcher")
}

targetPlatforms.forEach { (name, classifier) ->
    tasks.register<ShadowJar>("shadowJar-$name") {
        group = "distribution"
        archiveClassifier.set(name)
        
        from(sourceSets.main.get().output)
        configurations = listOf(project.configurations.runtimeClasspath.get())
        
        dependencies {
            targetPlatforms.values.forEach { otherClassifier ->
                exclude(dependency("org.openjfx:.*:.*:$otherClassifier"))
            }
            include(dependency("org.openjfx:.*:.*:$classifier"))
            
            include(dependency("org.codehaus.groovy:.*:.*"))
            include(dependency("com.google.code.gson:.*:.*"))
            include(dependency("com.auth0:.*:.*"))
        }

        manifest {
            attributes["Main-Class"] = "network.delay.ui.Launcher"
        }
        mergeServiceFiles()
    }
}

tasks.build {
    targetPlatforms.keys.forEach { name ->
        dependsOn("shadowJar-$name")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}