package ootbingo.barinade.bot.discord.racing

import ootbingo.barinade.bot.discord.racing.DiscordRaceStatusHolder.RaceState.*

class DiscordRaceStatusHolder {

  var state: RaceState = OPEN

  fun addEntrant(entrant: DiscordEntrant): Boolean = TODO()
  fun removeEntrant(entrant: DiscordEntrant): Boolean = TODO()

  fun setStatusForEntrant(entrant: DiscordEntrant, newStatus: EntrantStatus): Boolean = TODO()
  fun setStatusForAll(newStatus: EntrantStatus): Unit = TODO()

  fun allReady(): Boolean = TODO()

  fun countPerStatus(): Map<EntrantStatus, Int> = TODO()

  enum class RaceState {
    OPEN, STARTING, UNDEFINED
  }

  enum class EntrantStatus {
    NOT_READY, READY, UNDEFINED
  }
}
