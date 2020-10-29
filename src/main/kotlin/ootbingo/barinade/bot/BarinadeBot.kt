package ootbingo.barinade.bot

import de.scaramanga.lily.core.application.Lily
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication(scanBasePackages = ["de.scaramanga.lily", "ootbingo.barinade.bot"])
class BarinadeBot

fun main(vararg args: String) {
  Lily.run(BarinadeBot::class.java, *args)
}
