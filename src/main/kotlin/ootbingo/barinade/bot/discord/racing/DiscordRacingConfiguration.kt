package ootbingo.barinade.bot.discord.racing

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

typealias WaitWrapper = Long.() -> Unit

@Configuration
class DiscordRacingConfiguration {

  @Bean
  fun waitWrapper(): WaitWrapper = Thread::sleep
}
