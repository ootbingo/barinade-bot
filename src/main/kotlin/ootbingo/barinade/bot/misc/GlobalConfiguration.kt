package ootbingo.barinade.bot.misc

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import kotlin.time.Clock
import kotlin.time.Instant

@Configuration
class GlobalConfiguration {

  @Bean
  fun currentTimeSupplier(): () -> Instant = { Clock.System.now() }

  @Bean
  fun currentTimeSupplierJava(): () -> java.time.Instant = { java.time.Instant.now() }

  @Bean
  fun shameMessages(): () -> List<String> = {
    linesFromResourceFile("shame.txt")
  }

  @Bean
  fun themedWords(): () -> List<String> = {
    linesFromResourceFile("themed_words.txt")
  }

  @Bean
  fun parallelTaskScheduling(): ThreadPoolTaskScheduler {
    return ThreadPoolTaskScheduler().apply { poolSize = 10 }
  }

  @Bean
  fun commandExecutor(): CommandExecutor = {
    Runtime.getRuntime().exec(it.toTypedArray()).inputStream.bufferedReader().readText()
  }

  private fun linesFromResourceFile(filename: String): List<String> =
    GlobalConfiguration::class.java.classLoader
      .getResource(filename)
      ?.readText(Charsets.UTF_8)
      ?.lines()
      ?.filter { it.isNotBlank() }
      ?: emptyList()
}
