plugins {
    `java-library`
    kotlin("jvm") version "1.9.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.kteq.hiddenarmor"
version = "2.0.1"
description = "HiddenArmor"
java.sourceCompatibility = JavaVersion.VERSION_17

apply(plugin = "com.github.johnrengelman.shadow")
apply(plugin = "kotlin")

bukkit {
    version = rootProject.version.toString()
    name = rootProject.name
    main = "${rootProject.group}.HiddenArmorPlugin"
    description = "Let players hide their armor and show off their skins"
    apiVersion = "1.20"
    authors = listOf("Kteq", "OUTBREAK")
    depend = listOf("ProtocolLib", "CommandAPI")
    libraries = listOf("org.apache.commons:commons-text:1.11.0")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

val exposedVersion = "0.52.0"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    compileOnly("dev.jorel:commandapi-bukkit-core:9.7.0")

    implementation("org.jetbrains.exposed:exposed-core:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
    compileOnly("org.apache.commons:commons-text:1.11.0")
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    relocate("org.jetbrains.exposed", "${rootProject.group}.exposed")
    mergeServiceFiles()

    archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
    // destinationDirectory.set(file("I:\\OUTBREAK\\3.0\\vanilla\\plugins\\"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
