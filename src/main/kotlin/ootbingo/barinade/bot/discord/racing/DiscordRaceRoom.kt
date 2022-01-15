package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntryState.*
import ootbingo.barinade.bot.discord.data.model.DiscordRaceState
import ootbingo.barinade.bot.discord.data.model.DiscordRaceState.*
import ootbingo.barinade.bot.extensions.greaterThan
import ootbingo.barinade.bot.extensions.ifFalse
import ootbingo.barinade.bot.misc.generateFilename

abstract class DiscordRaceRoom(
    private val status: DiscordRaceStatusHolder,
    private val discordChannel: TextChannel,
    private val raceStartExecutor: RaceStartExecutor,
    private val wait: WaitWrapper,
    private val countdownService: CountdownService,
) {

  fun enter(entrant: User): String? =
      ifState(OPEN)
          ?.let { status.addEntrant(entrant) }
          ?.takeIf { it }
          ?.let { "${entrant.name} entered the race" }

  fun unenter(entrant: User): String? =
      ifState(OPEN)
          ?.let { status.removeEntrant(entrant) }
          ?.takeIf { it }
          ?.let { "${entrant.name} left the race" }

  fun ready(entrant: User): String? =
      ifState(OPEN)
          ?.let { status.setStatusForEntrant(entrant, READY) }
          ?.takeIf { it }
          ?.run {
            val counts = status.countPerEntrantState()
            if (counts[NOT_READY]?.equals(0) != false && counts[READY]?.greaterThan(1) == true) {
              discordChannel.sendMessage("${entrant.name} is ready").complete()
              start()
              return@run null
            }
            return@run this
          }
          ?.let { "${entrant.name} is ready".appendNotReadyCount() }

  fun unready(entrant: User): String? =
      ifState(OPEN)
          ?.let { status.setStatusForEntrant(entrant, NOT_READY) }
          ?.takeIf { it }
          ?.let { "${entrant.name} is not ready" }

  open fun bingosync(entrant: User): String? = null

  protected fun start() {

    (1..10)
        .asSequence()
        .map { readyToStart().ifFalse { wait(1000) } }
        .firstOrNull { it }
        ?: return

    raceStartExecutor.invoke {

      status.state = STARTING
      countdownService.postCountdownInChannel(discordChannel)
      discordChannel.sendMessage("Filename: ${generateFilename()}").queue()
      status.state = PROGRESS
      status.setStatusForAll(PLAYING)
    }
  }

  protected abstract fun readyToStart(): Boolean

  private fun ifState(vararg allowedStates: DiscordRaceState) =
      status.state.takeIf { it in allowedStates }

  private fun String.appendNotReadyCount() =
      status.countPerEntrantState()[NOT_READY]
          ?.takeIf { it > 0 }
          ?.let { "$this ($it remaining)" }
          ?: this
}
