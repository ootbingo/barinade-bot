package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.AntiBingoStage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.AntiBingoStageFactory
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.AntiBingoState
import org.slf4j.LoggerFactory

class AntiBingoRaceRoomLogic(
    private val status: RaceStatusHolder,
    stageHolder: Holder<AntiBingoStage>,
    private val racetimeHttpClient: RacetimeHttpClient,
    private val delegate: RaceRoomDelegate,
    private val stageFactory: AntiBingoStageFactory,
) : RaceRoomLogic {

  private val logger = LoggerFactory.getLogger(AntiBingoRaceRoomLogic::class.java)

  private var stage by stageHolder

  override val commands: Map<String, (ChatMessage) -> Unit> = mapOf(
      "!pick" to { stage.handleCommand(it) }
  )

  override fun initialize(race: RacetimeRace) {

    logger.info("Received initial race data for ${race.name}")

    racetimeHttpClient.editRace(race.slug) {
      autoStart = false
      infoBot = "Anti-Bingo"
    }

    delegate.sendMessage("Anti-Bingo initialized")
    status.race = race
    stage = stageFactory.raceOpenStage(
        { switchToRowPickingStage(it) },
        { message, user -> delegate.sendMessage(message, directTo = user.id) },
    ).also { it.initialize(AntiBingoState(race.entrants.map { e -> e.user }, listOf()), race) }
  }

  override fun onRaceUpdate(race: RacetimeRace) {
    status.race = race
    stage.raceUpdate(race)
  }

  private fun switchToRowPickingStage(state: AntiBingoState) {
    stage = stageFactory.rowPickingStage(
        { switchToRaceStartedStage(it) },
        Holder(state),
        { racetimeHttpClient.editRace(status.slug, it) },
        { message, actions ->
          delegate.sendMessage(message, actions = actions)
        },
        { /* TODO Implement kicking */ },
    ).also { it.initialize(state, status.race) }
  }

  private fun switchToRaceStartedStage(state: AntiBingoState) {
    stage = stageFactory.raceStartedStage(
        {},
        { racetimeHttpClient.editRace(status.slug, it) },
        { message, user -> delegate.sendMessage(message, directTo = user?.id) }
    ).also { it.initialize(state, status.race) }
  }
}
