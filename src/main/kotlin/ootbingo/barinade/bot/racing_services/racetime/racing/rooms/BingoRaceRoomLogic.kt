package ootbingo.barinade.bot.racing_services.racetime.racing.rooms

import ootbingo.barinade.bot.misc.generateFilename
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.*
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.BingoRaceRoomLogic.Mode.*
import org.slf4j.LoggerFactory
import kotlin.random.Random

class BingoRaceRoomLogic(
    private val status: RaceStatusHolder,
    private val delegate: RaceRoomDelegate,
) : RaceRoomLogic {

  private val logger = LoggerFactory.getLogger(BingoRaceRoomLogic::class.java)

  private val modes by lazy {
    Mode.entries.associateBy { "!" + it.name.lowercase().replace("[^a-z]".toRegex(), "") }
  }

  override val commands: Map<String, (ChatMessage) -> Unit> = buildMap {

    modes.keys.forEach { modeCommand -> put(modeCommand) { arg -> changeMode(arg.messagePlain) } }

    put("!anti") { startAntiBingo(it.message) }
  }

  override fun initialize(race: RacetimeRace) {

    status.race = race

    if (race.status !in listOf(OPEN, INVITATIONAL)) {
      return
    }

    if (race.teamRace) {
      mode = BLACKOUT
    }

    delegate.sendMessage("Welcome to OoT Bingo. I will generate a card and a filename at the start of the race.")
    delegate.sendMessage(
        "Current mode: ${mode.name.lowercase()}",
        actions = raceModeActions,
    )
  }

  override fun onRaceUpdate(race: RacetimeRace) {

    if (status.raceStatus != race.status) {
      onRaceStatusChange(status.raceStatus, race.status)
    }

    status.race = race
  }

  private var mode: Mode = NORMAL

  private fun changeMode(command: String) {
    modes[command]?.run {
      mode = this
      logger.info("New mode for ${status.slug}: $mode")
      delegate.sendMessage(
          "Current mode: ${mode.name.lowercase()}",
          actions = raceModeActions,
      )
    }
  }

  private fun startAntiBingo(ignore: String) {

    if (status.raceStatus !in listOf(OPEN, INVITATIONAL)) {
      return
    }

    delegate.changeLogic<AntiBingoRaceRoomLogic>()
  }

  private fun onRaceStatusChange(old: RacetimeRace.RacetimeRaceStatus?, new: RacetimeRace.RacetimeRaceStatus) {

    logger.info("Status change in ${status.slug}: $old -> $new")

    if (new == IN_PROGRESS && old != null && old !in listOf(FINISHED, CANCELLED)) {
      startRace()
    }

    if (new in listOf(FINISHED, CANCELLED)) {
      leaveRace()
    }
  }

  private fun generateSeed() = Random.nextInt(1, 1_000_000)

  private fun startRace() {

    logger.debug("Race ${status.slug} starting...")

    if (mode == NO_BINGO) {
      return
    }

    val goal = if (mode != CHILD) {
      "https://ootbingo.github.io/bingo/bingo.html?version=10.4&seed=${generateSeed()}&mode=${mode.name.lowercase()}"
    } else {
      "https://doctorno124.github.io/childkek/bingo.html?seed=${generateSeed()}&mode=normal"
    }

    delegate.setGoal(goal)
    delegate.sendMessage("Filename: ${generateFilename()}")
    delegate.sendMessage("Goal: $goal", pinned = false)
    delegate.sendMessage("Goal: $goal", pinned = true)
  }

  private fun leaveRace() {
    delegate.sendMessage("The race has concluded. Good bye.")
    logger.info("Closing websocket...")
    delegate.closeConnection(delay = true)
  }

  private enum class Mode {
    NORMAL, BLACKOUT, SHORT, CHILD, NO_BINGO
  }

  private val raceModeActions = mapOf(
      "Change Mode" to RacetimeActionButton(
          message = "!\${mode}",
          submit = "Send",
          survey = listOf(
              RacetimeSurvey(
                  name = "mode",
                  label = "New Mode: ",
                  type = RacetimeSurveyType.SELECT,
                  default = "normal",
                  options = mapOf(
                      "normal" to "Normal",
                      "blackout" to "Blackout",
                      "short" to "Short",
                      "child" to "Child only",
                      "anti" to "Anti-Bingo (BETA, irreversible)"
                  ),
              ),
          ),
      ),
      "Don't Generate" to RacetimeActionButton(
          message = "!nobingo",
      ),
  )
}
