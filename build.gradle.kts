plugins {
  java
  kotlin("jvm") version "1.3.50"
}

group = "ootbingo.barinade"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
  jcenter()
  maven {
    url = uri("http://h2841273.stratoserver.net:10091/repository/maven-releases/")
  }
}

dependencies {
  implementation("de.scaramanga:lily:0.1.0")
}

tasks.withType(Wrapper::class) {
  gradleVersion = "5.6.2"
}
