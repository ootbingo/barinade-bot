package ootbingo.barinade.bot

import de.scaramangado.lily.core.application.Lily
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["de.scaramangado.lily", "ootbingo.barinade.bot"])
class BarinadeBot

fun main(vararg args: String) {
  Lily.run(BarinadeBot::class.java, *args)
}
