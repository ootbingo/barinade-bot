package ootbingo.barinade.bot.statistics

import ootbingo.barinade.bot.racing_services.data.PlayerHelper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QueryConfiguration(
    private val playerHelper: PlayerHelper,
    private val raceGoalValidator: RaceGoalValidator,
) {

  @Bean
  fun queryServiceFactory() = QueryServiceFactory { queryService }

  private val queryService by lazy { QueryService(playerHelper, raceGoalValidator) }
}
