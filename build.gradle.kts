import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileWriter
import java.nio.charset.StandardCharsets.*
import java.util.*

plugins {

  val kotlinVersion = "1.6.10"

  java
  id("org.springframework.boot") version "2.6.3"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  id("org.sonarqube") version "3.3"
  jacoco
}

group = "ootbingo.barinade"
version = "3.0.0-Beta2"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
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
  maven {
    name = "m2-dv8tion"
    url = uri("https://m2.dv8tion.net/releases")
  }
}

dependencies {

  implementation("de.scaramangado:lily:0.2.0") {
    // TODO Upgrade Lily
    exclude(module = "JDA")
  }
  implementation("net.dv8tion:JDA:4.4.0_352") {
    exclude(module = "opus-java")
  }
  implementation("org.springframework.boot:spring-boot-starter-json")

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")

  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))

  implementation("com.google.code.gson:gson")

  implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:1.18")
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

  testImplementation("org.assertj:assertj-core:3.22.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
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

fun executeCommand(command: String) =
    try {
      Runtime.getRuntime().exec(command).inputStream.bufferedReader().readLine()
    } catch (e: Exception) {
      ""
    }

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
  jvmTarget = "17"
  freeCompilerArgs = listOf("-Xjvm-default=all")
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
  jvmTarget = "17"
}

tasks.withType<Wrapper> {
  gradleVersion = "7.3.2"
}
