package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import de.scaramangado.lily.core.communication.Dispatcher
import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.lily.dispatch
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class RaceConnection(
  raceEndpoint: String,
  connector: WebsocketConnector,
  private val status: RaceStatusHolder,
  logicHolder: Holder<RaceRoomLogic>,
  private val dispatcher: Dispatcher,
  private val logicFactory: RaceRoomLogicFactory,
  private val disconnect: RaceWebsocketHandler.(Boolean) -> Unit,
) : RaceWebsocketDelegate, RaceRoomDelegate {

  private val websocket: RaceWebsocketHandler = connector.connect(raceEndpoint, this)
  private val logger = LoggerFactory.getLogger(RaceConnection::class.java)
  private var logic by logicHolder

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
    logic.onRaceUpdate(race)
    logger.debug("Update race status for $slug")
  }

  private fun onChatMessage(chatMessage: ChatMessage) {

    logger.debug("${chatMessage.user?.name}: ${chatMessage.messagePlain}")

    if (chatMessage.isBot || chatMessage.isSystem == true) {
      return
    }

    logic.commands.keys.find { chatMessage.messagePlain.startsWith(it) }
      ?.let { logic.commands[it] }
      ?.invoke(chatMessage)
      ?: dispatcher.dispatch(chatMessage)?.run { websocket.sendMessage(text) }
  }

  private fun onRaceStatusChange(old: RacetimeRaceStatus?, new: RacetimeRaceStatus, race: RacetimeRace) {

    logger.info("Status change in ${race.name}: $old -> $new")

    if (old == null && new in listOf(OPEN, INVITATIONAL)) {
      logger.info("Received initial race data for ${race.name}")
      changeAndInitializeLogic(BingoRaceRoomLogic::class, race)
    }
  }

  override fun setGoal(goal: String) {
    websocket.setGoal(goal)
  }

  override fun sendMessage(
    message: String,
    pinned: Boolean,
    directTo: String?,
    actions: Map<String, RacetimeActionButton>?,
  ) {
    websocket.sendMessage(message, pinned, directTo, actions)
  }

  override fun kickUser(user: RacetimeUser) {
    websocket.kickUser(user)
  }

  override fun closeConnection(delay: Boolean) {
    disconnect.invoke(websocket, delay)
  }

  override fun <T : RaceRoomLogic> changeLogic(type: KClass<T>) {
    logger.info("Initializing new logic of type ${type.simpleName} for ${status.slug}")
    changeAndInitializeLogic(type)
  }

  private fun <T : RaceRoomLogic> changeAndInitializeLogic(type: KClass<T>, race: RacetimeRace = status.race) {
    logic = logicFactory.createLogic(type, this)
    logic.initialize(race)
  }
}
