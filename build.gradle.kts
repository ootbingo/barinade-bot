import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java
  id("org.springframework.boot") version "2.1.8.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  kotlin("jvm") version "1.3.50"
  kotlin("plugin.spring") version "1.3.50"
  kotlin("plugin.allopen") version "1.3.50"
  jacoco
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
  implementation("org.springframework.boot:spring-boot-starter-json")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")

  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  val jUnitVersion = "5.5.2"
  testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-params:$jUnitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "junit")
    exclude(module = "hamcrest-library")
    exclude(module = "hamcrest-core")
    exclude(module = "json-path")
    exclude(module = "jsonassert")
    exclude(module = "xmlunit-core")
  }

  testImplementation("org.assertj:assertj-core:3.13.2")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

tasks.withType<Jar> {

  archiveBaseName.set("barinade_bot")
  archiveVersion.set("")
}

tasks.withType<Test> {
  useJUnitPlatform()

  testLogging {
    events("passed", "skipped", "failed")
  }

  finalizedBy("jacocoTestReport")
}

allOpen {
  annotation("ootbingo.barinade.bot.compile.Open")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "1.8"
}

tasks.withType<Wrapper> {
  gradleVersion = "5.6.2"
}
