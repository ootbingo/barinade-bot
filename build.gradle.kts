import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileWriter
import java.nio.charset.StandardCharsets.*
import java.util.Properties

plugins {

  val kotlinVersion = "1.5.21"

  java
  id("org.springframework.boot") version "2.3.4.RELEASE"
  id("io.spring.dependency-management") version "1.0.8.RELEASE"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  kotlin("plugin.allopen") version kotlinVersion
  id("org.sonarqube") version "2.7.1"
  jacoco
}

group = "ootbingo.barinade"
version = "2.1.0-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

repositories {
  mavenCentral()
  maven {
    url = uri("https://maven.pkg.github.com/scaramangado/lily")
    credentials {
      username = project.properties["githubPackagesUser"]?.let { it as String } ?: ""
      password = project.properties["githubPackagesToken"]?.let { it as String } ?: ""
    }
  }
}

dependencies {

  implementation("de.scaramangado:lily:0.2.0")
  implementation("org.springframework.boot:spring-boot-starter-json")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")

  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  implementation("com.google.code.gson:gson")

  implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:1.15")
  implementation("org.springframework:spring-websocket")
  implementation("org.springframework:spring-messaging")

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

  dependsOn("versionProperties")

  useJUnitPlatform()

  systemProperties(Pair("spring.profiles.active", "test"))

  testLogging {
    events("passed", "skipped", "failed")
  }

  finalizedBy("jacocoTestReport")
}

tasks.withType<JacocoReport> {
  reports {
    xml.required.set(true)
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
    property("sonar.exclusions", "**/*Configuration.kt")
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
  jvmTarget = "11"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "11"
}

tasks.withType<Wrapper> {
  gradleVersion = "7.1.1"
}
