package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeActionButton
import ootbingo.barinade.bot.time.worker.WorkerThreadFactory
import org.springframework.stereotype.Service

interface AntiBingoStageFactory {

  fun raceOpenStage(completeStage: (AntiBingoState) -> Unit, sendDm: (String, RacetimeUser) -> Unit): RaceOpenStage

  fun rowPickingStage(
    completeStage: (AntiBingoState) -> Unit,
    stateHolder: Holder<AntiBingoState> = Holder(antiBingoState()),
    editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
    sendMessage: (String, Map<String, RacetimeActionButton>?) -> Unit,
    kickUser: (RacetimeUser) -> Unit,
  ): RowPickingStage

  fun raceStartedStage(
    completeStage: (AntiBingoState) -> Unit,
    editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
    sendMessage: (String, RacetimeUser?) -> Unit,
  ): RaceStartedStage
}

@Service
class DefaultAntiBingoStageFactory(
  private val entrantPairGenerator: EntrantPairGenerator,
  private val workerThreadFactory: WorkerThreadFactory,
) : AntiBingoStageFactory {

  override fun raceOpenStage(completeStage: (AntiBingoState) -> Unit, sendDm: (String, RacetimeUser) -> Unit) =
    RaceOpenStage(entrantPairGenerator, completeStage, sendDm)

  override fun rowPickingStage(
    completeStage: (AntiBingoState) -> Unit,
    stateHolder: Holder<AntiBingoState>,
    editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
    sendMessage: (String, Map<String, RacetimeActionButton>?) -> Unit,
    kickUser: (RacetimeUser) -> Unit,
  ) = RowPickingStage(completeStage, stateHolder, workerThreadFactory, editRace, sendMessage, kickUser)

  override fun raceStartedStage(
    completeStage: (AntiBingoState) -> Unit,
    editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
    sendMessage: (String, RacetimeUser?) -> Unit,
  ) = RaceStartedStage(completeStage, editRace, workerThreadFactory, sendMessage)
}
