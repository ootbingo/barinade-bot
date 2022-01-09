package ootbingo.barinade.bot.discord.racing

import ootbingo.barinade.bot.discord.racing.DiscordRaceStatusHolder.EntrantStatus.*
import ootbingo.barinade.bot.discord.racing.DiscordRaceStatusHolder.RaceState.*

class DiscordRaceStatusHolder {

  var state: RaceState = OPEN

  private val entrants = mutableMapOf<DiscordEntrant, EntrantStatus>()

  fun addEntrant(entrant: DiscordEntrant): Boolean {

    if (entrant !in entrants) {
      entrants += entrant to NOT_READY
      return true
    }

    return false
  }

  fun removeEntrant(entrant: DiscordEntrant): Boolean {

    if (entrant in entrants) {
      entrants -= entrant
      return true
    }

    return false
  }

  fun setStatusForEntrant(entrant: DiscordEntrant, newStatus: EntrantStatus): Boolean {

    if (entrant !in entrants || entrants[entrant] == newStatus) {
      return false
    }

    entrants[entrant] = newStatus
    return true
  }

  fun setStatusForAll(newStatus: EntrantStatus) {
    entrants.replaceAll { _, _ -> newStatus }
  }

  fun countPerStatus(): Map<EntrantStatus, Int> =
      entrants.values
          .groupingBy { it }
          .eachCount()
          .filter { it.value > 0 }

  enum class RaceState {
    OPEN, STARTING, UNDEFINED
  }

  enum class EntrantStatus {
    NOT_READY, READY, UNDEFINED
  }
}
