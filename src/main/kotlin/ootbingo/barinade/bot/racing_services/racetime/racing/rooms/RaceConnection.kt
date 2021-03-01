package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.*
import org.slf4j.LoggerFactory
import kotlin.random.Random

class RaceConnection(raceEndpoint: String, connector: WebsocketConnector, private val status: RaceStatusHolder) {

  private val websocket: RaceWebsocketHandler = connector.connect(raceEndpoint, this)
  private val logger = LoggerFactory.getLogger(RaceConnection::class.java)

  fun onMessage(message: RacetimeMessage) {

    if (message is RaceUpdate) {
      onRaceUpdate(message.race)
    }
  }

  private fun onRaceUpdate(race: RacetimeRace) {

    if (status.raceStatus != race.status) {
      onRaceStatusChange(status.raceStatus, race.status, race)
    }

    status.race = race
    logger.debug("Update race status for ${status.slug}")
  }

  private fun onRaceStatusChange(old: RacetimeRaceStatus?, new: RacetimeRaceStatus, race: RacetimeRace) {

    if (old == null && new in listOf(OPEN, INVITATIONAL)) {
      logger.info("Received initial race data for ${race.name}")

      websocket.sendMessage("Welcome to OoT Bingo. I will generate a card and a filename at the start of the race.")
      websocket.sendMessage("Commands: '!mode en', '!mode jp', '!mode blackout', '!mode short' and '!nobingo'")
      websocket.sendMessage("Current mode: JP")
    }

    if (new == IN_PROGRESS && old != null && old !in listOf(FINISHED, CANCELLED)) {
      logger.debug("Race ${race.name} starting...")

      val goal = "https://ootbingo.github.io/bingo/v10.1/bingo.html?seed=${generateSeed()}&mode=normal"

      websocket.setGoal(goal)
      websocket.sendMessage("Filename: ${generateFilename()}")
      websocket.sendMessage("Goal: $goal")
    }
  }

  private fun generateSeed() = Random.nextInt(1, 1_000_000)

  private fun generateFilename(): String {

    val charPool: List<Char> = ('A'..'Z').toList()

    return (1..2)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
  }
}
