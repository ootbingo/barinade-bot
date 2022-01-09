package ootbingo.barinade.bot.discord.racing

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DiscordRacingWrappers {

  @Bean
  fun waitWrapper(): WaitWrapper = Thread::sleep

  @Bean
  fun raceStartExecutor(): RaceStartExecutor = { it() }
}
