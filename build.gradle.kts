import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java
  kotlin("jvm") version "1.3.50"
  kotlin("plugin.spring") version "1.3.50"
}

group = "ootbingo.barinade"
version = "0.0.1-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

repositories {
  mavenCentral()
  jcenter()
  maven {
    url = uri("http://h2841273.stratoserver.net:10091/repository/maven-releases/")
  }
}

dependencies {
  implementation("de.scaramanga:lily:0.1.0")
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
}

tasks.withType(Jar::class) {

  manifest {
    attributes["Main-Class"] = "ootbingo.barinade.bot.BarinadeBotKt"
  }

  from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "1.8"
}

tasks.withType(Wrapper::class) {
  gradleVersion = "5.6.2"
}
