package ootbingo.barinade.bot.racing_services.racetime.racing

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import org.slf4j.LoggerFactory

class RaceConnection(raceEndpoint: String, connector: WebsocketConnector, private val status: RaceStatusHolder) {

  private val websocket: RaceWebsocketHandler = connector.connect(raceEndpoint, this)
  private val logger = LoggerFactory.getLogger(RaceConnection::class.java)

  fun onMessage(message: RacetimeMessage) {

    if (message is RaceUpdate) {
      onRaceUpdate(message.race)
    }
  }

  private fun onRaceUpdate(race: RacetimeRace) {

    if (status.raceStatus == null) {

      logger.info("Received initial race data for ${race.name}")

      websocket.sendMessage("Welcome to OoT Bingo. I will generate a card and a filename at the start of the race.")
      websocket.sendMessage("Commands: '!mode en', '!mode jp', '!mode blackout', '!mode short' and '!nobingo'")
      websocket.sendMessage("Current mode: JP")
    }

    status.race = race
    logger.debug("Update race status for ${status.slug}")
  }
}
