package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel
import ootbingo.barinade.bot.discord.racing.DiscordRaceStatusHolder.*
import ootbingo.barinade.bot.discord.racing.DiscordRaceStatusHolder.EntrantStatus.*
import ootbingo.barinade.bot.discord.racing.DiscordRaceStatusHolder.RaceState.*
import ootbingo.barinade.bot.discord.racing.DiscordRaceStatusHolder.RaceState.UNDEFINED
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

  fun enter(entrant: DiscordEntrant): String? =
      ifState(OPEN)
          ?.let { status.addEntrant(entrant) }
          ?.takeIf { it }
          ?.let { "${entrant.username} entered the race" }

  fun unenter(entrant: DiscordEntrant): String? =
      ifState(OPEN)
          ?.let { status.removeEntrant(entrant) }
          ?.takeIf { it }
          ?.let { "${entrant.username} left the race" }

  fun ready(entrant: DiscordEntrant): String? =
      ifState(OPEN)
          ?.let { status.setStatusForEntrant(entrant, READY) }
          ?.takeIf { it }
          ?.let { "${entrant.username} is ready".appendNotReadyCount() }
          ?.also {
            val counts = status.countPerStatus()
            if (counts[NOT_READY]?.equals(0) != false && counts[READY]?.greaterThan(1) == true) {
              start()
            }
          }

  fun unready(entrant: DiscordEntrant): String? =
      ifState(OPEN)
          ?.let { status.setStatusForEntrant(entrant, NOT_READY) }
          ?.takeIf { it }
          ?.let { "${entrant.username} is not ready" }

  open fun bingosync(entrant: DiscordEntrant): String? = null

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
      status.state = UNDEFINED
      status.setStatusForAll(EntrantStatus.UNDEFINED)
    }
  }

  protected abstract fun readyToStart(): Boolean

  private fun ifState(vararg allowedStates: RaceState) =
      status.state.takeIf { it in allowedStates }

  private fun String.appendNotReadyCount() =
      status.countPerStatus()[NOT_READY]
          ?.takeIf { it > 0 }
          ?.let { "$this ($it remaining)" }
          ?: this
}
