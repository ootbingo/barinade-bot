package ootbingo.barinade.bot.misc

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant

@Configuration
class GlobalConfiguration {

  @Bean
  fun currentTimeSupplier(): () -> Instant = { Instant.now() }
}
