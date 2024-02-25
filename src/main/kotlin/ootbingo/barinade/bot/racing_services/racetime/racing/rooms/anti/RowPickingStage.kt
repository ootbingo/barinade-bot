package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.misc.Holder
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeActionButton
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeSurvey
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.RacetimeSurveyType
import ootbingo.barinade.bot.time.worker.WorkerTask
import ootbingo.barinade.bot.time.worker.WorkerThread
import ootbingo.barinade.bot.time.worker.WorkerThreadFactory
import org.slf4j.LoggerFactory
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RowPickingStage(
  completeStage: (AntiBingoState) -> Unit,
  stateHolder: Holder<AntiBingoState> = Holder(AntiBingoState("", emptyList(), emptyList())),
  private val workerThreadFactory: WorkerThreadFactory,
  private val editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
  private val sendMessage: (String, Map<String, RacetimeActionButton>?) -> Unit,
  private val kickUser: (RacetimeUser) -> Unit,
) : AntiBingoStage(completeStage) {

  private val logger = LoggerFactory.getLogger(RowPickingStage::class.java)
  private val rowPickingTimeLimit = 3.minutes // TODO Make configurable

  private var state by stateHolder
  private var stageCompleted = false
  private var countdownThread: WorkerThread? = null

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {

    state = initialState

    val bingoUrl = "https://ootbingo.github.io/bingo/bingo.html?version=10.4&seed=${generateSeed()}&mode=normal"

    editRace {
      infoBot = "Anti-Bingo $bingoUrl"
      chatMessageDelay = 90
    }

    sendMessage("Goal: $bingoUrl", null)

    sendMessage(
      "Chat messages (including your row pick) are delayed by 90 seconds. (UNLESS you are moderator or race-monitor)",
      mapOf(
        "Pick a Row" to RacetimeActionButton(
          message = "!pick \${row}",
          submit = "Pick",
          survey = listOf(
            RacetimeSurvey(
              name = "row",
              label = "Row",
              type = RacetimeSurveyType.SELECT,
              options = AntiBingoState.Row.asStringMap()
            )
          )
        )
      )
    )

    sendMessage("You have 3 minutes to pick a row. Otherwise a random row will be picked.", null)

    startCountdown()
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

    if (!command.messagePlain.matches("^!pick (BLTR|TLBR|(ROW|COL)[1-5])$".toRegex())) {
      return
    }

    val row = AntiBingoState.Row.valueOf(command.messagePlain.replace("!pick ", ""))

    logger.info("${picker.name} picked $row for ${entrantMapping.choosesFor.name}")

    if (entrantMapping.chosenRow == null) {
      sendMessage("${picker.name} picked", null)
    }

    entrantMapping.chosenRow = row

    checkForStageCompletion()
  }

  private fun generateSeed() = Random.nextInt(1, 1_000_000)

  private fun checkForStageCompletion() {

    if (stageCompleted) {
      return
    }

    if (state.entrantMappings.all { it.chosenRow != null }) {
      stageCompleted = true
      countdownThread?.cancel()
      completeStage(state)
    }
  }

  private fun startCountdown() {

    infix fun Duration.before(limit: Duration) = limit - this

    val tasks = listOf(
      WorkerTask(90.seconds before rowPickingTimeLimit, "WARN 90 seconds") { sendMessage("90 seconds left.", null) },
      WorkerTask(60.seconds before rowPickingTimeLimit, "WARN 60 seconds") { sendMessage("One minute left.", null) },
      WorkerTask(30.seconds before rowPickingTimeLimit, "WARN 30 seconds") { sendMessage("30 seconds left.", null) },
      WorkerTask(10.seconds before rowPickingTimeLimit, "WARN 10 seconds") {
        sendMessage("10 seconds left. Pick now!", null)
      },
      WorkerTask(rowPickingTimeLimit, "Force Start") { forceStart() },
    )

    countdownThread = workerThreadFactory.runWorkerThread("RPS/${shorten(state.slug)}", tasks)
  }

  private fun forceStart() {

    state.entrantMappings
      .filter { it.chosenRow == null }
      .onEach {
        logger.info("Random pick for ${it.choosesFor.name}")
        it.chosenRow = AntiBingoState.Row.entries.random()
      }.takeIf { it.isNotEmpty() }
      ?.also { sendMessage("Failed to pick: ${it.joinToString(", ") { m -> m.entrant.name }}", null) }

    checkForStageCompletion()
  }
}
