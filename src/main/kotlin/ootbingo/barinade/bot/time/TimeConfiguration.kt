package ootbingo.barinade.bot.time

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.time.Clock

@Configuration
class TimeConfiguration {

  @Bean
  fun clock() = Clock.System
}
