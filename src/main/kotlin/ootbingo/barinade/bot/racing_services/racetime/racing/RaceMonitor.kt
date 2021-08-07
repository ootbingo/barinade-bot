package ootbingo.barinade.bot.racing_services.racetime.racing

import ootbingo.barinade.bot.racing_services.racetime.api.RacetimeApiProperties
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.*
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RaceConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("gg.racetime.enable-racing", havingValue = "true")
class RaceMonitor(
    private val httpClient: RacetimeHttpClient,
    private val connectionFactory: RaceConnectionFactory,
    private val properties: RacetimeApiProperties,
) {

  private val raceConnections = mutableSetOf<String>()

  @Scheduled(fixedDelay = 5000)
  fun scanForRaces() {
    httpClient.getOpenRaces()
        .filter { it.status in listOf(OPEN, INVITATIONAL) }
        .filter { !it.goal.custom && it.goal.name == "Bingo" }
        .map { it.name.split("/")[1] }
        .filter { it !in raceConnections }
        .forEach {
          connectionFactory.openConnection(websocketUrl(it))
          raceConnections.add(it)
        }
  }

  private fun websocketUrl(slug: String) =
      "${properties.websocketBaseUrl}/ws/o/bot/$slug"
}
