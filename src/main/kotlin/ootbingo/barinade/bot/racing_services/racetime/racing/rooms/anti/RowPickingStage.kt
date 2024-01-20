package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeActionButton
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeSurvey
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeSurveyType
import org.slf4j.LoggerFactory

class RowPickingStage(
    completeStage: (AntiBingoState) -> Unit,
    stateHolder: Holder<AntiBingoState> = Holder(AntiBingoState(emptyList(), emptyList())),
    private val editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
    private val sendMessage: (String, Map<String, RacetimeActionButton>?) -> Unit,
    private val kickUser: (RacetimeUser) -> Unit,
) : AntiBingoStage(completeStage) {

  private val logger = LoggerFactory.getLogger(RowPickingStage::class.java)

  private var state by stateHolder

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {

    state = initialState

    editRace {
      chatMessageDelay = 90
    }

    sendMessage(
        "Chat messages (including your row pick) are delayed by 90 seconds. You have 5 minutes to pick a row. If you fail to pick a row in time, a random row will be assigned.",
        mapOf("Pick a Row" to RacetimeActionButton(
            message = "!pick \${row}",
            submit = "Pick",
            survey = listOf(RacetimeSurvey(
                name = "row",
                label = "Row",
                type = RacetimeSurveyType.SELECT,
                options = AntiBingoState.Row.asStringMap()
            ))
        ))
    )
  }

  override fun raceUpdate(race: RacetimeRace) {
    race.entrants.filter { it.user !in state.entrants }.forEach {
      sendMessage("This race is already in the picking phase. No new entrants permitted.", null)
      logger.debug("Kicking user {}...", it.user.name)
      kickUser(it.user)
    }
  }

  override fun handleCommand(command: ChatMessage) {

    val picker = command.user
    val entrantMapping = state.entrantMappings.find { it.entrant == picker }

    if (picker == null) {
      logger.error("Picking user ${command.user} not known")
      return
    }

    if (entrantMapping == null) {
      logger.error("No user to pick for found")
      return
    }

    if (!command.messagePlain.matches("^!pick (BLTR|TLBR|(ROW|COL)[1-5])+$".toRegex())) {
      return
    }

    val row = AntiBingoState.Row.valueOf(command.messagePlain.replace("!pick ", ""))

    logger.info("${picker.name} picked $row for ${entrantMapping.choosesFor.name}")

    entrantMapping.chosenRow = row
  }
}
