package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeActionButton
import org.springframework.stereotype.Service

interface AntiBingoStageFactory {

  fun raceOpenStage(completeStage: (AntiBingoState) -> Unit): RaceOpenStage

  fun rowPickingStage(
      completeStage: (AntiBingoState) -> Unit,
      stateHolder: Holder<AntiBingoState> = Holder(AntiBingoState(emptyList(), emptyList())),
      editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
      sendMessage: (String, Map<String, RacetimeActionButton>?) -> Unit,
      kickUser: (RacetimeUser) -> Unit,
  ): RowPickingStage

  fun raceStartedStage(completeStage: (AntiBingoState) -> Unit): RaceStartedStage
}

@Service
class DefaultAntiBingoStageFactory : AntiBingoStageFactory {

  override fun raceOpenStage(completeStage: (AntiBingoState) -> Unit) =
      RaceOpenStage(completeStage)

  override fun rowPickingStage(
      completeStage: (AntiBingoState) -> Unit,
      stateHolder: Holder<AntiBingoState>,
      editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
      sendMessage: (String, Map<String, RacetimeActionButton>?) -> Unit,
      kickUser: (RacetimeUser) -> Unit,
  ) = RowPickingStage(completeStage, stateHolder, editRace, sendMessage, kickUser)

  override fun raceStartedStage(completeStage: (AntiBingoState) -> Unit) =
      RaceStartedStage(completeStage)
}
