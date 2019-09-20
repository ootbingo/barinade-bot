import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  java
  id("org.springframework.boot") version "2.1.8.RELEASE"
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

  implementation("de.scaramanga:lily:0.1.1")

  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  val jUnitVersion = "5.5.2"
  testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test:2.1.8.RELEASE") {
    exclude(module = "junit")
    exclude(module = "hamcrest-library")
    exclude(module = "hamcrest-core")
    exclude(module = "json-path")
    exclude(module = "jsonassert")
    exclude(module = "xmlunit-core")
  }

  testImplementation("org.assertj:assertj-core:3.13.2")
}

tasks.withType(Jar::class) {

  archiveBaseName.set("barinade_bot")
  archiveVersion.set("")
}

tasks.withType<Test>() {
  useJUnitPlatform()
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
