package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import de.scaramangado.lily.core.communication.Dispatcher
import ootbingo.barinade.bot.extensions.exception
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import org.slf4j.LoggerFactory

class AntiBingoRaceConnection(
    raceEndpoint: String,
    connector: WebsocketConnector,
    private val status: RaceStatusHolder,
    private val dispatcher: Dispatcher,
    private val disconnect: RaceWebsocketDelegate.() -> Unit,
    private val racetimeHttpClient: RacetimeHttpClient,
) : RaceWebsocketDelegate {

  private val logger = LoggerFactory.getLogger(AntiBingoRaceConnection::class.java)
  private val websocket: RaceWebsocketHandler = connector.connect(raceEndpoint, this)

  override val slug: String = raceEndpoint.split("/").last()

  override fun onMessage(message: RacetimeMessage) {

    when (message) {
      is RaceUpdate -> onRaceUpdate(message.race)
      is ChatMessage -> onChatMessage(message)
    }
  }

  private fun onRaceUpdate(race: RacetimeRace) {

    if (status.raceStatus != race.status) {
      onRaceStatusChange(status.raceStatus, race.status, race)
    }

    status.race = race
    logger.debug("Update race status for $slug")
  }

  private fun onRaceStatusChange(old: RacetimeRace.RacetimeRaceStatus?, new: RacetimeRace.RacetimeRaceStatus, race: RacetimeRace) {

    logger.info("Status change in ${race.name}: $old -> $new")

    if (old == null && new in listOf(RacetimeRace.RacetimeRaceStatus.OPEN, RacetimeRace.RacetimeRaceStatus.INVITATIONAL)) {
      try {
        initialize(race)
      } catch (e: Exception) {
        websocket.sendMessage("Failed to disable autostart.")
        logger.exception("ERROR: Failed to initialize", e)
      }
    }

    if (new in listOf(RacetimeRace.RacetimeRaceStatus.FINISHED, RacetimeRace.RacetimeRaceStatus.CANCELLED)) {
      websocket.sendMessage("The race has concluded. Good bye.")
      logger.info("Closing websocket...")
      disconnect()
    }
  }

  private fun onChatMessage(chatMessage: ChatMessage) {
    logger.debug("${chatMessage.user?.name}: ${chatMessage.messagePlain}")
  }

  private fun initialize(race: RacetimeRace) {
    logger.info("Received initial race data for ${race.name}")
    racetimeHttpClient.editRace(slug) {
      autoStart = false
      infoBot = "Anti-Bingo"
    }
    websocket.sendMessage("Anti Bingo!")
  }
}
