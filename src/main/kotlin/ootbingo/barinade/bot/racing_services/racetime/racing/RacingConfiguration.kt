package ootbingo.barinade.bot.racing_services.racetime.racing

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RacingConfiguration {

  @Bean
  fun raceConnectionFactory() = object : RaceConnectionFactory {
    override fun openConnection(raceEndpoint: String) {
      RaceConnection(raceEndpoint)
    }
  }
}
