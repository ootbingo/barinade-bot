package ootbingo.barinade.bot.racing_services.racetime.sync

import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import ootbingo.barinade.bot.racing_services.data.connection.RaceRepository
import ootbingo.barinade.bot.racing_services.data.connection.RaceResultRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RacetimeSyncConfiguration(private val playerHelper: PlayerHelper,
                                private val raceRepository: RaceRepository,
                                private val raceResultRepository: RaceResultRepository) {

  @Bean
  fun racetimeImporter(): () -> RacetimeImporter = {
    RacetimeImporter(playerHelper, raceRepository, raceResultRepository)
  }
}
