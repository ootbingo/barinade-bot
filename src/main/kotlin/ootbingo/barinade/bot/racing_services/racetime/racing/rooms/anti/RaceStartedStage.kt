package ootbingo.barinade.bot.racing_services.racetime.racing.rooms.anti

import ootbingo.barinade.bot.misc.generateFilename
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeEditableRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeRace.RacetimeRaceStatus.*
import ootbingo.barinade.bot.racing_services.racetime.api.model.RacetimeUser
import ootbingo.barinade.bot.racing_services.racetime.racing.rooms.ChatMessage
import ootbingo.barinade.bot.time.worker.WorkerTask
import ootbingo.barinade.bot.time.worker.WorkerThreadFactory
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RaceStartedStage(
  completeStage: (AntiBingoState) -> Unit,
  private val editRace: (RacetimeEditableRace.() -> Unit) -> Unit,
  private val workerThreadFactory: WorkerThreadFactory,
  private val sendMessage: (String, RacetimeUser?) -> Unit,
) : AntiBingoStage(completeStage) {

  private lateinit var state: AntiBingoState
  private var raceStartMessagesSent = false

  override fun initialize(initialState: AntiBingoState, race: RacetimeRace) {

    state = initialState

    editRace {
      autoStart = true
      chatMessageDelay = 0
    }

    sendMessage("Chat message delay disabled", null)

    startRowPickingInfoWorker()
  }

  override fun raceUpdate(race: RacetimeRace) {

    if (raceStartMessagesSent || race.status != IN_PROGRESS) {
      return
    }

    raceStartMessagesSent = true

    sendMessage("Filename: ${generateFilename()}", null)

    state.entrantMappings.forEach {
      sendMessage("Your row is ${it.chosenRow?.formatted}", it.choosesFor)
    }
  }

  override fun handleCommand(command: ChatMessage) {
    // Do nothing
  }

  private fun startRowPickingInfoWorker() {

    val task = WorkerTask(1.minutes + 10.seconds, "Inform about row picks") {
      state.entrantMappings.forEach {
        sendMessage("${it.entrant.name} chose ${it.chosenRow?.formatted} for ${it.choosesFor.name}", null)
      }
    }

    workerThreadFactory.runWorkerThread("RSS/${shorten(state.slug)}", listOf(task))
  }
}
