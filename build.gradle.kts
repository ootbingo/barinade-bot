import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileWriter
import java.nio.charset.StandardCharsets.*
import java.util.Properties

plugins {
  java
  id("org.springframework.boot") version "2.3.2.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  kotlin("jvm") version "1.3.50"
  kotlin("plugin.spring") version "1.3.50"
  kotlin("plugin.allopen") version "1.3.50"
  id("org.sonarqube") version "2.7.1"
  jacoco
}

group = "ootbingo.barinade"
version = "1.0.0-M1"

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

  implementation("de.scaramanga:lily:0.1.2")
  implementation("org.springframework.boot:spring-boot-starter-json")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")

  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  implementation("com.google.code.gson:gson")

  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "junit-vintage-engine")
    exclude(module = "hamcrest-library")
    exclude(module = "hamcrest-core")
    exclude(module = "json-path")
    exclude(module = "jsonassert")
    exclude(module = "xmlunit-core")
    exclude(group = "org.junit.jupiter")
  }

  testImplementation("org.assertj:assertj-core:3.13.2")
  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

tasks.withType<Jar> {

  dependsOn("versionProperties")

  archiveBaseName.set("barinade_bot")
  archiveVersion.set("")
}

val versionProperties by tasks.register("versionProperties") {

  group = "build"
  description = "Saves version and build number in the resources folder."

  val properties = Properties()
  properties["barinade.version"] = version
  properties["barinade.build"] = executeCommand("git log -1 --format=%h")
  properties.store(FileWriter("src/main/resources/version.properties", UTF_8, false), null)
}

tasks.withType<Test> {
  useJUnitPlatform()

  systemProperties(Pair("spring.profiles.active", "test"))

  testLogging {
    events("passed", "skipped", "failed")
  }

  finalizedBy("jacocoTestReport")
}

tasks.withType<JacocoReport> {
  reports {
    xml.isEnabled = true
  }
}

sonarqube {

  val sonarUsername: String by project
  val sonarPassword: String by project

  properties {
    property("sonar.projectKey", "ootbingo_barinade-bot")
    property("sonar.organization", sonarUsername)
    property("sonar.host.url", "https://sonarcloud.io")
    property("sonar.login", sonarPassword)
    property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco/test/jacocoTestReport.xml")
  }
}

allOpen {
  annotation("ootbingo.barinade.bot.compile.Open")
}

fun executeCommand(command: String) =
    try {
      Runtime.getRuntime().exec(command).inputStream.bufferedReader().readLine()
    } catch (e: Exception) {
      ""
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
  gradleVersion = "6.3"
}
