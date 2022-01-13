package ootbingo.barinade.bot.misc

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.time.Instant

@Configuration
class GlobalConfiguration {

  @Bean
  fun currentTimeSupplier(): () -> Instant = { Instant.now() }

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

  private fun linesFromResourceFile(filename: String): List<String> =
      GlobalConfiguration::class.java.classLoader
          .getResource(filename)
          ?.readText(Charsets.UTF_8)
          ?.lines()
          ?.filter { it.isNotBlank() }
          ?: emptyList()
}
