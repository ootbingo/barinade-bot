package ootbingo.barinade.bot.discord.racing

import com.google.gson.Gson
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import ootbingo.barinade.bot.discord.data.connection.DiscordPlayerRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceEntryRepository
import ootbingo.barinade.bot.discord.data.connection.DiscordRaceRepository
import ootbingo.barinade.bot.discord.data.model.*
import ootbingo.barinade.bot.discord.data.model.DiscordRaceEntry.*
import ootbingo.barinade.bot.extensions.exception
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

class DiscordRaceStatusHolder(
    private val playerRepository: DiscordPlayerRepository,
    private val raceRepository: DiscordRaceRepository,
    private val entryRepository: DiscordRaceEntryRepository,
    private val gson: Gson,
    discordChannel: TextChannel,
    initialType: DiscordRaceType,
) {

  private val logger = LoggerFactory.getLogger(DiscordRaceStatusHolder::class.java)

  var type: DiscordRaceType
  private val raceId: Long
  private val race get() = raceRepository.findById(raceId)!!
  private val additionalInfo = mutableMapOf<String, String>()

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

  fun getStatusForEntrant(entrant: User): DiscordRaceEntryState? =
      entryRepository.findByRaceIdAndPlayer(raceId, playerRepository.fromDiscordUser(entrant))?.state

  fun setStatusForEntrant(entrant: User, newState: DiscordRaceEntryState, finalTime: Duration? = null): Boolean {

    if (finalTime != null && newState != DiscordRaceEntryState.FINISHED) {
      throw IllegalArgumentException()
    }

    val entry = entryRepository.findByRaceIdAndPlayer(raceId, playerRepository.fromDiscordUser(entrant))

    if (entry == null || entry.state == newState) {
      return false
    }

    entry.state = newState
    if (newState == DiscordRaceEntryState.FINISHED && finalTime != null) {
      entry.place = 1
      entry.time = finalTime
    }
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

  fun addAdditionalInfo(key: String, value: String) {
    additionalInfo[key] = value
    updateDbRace { it.additionalInfo = gson.toJson(additionalInfo) }
  }

  var state: DiscordRaceState
    get() = race.state
    set(newState) {
      updateDbRace {
        if (it.state == newState) return@updateDbRace
        it.state = newState
        when (newState) {
          DiscordRaceState.PROGRESS -> it.startTime = Instant.now().also { t -> startTime = t }
          DiscordRaceState.FINISHED -> it.endTime = Instant.now()
          else -> {}
        }
      }
    }

  var startTime: Instant? = null
    private set

  private fun updateDbRace(block: (DiscordRace) -> Unit): Boolean = try {

    val dbRace = race
    block(dbRace)
    raceRepository.save(dbRace)
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
