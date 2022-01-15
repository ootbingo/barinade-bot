package ootbingo.barinade.bot.discord.racing

import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.connection.DiscordPlayerRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceEntryRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceRepository
import ootbingo.barinade.bot.discord.data.model.*
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntry.*
import ootbingo.barinade.bot.extensions.exception
import org.slf4j.LoggerFactory

class DiscordRaceStatusHolder(
    private val playerRepository: DiscordPlayerRepository,
    private val raceRepository: DiscordRaceRepository,
    private val entryRepository: DiscordRaceEntryRepository,
    discordChannel: TextChannel,
    initialType: DiscordRaceType,
) {

  private val logger = LoggerFactory.getLogger(DiscordRaceStatusHolder::class.java)

  var type: DiscordRaceType
  private val raceId: Long
  private val race get() = raceRepository.findById(raceId)!!

  init {
    type = initialType
    raceId = discordChannel.idLong
    raceRepository.save(
        DiscordRace(
            discordChannel.idLong,
            discordChannel.name,
            type,
        )
    )
  }

  fun addEntrant(entrant: User): Boolean {

    val player = try {
      playerRepository.fromDiscordUser(entrant)
    } catch (e: Exception) {
      logger.exception("Failed to initiate DiscordPlayer(${entrant.asTag})", e)
      return false
    }

    if (entryRepository.findByRaceIdAndPlayer(raceId, player) != null) {
      return false
    }

    entryRepository.save(DiscordRaceEntry(EntryId(race, player)))
    return true
  }

  fun removeEntrant(entrant: User): Boolean {

    (entryRepository.findByRaceIdAndPlayer(raceId, playerRepository.fromDiscordUser(entrant))
        ?: return false)
        .run {
          entryRepository.delete(this)
          return true
        }
  }

  fun setStatusForEntrant(entrant: User, newState: DiscordRaceEntryState): Boolean {

    val entry = entryRepository.findByRaceIdAndPlayer(raceId, playerRepository.fromDiscordUser(entrant))

    if (entry == null || entry.state == newState) {
      return false
    }

    entry.state = newState
    entryRepository.save(entry)
    return true
  }

  fun setStatusForAll(newState: DiscordRaceEntryState) {

    entryRepository.findAllByRaceId(raceId)
        .filter { it.state != newState }
        .onEach { it.state = newState }
        .takeIf { it.isNotEmpty() }
        ?.run {
          entryRepository.saveAll(this)
        }
  }

  fun countPerEntrantState(): Map<DiscordRaceEntryState, Int> =
      entryRepository.findAllByRaceId(raceId).map { it.state }
          .groupingBy { it }
          .eachCount()
          .filter { it.value > 0 }

  var state: DiscordRaceState
    get() = race.state
    set(newState) {
      val dbRace = race
      if (dbRace.state == newState) return
      dbRace.state = newState
      save(dbRace)
    }

  private fun save(race: DiscordRace): Boolean = try {
    raceRepository.save(race)
    true
  } catch (e: Throwable) {

    if (e !is Exception && e !is StackOverflowError) {
      throw e
    }

    logger.error("Called at: ${Thread.currentThread().stackTrace[2]}")
    logger.exception("Failed to update race information", e)
    false
  }
}
