plugins {
    `java-library`
    kotlin("jvm") version "1.9.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.kteq"
version = "2.0.0"
description = "HiddenArmor"
java.sourceCompatibility = JavaVersion.VERSION_17

apply(plugin = "com.github.johnrengelman.shadow")
apply(plugin = "kotlin")

bukkit {
    version = rootProject.version.toString()
    name = rootProject.name
    main = "${rootProject.group}.${rootProject.name.lowercase()}.HiddenArmorPlugin"
    description = "Let players hide their armor and show off their skins"
    apiVersion = "1.20"
    authors = listOf("Kteq", "OUTBREAK")
    depend = listOf("ProtocolLib")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0")
    compileOnly("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    implementation("dev.jorel:commandapi-bukkit-shade:9.0.3")
    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.40.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
//    implementation("org.jetbrains.exposed:exposed-java-time:0.40.1")
    implementation("org.apache.commons:commons-text:1.10.0")
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    relocate("dev.jorel.commandapi", "${rootProject.group}.${rootProject.name.lowercase()}.commandapi")

    archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
    destinationDirectory.set(file("D:\\test_server_light\\plugins\\"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
