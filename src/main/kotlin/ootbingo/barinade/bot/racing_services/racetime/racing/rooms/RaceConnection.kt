package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import de.scaramangado.lily.core.communication.Dispatcher
import ootbingo.barinade.bot.misc.generateFilename
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.*
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RaceConnection.Mode.*
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.dispatch
import org.slf4j.LoggerFactory
import kotlin.random.Random

class RaceConnection(
    raceEndpoint: String, connector: WebsocketConnector, private val status: RaceStatusHolder,
    private val dispatcher: Dispatcher, private val disconnect: RaceConnection.() -> Unit,
) {

  private val websocket: RaceWebsocketHandler = connector.connect(raceEndpoint, this)
  private val logger = LoggerFactory.getLogger(RaceConnection::class.java)

  private var mode: Mode = NORMAL
  private val modes = mapOf(
      "!normal" to NORMAL,
      "!blackout" to BLACKOUT,
      "!short" to SHORT,
      "!child" to CHILD,
      "!nobingo" to NO_BINGO
  )

  val slug: String = raceEndpoint.split("/").last()

  fun onMessage(message: RacetimeMessage) {

    when (message) {
      is RaceUpdate -> onRaceUpdate(message.race)
      is ChatMessage -> onChatMessage(message)
    }
  }

  fun closeWebsocket() = websocket.disconnect()

  private fun onRaceUpdate(race: RacetimeRace) {

    if (status.raceStatus != race.status) {
      onRaceStatusChange(status.raceStatus, race.status, race)
    }

    status.race = race
    logger.debug("Update race status for $slug")
  }

  private fun onChatMessage(chatMessage: ChatMessage) {

    if (chatMessage.isBot || chatMessage.isSystem) {
      return
    }

    if (chatMessage.messagePlain in modes.keys) {
      mode = modes[chatMessage.messagePlain]!!
      logger.info("New mode for $slug: $mode")
      websocket.sendMessage("Current mode: ${mode.name.lowercase()}")
      return
    }

    dispatcher.dispatch(chatMessage)?.run { websocket.sendMessage(text) }
  }

  private fun onRaceStatusChange(old: RacetimeRaceStatus?, new: RacetimeRaceStatus, race: RacetimeRace) {

    logger.info("Status change in ${race.name}: $old -> $new")

    if (old == null && new in listOf(OPEN, INVITATIONAL)) {
      logger.info("Received initial race data for ${race.name}")

      if (race.teamRace) {
        mode = BLACKOUT
      }

      websocket.sendMessage("Welcome to OoT Bingo. I will generate a card and a filename at the start of the race.")
      websocket.sendMessage("Change modes: !normal, !blackout, !short, !child, !nobingo")
      websocket.sendMessage("Current mode: ${mode.name.lowercase()}")
    }

    if (new == IN_PROGRESS && old != null && old !in listOf(FINISHED, CANCELLED)) {
      logger.debug("Race ${race.name} starting...")

      if (mode == NO_BINGO) {
        return
      }

      val goal = if (mode != CHILD) {
        "https://ootbingo.github.io/bingo/bingo.html?version=10.3.1&seed=${generateSeed()}&mode=${mode.name.lowercase()}"
      } else {
        "https://doctorno124.github.io/childkek/bingo.html?seed=${generateSeed()}&mode=normal"
      }

      websocket.setGoal(goal)
      websocket.sendMessage("Filename: ${generateFilename()}")
      websocket.sendMessage("Goal: $goal")
    }

    if (new in listOf(FINISHED, CANCELLED)) {
      websocket.sendMessage("The race has concluded. Good bye.")
      logger.info("Closing websocket...")
      disconnect()
    }
  }

  private fun generateSeed() = Random.nextInt(1, 1_000_000)

  private enum class Mode {
    NORMAL, BLACKOUT, SHORT, CHILD, NO_BINGO
  }
}
