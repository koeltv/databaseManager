@file:Suppress("INACCESSIBLE_TYPE")

val kotlinVersion: String by project
val javafakerVersion: String by project
val sqliteDriverVersion: String by project
val mysqlDriverVersion: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.beryx.runtime") version "1.13.0"

    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com.koeltv"
version = "1.0.0"

application {
    mainClass.set("com.koeltv.databasemanager.CommandLineInterfaceKt")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "17.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjfx:javafx-controls:19")
    implementation("org.openjfx:javafx-fxml:19")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")

    implementation("com.github.javafaker:javafaker:${javafakerVersion}")
    implementation("org.xerial:sqlite-jdbc:${sqliteDriverVersion}")
    implementation("com.mysql:mysql-connector-j:${mysqlDriverVersion}")

    testImplementation(kotlin("test"))
}

fun File.relocateScript(destinationFile: File, vararg replacePairs: Pair<String, String>) {
    val lineSeparator = if ("bat" in destinationFile.extension) "\r\n" else "\n"

    destinationFile.createNewFile()
    useLines { lines ->
        lines.forEach { line ->
            destinationFile.appendText(
                replacePairs.fold(line) { resultingLine, (old, new) ->
                    resultingLine.replace(old, new)
                } + lineSeparator
            )
        }
    }
    delete()
}

tasks.register("restructureDist") {
    dependsOn(tasks.installShadowDist)

    val imageDir = "${project.buildDir}/install/${rootProject.name}-shadow"

    onlyIf { File("$imageDir/bin").listFiles()?.size != 0 }

    doLast {
        File("$imageDir/bin/${rootProject.name}").relocateScript(
            File("$imageDir/${rootProject.name}"),
            "/.." to ""
        )

        File("$imageDir/bin/${rootProject.name}.bat").relocateScript(
            File("$imageDir/${rootProject.name}.bat"),
            ".." to ""
        )
    }
}

tasks.jar {
    dependsOn("restructureDist")
}

val jreVersion: String by project
val downloadPage: String by project
val baseModules: String by project
val targets: String by project

runtime {
    options.set(listOf("--strip-debug", "--compress", "1", "--no-header-files", "--no-man-pages"))
    modules.set(baseModules.split(','))
    imageZip.set(project.file("${project.buildDir}/dist/${rootProject.name}-${version}.zip"))

    targets.split(',').run {
        filter { target -> project.hasProperty(target) || none { project.hasProperty(it) } }
            .forEach {
                val fullPlatform = if (it == "win") "windows" else it
                val format = if (it == "win") "zip" else "tar.gz"
                val encodedJreVersion = jreVersion.replace("_", "%2B")
                val link =
                    "$downloadPage/jdk-$encodedJreVersion/OpenJDK17U-jdk_x64_${fullPlatform}_hotspot_$jreVersion.$format"
                targetPlatform(it) { setJdkHome(jdkDownload(link)) }
            }
    }
}

tasks.register("version") {
    doLast { println("v$version") }
}