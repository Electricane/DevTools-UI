import org.gradle.api.tasks.compile.GroovyCompile

plugins {
    java
    application
    groovy
    id("org.openjfx.javafxplugin") version "0.0.13"
//    id("org.graalvm.buildtools.native") version "0.11.1"
//    id("com.gluonhq.gluonfx-gradle-plugin") version "1.0.28"
}

group = "network.delay"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

//tasks.withType<GroovyCompile> {
//    groovyOptions.optimizationOptions?.set("indy", false)
//}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("network.delay.ui.Launcher")
}

javafx {
    version = "21.0.6"
    // Added javafx.web for QuadBlocks and javafx.media for general support
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.web", "javafx.media")
}

//graalvmNative {
//    binaries {
//        named("main") {
//            javaLauncher.set(javaToolchains.launcherFor {
//                languageVersion.set(JavaLanguageVersion.of(23))
//                vendor.set(JvmVendorSpec.matching("GraalVM"))
//            })
//
//            buildArgs.add("--initialize-at-run-time=com.sun.javafx.tk.quantum.QuantumToolkit")
//            buildArgs.add("--initialize-at-run-time=com.sun.glass.ui.win.WinApplication")
//            buildArgs.add("--initialize-at-run-time=com.sun.glass.ui.Cursor")
//            buildArgs.add("--initialize-at-run-time=com.sun.glass.ui.Screen")
//            buildArgs.add("--initialize-at-run-time=com.sun.prism.d3d.D3DResourceFactory")
//
//            buildArgs.add("--initialize-at-build-time=org.codehaus.groovy,groovy.lang")
//
//            buildArgs.add("-H:+AddAllCharsets")
//            buildArgs.add("--allow-incomplete-classpath")
//            buildArgs.add("--report-unsupported-elements-at-runtime")
//
//            buildArgs.add("-H:IncludeResources=.*\\.css$")
//            buildArgs.add("-H:IncludeResources=.*\\.fxml$")
//            buildArgs.add("-H:IncludeResources=.*\\.properties$")
//
////            buildArgs.add("--include-locals")
//        }
//    }
//}

//gluonfx { WIP
//    runtimeArgs.add("--initialize-at-run-time=com.sun.javafx.tk.quantum.QuantumToolkit");
//    runtimeArgs.add("--initialize-at-run-time=com.sun.glass.ui.win.WinApplication");
//    runtimeArgs.add("--initialize-at-run-time=com.sun.glass.ui.Cursor");
//    runtimeArgs.add("--initialize-at-run-time=com.sun.glass.ui.Screen");
//    runtimeArgs.add("--initialize-at-run-time=com.sun.prism.d3d.D3DResourceFactory");
//
//    compilerArgs.add("--initialize-at-build-time=org.codehaus.groovy,groovy.lang");
//    compilerArgs.add("--report-unsupported-elements-at-runtime");
//    compilerArgs.add("--allow-incomplete-classpath");
//    compilerArgs.add("-H:+AddAllCharsets");
//    compilerArgs.add("-H:IncludeResources=.*\\.css$");
//    compilerArgs.add("-H:IncludeResources=.*\\.fxml$");
//    compilerArgs.add("-H:IncludeResources=.*\\.properties$");
//}
//
dependencies {
    implementation("org.apache.groovy:groovy:4.0.21")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.apache.groovy:groovy:4.0.21")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//tasks.withType<org.gradle.api.tasks.compile.GroovyCompile> {
//    groovyOptions.optimizationOptions?.set("indy", false)
//}