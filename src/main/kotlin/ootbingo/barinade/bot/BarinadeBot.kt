package ootbingo.barinade.bot

import de.scaramanga.lily.core.application.Lily
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["de.scaramanga.lily", "ootbingo.barinade.bot"])
class BarinadeBot

fun main() {
  Lily.run(BarinadeBot::class.java)
}
