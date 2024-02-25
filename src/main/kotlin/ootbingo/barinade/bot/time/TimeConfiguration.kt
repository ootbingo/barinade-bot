package ootbingo.barinade.bot.time

import kotlinx.datetime.Clock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TimeConfiguration {

  @Bean
  fun clock() = Clock.System
}
