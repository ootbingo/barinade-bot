package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.client.RacetimeHttpClient
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti.AntiBingoStage
import org.slf4j.LoggerFactory

class AntiBingoRaceRoomLogic(
    private val status: RaceStatusHolder,
    stageHolder: Holder<AntiBingoStage>,
    private val racetimeHttpClient: RacetimeHttpClient,
    private val delegate: RaceRoomDelegate,
) : RaceRoomLogic {

  private val logger = LoggerFactory.getLogger(AntiBingoRaceRoomLogic::class.java)

  private var stage by stageHolder

  override val commands: Map<String, (ChatMessage) -> Unit> = emptyMap()

  override fun initialize(race: RacetimeRace) {

    logger.info("Received initial race data for ${race.name}")

    racetimeHttpClient.editRace(race.slug) {
      autoStart = false
      infoBot = "Anti-Bingo"
    }

    delegate.sendMessage("Anti-Bingo initialized")
    status.race = race
  }

  override fun onRaceUpdate(race: RacetimeRace) {
    status.race = race
  }
}
